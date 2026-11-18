package com.oficina.os.controller;

import com.oficina.os.enums.Prioridade;
import com.oficina.os.enums.StatusOS;
import com.oficina.os.model.OrdemServico;
import com.oficina.os.service.OrdemServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/ordens")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    // GET /ordens ou GET /ordens?status=ABERTA
    @GetMapping
    public List<OrdemServico> listar(@RequestParam(required = false) StatusOS status) {
        if (status != null) {
            return ordemServicoService.listarPorStatus(status);
        }
        return ordemServicoService.listarTodas();
    }

    // GET /ordens/1
    @GetMapping("/{id}")
    public OrdemServico buscar(@PathVariable Long id) {
        return ordemServicoService.buscarPorId(id);
    }

    // GET /ordens/cliente/1
    @GetMapping("/cliente/{clienteId}")
    public List<OrdemServico> listarPorCliente(@PathVariable Long clienteId) {
        return ordemServicoService.listarPorCliente(clienteId);
    }

    // GET /ordens/veiculo/1
    @GetMapping("/veiculo/{veiculoId}")
    public List<OrdemServico> listarPorVeiculo(@PathVariable Long veiculoId) {
        return ordemServicoService.listarPorVeiculo(veiculoId);
    }

    // GET /ordens/prioridade/ALTA
    @GetMapping("/prioridade/{prioridade}")
    public List<OrdemServico> listarPorPrioridade(@PathVariable Prioridade prioridade) {
        return ordemServicoService.listarPorPrioridade(prioridade);
    }

    // POST /ordens
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdemServico abrir(@RequestBody OrdemServico os) {
        return ordemServicoService.abrir(os);
    }

    // PUT /ordens/1/mecanico/2
    @PutMapping("/{id}/mecanico/{mecanicoId}")
    public OrdemServico atribuirMecanico(@PathVariable Long id, @PathVariable Long mecanicoId) {
        return ordemServicoService.atribuirMecanico(id, mecanicoId);
    }

    // PUT /ordens/1/concluir?valor=350.00
    @PutMapping("/{id}/concluir")
    public OrdemServico concluir(@PathVariable Long id,
                                 @RequestParam(required = false) BigDecimal valor,
                                 @RequestParam(required = false) String servicoExecutado) {
        return ordemServicoService.concluir(id, valor, servicoExecutado);
    }

    // PUT /ordens/1/cancelar
    @PutMapping("/{id}/cancelar")
    public OrdemServico cancelar(@PathVariable Long id) {
        return ordemServicoService.cancelar(id);
    }
}
