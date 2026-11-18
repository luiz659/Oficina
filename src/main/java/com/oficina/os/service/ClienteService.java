package com.oficina.os.service;

import com.oficina.os.model.Cliente;
import com.oficina.os.repository.ClienteRepository;
import com.oficina.os.repository.OrdemServicoRepository;
import com.oficina.os.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public ClienteService(ClienteRepository clienteRepository,
                          VeiculoRepository veiculoRepository,
                          OrdemServicoRepository ordemServicoRepository) {
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado: " + id));
    }

    // REGRA 1: não pode existir dois clientes com o mesmo CPF
    public Cliente salvar(Cliente cliente) {
        if (clienteRepository.existsByCpf(cliente.getCpf())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Já existe cliente com este CPF");
        }
        return clienteRepository.save(cliente);
    }

    public Cliente atualizar(Long id, Cliente dados) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(dados.getNome());
        cliente.setTelefone(dados.getTelefone());
        cliente.setEmail(dados.getEmail());
        return clienteRepository.save(cliente);
    }

    public void excluir(Long id) {
        Cliente cliente = buscarPorId(id);
        validarSemOrdemDeServico(cliente.getId());
        if (veiculoRepository.existsByClienteId(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cliente possui veículos cadastrados. Exclua os veículos antes");
        }
        clienteRepository.delete(cliente);
    }

    // REGRA 9: cliente com ordem de serviço, ou com veículo que tenha OS, não pode ser excluído
    private void validarSemOrdemDeServico(Long clienteId) {
        if (ordemServicoRepository.existsByVeiculoClienteId(clienteId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cliente com ordem de serviço não pode ser excluído");
        }
    }
}
