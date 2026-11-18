package com.oficina.os.controller;

import com.oficina.os.model.Veiculo;
import com.oficina.os.service.VeiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    // GET /veiculos
    @GetMapping
    public List<Veiculo> listar() {
        return veiculoService.listarTodos();
    }

    // GET /veiculos/cliente/1
    @GetMapping("/cliente/{clienteId}")
    public List<Veiculo> listarPorCliente(@PathVariable Long clienteId) {
        return veiculoService.listarPorCliente(clienteId);
    }

    // GET /veiculos/1
    @GetMapping("/{id}")
    public Veiculo buscar(@PathVariable Long id) {
        return veiculoService.buscarPorId(id);
    }

    // POST /veiculos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Veiculo cadastrar(@RequestBody Veiculo veiculo) {
        return veiculoService.salvar(veiculo);
    }

    // PUT /veiculos/1
    @PutMapping("/{id}")
    public Veiculo atualizar(@PathVariable Long id, @RequestBody Veiculo veiculo) {
        return veiculoService.atualizar(id, veiculo);
    }

    // DELETE /veiculos/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        veiculoService.excluir(id);
    }
}
