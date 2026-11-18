package com.oficina.os.service;

import com.oficina.os.model.Cliente;
import com.oficina.os.model.Veiculo;
import com.oficina.os.repository.OrdemServicoRepository;
import com.oficina.os.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;
    private final OrdemServicoRepository ordemServicoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository,
                          ClienteService clienteService,
                          OrdemServicoRepository ordemServicoRepository) {
        this.veiculoRepository = veiculoRepository;
        this.clienteService = clienteService;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public List<Veiculo> listarPorCliente(Long clienteId) {
        clienteService.buscarPorId(clienteId);
        return veiculoRepository.findByClienteId(clienteId);
    }

    public Veiculo buscarPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Veículo não encontrado: " + id));
    }

    public Veiculo salvar(Veiculo veiculo) {
        validarDados(veiculo);
        veiculo.setPlaca(normalizarPlaca(veiculo.getPlaca()));
        if (veiculoRepository.existsByPlaca(veiculo.getPlaca())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Já existe veículo com esta placa");
        }
        veiculo.setCliente(buscarCliente(veiculo));
        return veiculoRepository.save(veiculo);
    }

    public Veiculo atualizar(Long id, Veiculo dados) {
        Veiculo veiculo = buscarPorId(id);
        validarDados(dados);
        String placa = normalizarPlaca(dados.getPlaca());
        if (!placa.equals(veiculo.getPlaca()) && veiculoRepository.existsByPlaca(placa)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Já existe veículo com esta placa");
        }
        veiculo.setPlaca(placa);
        veiculo.setMarca(dados.getMarca());
        veiculo.setModelo(dados.getModelo());
        veiculo.setAno(dados.getAno());
        return veiculoRepository.save(veiculo);
    }

    public void excluir(Long id) {
        Veiculo veiculo = buscarPorId(id);
        validarSemOrdemDeServico(veiculo.getId());
        veiculoRepository.delete(veiculo);
    }

    // REGRA 9: veículo com ordem de serviço não pode ser excluído
    private void validarSemOrdemDeServico(Long veiculoId) {
        if (ordemServicoRepository.existsByVeiculoId(veiculoId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Veículo com ordem de serviço não pode ser excluído");
        }
    }

    private void validarDados(Veiculo veiculo) {
        if (veiculo.getPlaca() == null || veiculo.getPlaca().isBlank()
                || veiculo.getMarca() == null || veiculo.getMarca().isBlank()
                || veiculo.getModelo() == null || veiculo.getModelo().isBlank()
                || veiculo.getAno() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe placa, marca, modelo e ano do veículo");
        }
        if (veiculo.getAno() < 1950 || veiculo.getAno() > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ano do veículo inválido");
        }
    }

    private String normalizarPlaca(String placa) {
        return placa.trim().toUpperCase();
    }

    private Cliente buscarCliente(Veiculo veiculo) {
        if (veiculo.getCliente() == null || veiculo.getCliente().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o id do cliente");
        }
        return clienteService.buscarPorId(veiculo.getCliente().getId());
    }
}
