package com.oficina.os.controller;

import com.oficina.os.model.Mecanico;
import com.oficina.os.service.MecanicoService;
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
@RequestMapping("/mecanicos")
public class MecanicoController {

    private final MecanicoService mecanicoService;

    public MecanicoController(MecanicoService mecanicoService) {
        this.mecanicoService = mecanicoService;
    }

    // GET /mecanicos
    @GetMapping
    public List<Mecanico> listar() {
        return mecanicoService.listarTodos();
    }

    // GET /mecanicos/1
    @GetMapping("/{id}")
    public Mecanico buscar(@PathVariable Long id) {
        return mecanicoService.buscarPorId(id);
    }

    // POST /mecanicos
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mecanico cadastrar(@RequestBody Mecanico mecanico) {
        return mecanicoService.salvar(mecanico);
    }

    // PUT /mecanicos/1
    @PutMapping("/{id}")
    public Mecanico atualizar(@PathVariable Long id, @RequestBody Mecanico mecanico) {
        return mecanicoService.atualizar(id, mecanico);
    }

    // DELETE /mecanicos/1
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        mecanicoService.excluir(id);
    }
}
