package com.oficina.os.service;

import com.oficina.os.model.Mecanico;
import com.oficina.os.repository.MecanicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MecanicoService {

    private final MecanicoRepository mecanicoRepository;

    public MecanicoService(MecanicoRepository mecanicoRepository) {
        this.mecanicoRepository = mecanicoRepository;
    }

    public List<Mecanico> listarTodos() {
        return mecanicoRepository.findAll();
    }

    public Mecanico buscarPorId(Long id) {
        return mecanicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Mecânico não encontrado: " + id));
    }

    public Mecanico salvar(Mecanico mecanico) {
        if (mecanico.getNome() == null || mecanico.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do mecânico");
        }
        if (mecanico.getAtivo() == null) {
            mecanico.setAtivo(true);
        }
        return mecanicoRepository.save(mecanico);
    }

    public Mecanico atualizar(Long id, Mecanico dados) {
        Mecanico mecanico = buscarPorId(id);
        if (dados.getNome() == null || dados.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do mecânico");
        }
        mecanico.setNome(dados.getNome());
        mecanico.setEspecialidade(dados.getEspecialidade());
        if (dados.getAtivo() != null) {
            mecanico.setAtivo(dados.getAtivo());
        }
        return mecanicoRepository.save(mecanico);
    }

    public void excluir(Long id) {
        Mecanico mecanico = buscarPorId(id);
        mecanicoRepository.delete(mecanico);
    }
}
