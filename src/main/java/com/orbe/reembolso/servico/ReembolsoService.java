package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.config.ClockConfig;
import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.ChaveDiaRefeicao;
import com.orbe.reembolso.dominio.RecursoNaoEncontradoException;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.ReembolsoDuplicadoException;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.dominio.VersaoPolitica;

import jakarta.persistence.EntityManager;

@Service
@Transactional
public class ReembolsoService {

    private static final LocalDateTime INICIO_V3 = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final List<StatusReembolso> ESTADOS_CONSUMIDORES = List.of(
            StatusReembolso.SUBMETIDA, StatusReembolso.AGUARDANDO_FINANCEIRO,
            StatusReembolso.APROVADA, StatusReembolso.PAGA);

    private final ReembolsoRepository repositorio;
    private final PoliticaDePrazo politicaDePrazo;
    private final PoliticaDeVigencia politicaDeVigencia;
    private final PoliticaDeAprovacao politicaDeAprovacao;
    private final PoliticaDeRejeicao politicaDeRejeicao;
    private final PoliticaDeRefeicao politicaDeRefeicao;
    private final ControleDeTetoDiario controleDeTetoDiario;
    private final EntityManager entityManager;
    private final Clock clock;

    public ReembolsoService(ReembolsoRepository repositorio, PoliticaDePrazo politicaDePrazo,
                            PoliticaDeVigencia politicaDeVigencia, PoliticaDeAprovacao politicaDeAprovacao,
                            PoliticaDeRejeicao politicaDeRejeicao, PoliticaDeRefeicao politicaDeRefeicao,
                            ControleDeTetoDiario controleDeTetoDiario, EntityManager entityManager, Clock clock) {
        this.repositorio = repositorio;
        this.politicaDePrazo = politicaDePrazo;
        this.politicaDeVigencia = politicaDeVigencia;
        this.politicaDeAprovacao = politicaDeAprovacao;
        this.politicaDeRejeicao = politicaDeRejeicao;
        this.politicaDeRefeicao = politicaDeRefeicao;
        this.controleDeTetoDiario = controleDeTetoDiario;
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reembolso submeter(NovoReembolso comando) {
        LocalDateTime submetidoEm = agora();
        politicaDePrazo.validar(comando.dataDespesa(), submetidoEm.toLocalDate());
        boolean v3 = politicaDeVigencia.classificar(submetidoEm) == VersaoPolitica.V3;
        if (v3) {
            politicaDeAprovacao.validarSubmissao(comando.valor());
        }

        ChaveDiaRefeicao chave = null;
        if (v3 && comando.categoria() == Categoria.REFEICAO) {
            chave = new ChaveDiaRefeicao(comando.colaborador(), comando.dataDespesa());
            bloquearDia(chave);
        }

        boolean duplicado = repositorio.existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
                comando.colaborador(), comando.cnpjFornecedor(), comando.valor(), comando.dataDespesa());
        if (duplicado) {
            throw new ReembolsoDuplicadoException();
        }

        if (chave != null) {
            politicaDeRefeicao.validarSubmissao(totalReservado(chave), comando.valor());
        }

        Reembolso reembolso = new Reembolso(
                comando.colaborador(),
                comando.categoria(),
                comando.valor(),
                comando.dataDespesa(),
                comando.cnpjFornecedor(),
                comando.descricao(),
                submetidoEm);
        return repositorio.saveAndFlush(reembolso);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reembolso aprovar(Long id, DecidirReembolso comando) {
        LocalDateTime quando = agora();
        Reembolso reembolso = buscarParaDecidir(id, comando);
        if (politicaDe(reembolso) == VersaoPolitica.V2) {
            reembolso.aprovar(comando.responsavel(), quando);
        } else {
            boolean exigeFinanceiro = politicaDeAprovacao.validar(reembolso, comando);
            if (reembolso.getCategoria() == Categoria.REFEICAO) {
                politicaDeRefeicao.validarAprovacao(totalReservado(chaveDe(reembolso)));
            }
            // Atualizar a versão antes do INSERT IDENTITY do histórico evita que a
            // unicidade do evento mascare o conflito otimista. O rollback abrange ambos.
            reembolso.aprovar(comando.etapa(), comando.responsavel(), quando, exigeFinanceiro,
                    entityManager::flush);
        }
        return repositorio.saveAndFlush(reembolso);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Reembolso rejeitar(Long id, DecidirReembolso comando) {
        LocalDateTime quando = agora();
        Reembolso reembolso = buscarParaDecidir(id, comando);
        if (politicaDe(reembolso) == VersaoPolitica.V2) {
            reembolso.rejeitar(comando.responsavel(), comando.motivo(), quando);
        } else {
            politicaDeRejeicao.validar(reembolso, comando);
            reembolso.rejeitar(comando.etapa(), comando.responsavel(), comando.motivo(), quando,
                    entityManager::flush);
        }
        return repositorio.saveAndFlush(reembolso);
    }

    @Transactional(readOnly = true)
    public Reembolso buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public List<Reembolso> listarPorColaborador(String colaborador) {
        return repositorio.findByColaboradorOrderByDataDespesaDescIdDesc(colaborador);
    }

    @Transactional(readOnly = true)
    public VersaoPolitica politicaDe(Reembolso reembolso) {
        return politicaDeVigencia.classificar(reembolso.getSubmetidoEm());
    }

    private Reembolso buscarParaDecidir(Long id, DecidirReembolso comando) {
        Reembolso inicial = buscar(id);
        validarEtapaRequerida(inicial, comando);
        if (politicaDe(inicial) != VersaoPolitica.V3 || inicial.getCategoria() != Categoria.REFEICAO) {
            return inicial;
        }
        // A leitura inicial valida a etapa explícita e descobre a chave, sem autorizar ou modificar.
        bloquearDia(chaveDe(inicial));
        // Não há escritas pendentes. Descartar também histórico/versão do primeiro nível
        // força a releitura após a espera. O clear não libera o lock transacional da âncora.
        entityManager.clear();
        Reembolso atual = buscar(id);
        validarEtapaRequerida(atual, comando);
        return atual;
    }

    private void validarEtapaRequerida(Reembolso reembolso, DecidirReembolso comando) {
        // Ausência de etapa continua sendo erro contextual da política v3, não transição.
        if (politicaDe(reembolso) == VersaoPolitica.V3 && comando.etapa() != null) {
            reembolso.exigirEtapa(comando.etapa(), "decidir na etapa " + comando.etapa());
        }
    }

    private void bloquearDia(ChaveDiaRefeicao chave) {
        // A garantia REQUIRES_NEW deve ocorrer antes de qualquer escrita ou lock externo.
        controleDeTetoDiario.garantirExistencia(chave);
        controleDeTetoDiario.bloquear(chave);
    }

    private ChaveDiaRefeicao chaveDe(Reembolso reembolso) {
        return new ChaveDiaRefeicao(reembolso.getColaborador(), reembolso.getDataDespesa());
    }

    private BigDecimal totalReservado(ChaveDiaRefeicao chave) {
        return repositorio.somarRefeicoesReservadas(
                chave.getColaborador(), chave.getDataDespesa(), INICIO_V3, ESTADOS_CONSUMIDORES);
    }

    private LocalDateTime agora() {
        return LocalDateTime.ofInstant(clock.instant(), ClockConfig.FUSO_PADRAO);
    }
}
