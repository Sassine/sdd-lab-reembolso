package com.orbe.reembolso.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.servico.ReembolsoService;

@RestController
@RequestMapping("/reembolsos")
public class ReembolsoController {

    private final ReembolsoService servico;

    public ReembolsoController(ReembolsoService servico) {
        this.servico = servico;
    }

    @PostMapping
    public ResponseEntity<ReembolsoResponse> submeter(@Valid @RequestBody NovoReembolsoRequest pedido) {
        Reembolso criado = servico.submeter(pedido.paraComando());
        return ResponseEntity.created(URI.create("/reembolsos/" + criado.getId()))
                .body(ReembolsoResponse.de(criado));
    }

    @GetMapping("/{id}")
    public ReembolsoResponse buscar(@PathVariable Long id) {
        return ReembolsoResponse.de(servico.buscar(id));
    }

    @GetMapping
    public List<ReembolsoResponse> listar(@RequestParam String colaborador) {
        return servico.listarPorColaborador(colaborador).stream().map(ReembolsoResponse::de).toList();
    }

    @PostMapping("/{id}/aprovacao")
    public ReembolsoResponse aprovar(@PathVariable Long id, @Valid @RequestBody AprovacaoRequest aprovacao) {
        return ReembolsoResponse.de(servico.aprovar(id, aprovacao.responsavel().trim(), aprovacao.papel()));
    }

    @PostMapping("/{id}/rejeicao")
    public ReembolsoResponse rejeitar(@PathVariable Long id, @Valid @RequestBody DecisaoRequest decisao) {
        return ReembolsoResponse.de(servico.rejeitar(id, decisao.responsavel().trim(), decisao.motivo()));
    }
}
