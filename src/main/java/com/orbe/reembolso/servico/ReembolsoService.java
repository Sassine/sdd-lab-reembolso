package com.orbe.reembolso.servico;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.PapelAprovador;
import com.orbe.reembolso.dominio.RecursoNaoEncontradoException;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.ReembolsoDuplicadoException;

@Service
@Transactional
public class ReembolsoService {

    private final ReembolsoRepository repositorio;
    private final PoliticaDePrazo politicaDePrazo;
    private final PoliticaDeAlcada politicaDeAlcada;
    private final PoliticaDeSegregacao politicaDeSegregacao;
    private final PoliticaTetoRefeicao politicaTetoRefeicao;
    private final Clock clock;

    public ReembolsoService(ReembolsoRepository repositorio, PoliticaDePrazo politicaDePrazo,
                            PoliticaDeAlcada politicaDeAlcada, PoliticaDeSegregacao politicaDeSegregacao,
                            PoliticaTetoRefeicao politicaTetoRefeicao, Clock clock) {
        this.repositorio = repositorio;
        this.politicaDePrazo = politicaDePrazo;
        this.politicaDeAlcada = politicaDeAlcada;
        this.politicaDeSegregacao = politicaDeSegregacao;
        this.politicaTetoRefeicao = politicaTetoRefeicao;
        this.clock = clock;
    }

    /** Ordem importa (plano 002): prazo, alçada, duplicidade e só então o teto. */
    public Reembolso submeter(NovoReembolso comando) {
        politicaDePrazo.validar(comando.dataDespesa());
        politicaDeAlcada.validarSubmissao(comando.valor());

        boolean duplicado = repositorio.existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
                comando.colaborador(), comando.cnpjFornecedor(), comando.valor(), comando.dataDespesa());
        if (duplicado) {
            throw new ReembolsoDuplicadoException();
        }
        if (comando.categoria() == Categoria.REFEICAO) {
            politicaTetoRefeicao.validar(comando.colaborador(), comando.dataDespesa(), comando.valor());
        }

        Reembolso reembolso = new Reembolso(
                comando.colaborador(),
                comando.categoria(),
                comando.valor(),
                comando.dataDespesa(),
                comando.cnpjFornecedor(),
                comando.descricao(),
                agora());
        return repositorio.save(reembolso);
    }

    /** saveAndFlush: o conflito de versão (R7) aparece aqui dentro, não no commit. */
    public Reembolso aprovar(Long id, String responsavel, PapelAprovador papel) {
        Reembolso reembolso = buscar(id);
        politicaDeSegregacao.validar(reembolso, responsavel, papel);
        reembolso.aprovar(responsavel, papel, politicaDeAlcada.exigeFinanceiro(reembolso.getValor()), agora());
        return repositorio.saveAndFlush(reembolso);
    }

    public Reembolso rejeitar(Long id, String responsavel, String motivo) {
        Reembolso reembolso = buscar(id);
        reembolso.rejeitar(responsavel, motivo, agora());
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

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
