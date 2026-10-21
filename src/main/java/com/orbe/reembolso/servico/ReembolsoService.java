package com.orbe.reembolso.servico;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.config.ClockConfig;
import com.orbe.reembolso.dominio.RecursoNaoEncontradoException;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.ReembolsoDuplicadoException;
import com.orbe.reembolso.dominio.VersaoPolitica;

@Service
@Transactional
public class ReembolsoService {

    private final ReembolsoRepository repositorio;
    private final PoliticaDePrazo politicaDePrazo;
    private final PoliticaDeVigencia politicaDeVigencia;
    private final PoliticaDeAprovacao politicaDeAprovacao;
    private final Clock clock;

    public ReembolsoService(ReembolsoRepository repositorio, PoliticaDePrazo politicaDePrazo,
                            PoliticaDeVigencia politicaDeVigencia, PoliticaDeAprovacao politicaDeAprovacao,
                            Clock clock) {
        this.repositorio = repositorio;
        this.politicaDePrazo = politicaDePrazo;
        this.politicaDeVigencia = politicaDeVigencia;
        this.politicaDeAprovacao = politicaDeAprovacao;
        this.clock = clock;
    }

    public Reembolso submeter(NovoReembolso comando) {
        LocalDateTime submetidoEm = agora();
        politicaDePrazo.validar(comando.dataDespesa(), submetidoEm.toLocalDate());
        if (politicaDeVigencia.classificar(submetidoEm) == VersaoPolitica.V3) {
            politicaDeAprovacao.validarSubmissao(comando.valor());
        }

        boolean duplicado = repositorio.existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
                comando.colaborador(), comando.cnpjFornecedor(), comando.valor(), comando.dataDespesa());
        if (duplicado) {
            throw new ReembolsoDuplicadoException();
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

    public Reembolso aprovar(Long id, DecidirReembolso comando) {
        LocalDateTime quando = agora();
        Reembolso reembolso = buscar(id);
        if (politicaDe(reembolso) == VersaoPolitica.V2) {
            reembolso.aprovar(comando.responsavel(), quando);
        } else {
            boolean exigeFinanceiro = politicaDeAprovacao.validar(reembolso, comando);
            reembolso.aprovar(comando.etapa(), comando.responsavel(), quando, exigeFinanceiro);
        }
        return repositorio.saveAndFlush(reembolso);
    }

    public Reembolso rejeitar(Long id, String responsavel, String motivo) {
        LocalDateTime quando = agora();
        Reembolso reembolso = buscar(id);
        reembolso.rejeitar(responsavel, motivo, quando);
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

    private LocalDateTime agora() {
        return LocalDateTime.ofInstant(clock.instant(), ClockConfig.FUSO_PADRAO);
    }
}
