package com.orbe.reembolso.servico;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.dominio.RecursoNaoEncontradoException;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.ReembolsoDuplicadoException;

@Service
@Transactional
public class ReembolsoService {

    private final ReembolsoRepository repositorio;
    private final PoliticaDePrazo politicaDePrazo;
    private final Clock clock;

    public ReembolsoService(ReembolsoRepository repositorio, PoliticaDePrazo politicaDePrazo, Clock clock) {
        this.repositorio = repositorio;
        this.politicaDePrazo = politicaDePrazo;
        this.clock = clock;
    }

    public Reembolso submeter(NovoReembolso comando) {
        politicaDePrazo.validar(comando.dataDespesa());

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
                agora());
        return repositorio.save(reembolso);
    }

    public Reembolso aprovar(Long id, String responsavel) {
        Reembolso reembolso = buscar(id);
        reembolso.aprovar(responsavel, agora());
        return repositorio.save(reembolso);
    }

    public Reembolso rejeitar(Long id, String responsavel, String motivo) {
        Reembolso reembolso = buscar(id);
        reembolso.rejeitar(responsavel, motivo, agora());
        return repositorio.save(reembolso);
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
