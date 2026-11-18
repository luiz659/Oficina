package com.oficina.os.service;

import com.oficina.os.enums.Prioridade;
import com.oficina.os.enums.StatusOS;
import com.oficina.os.model.Mecanico;
import com.oficina.os.model.OrdemServico;
import com.oficina.os.model.Veiculo;
import com.oficina.os.repository.OrdemServicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final VeiculoService veiculoService;
    private final MecanicoService mecanicoService;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               VeiculoService veiculoService,
                               MecanicoService mecanicoService) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.veiculoService = veiculoService;
        this.mecanicoService = mecanicoService;
    }

    // CONSULTAS (sem regra de negócio)

    public List<OrdemServico> listarTodas() {
        return ordemServicoRepository.findAll();
    }

    public List<OrdemServico> listarPorStatus(StatusOS status) {
        return ordemServicoRepository.findByStatus(status);
    }

    public List<OrdemServico> listarPorCliente(Long clienteId) {
        return ordemServicoRepository.findByVeiculoClienteId(clienteId);
    }

    public List<OrdemServico> listarPorVeiculo(Long veiculoId) {
        return ordemServicoRepository.findByVeiculoId(veiculoId);
    }

    public List<OrdemServico> listarPorPrioridade(Prioridade prioridade) {
        return ordemServicoRepository.findByPrioridade(prioridade);
    }

    public OrdemServico buscarPorId(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ordem de serviço não encontrada: " + id));
    }

    // REGRA 2: a OS só pode ser aberta para um veículo cadastrado de um cliente da oficina
    private Veiculo validarVeiculoCadastrado(OrdemServico os) {
        if (os.getVeiculo() == null || os.getVeiculo().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe o id do veículo");
        }
        Veiculo veiculo = veiculoService.buscarPorId(os.getVeiculo().getId());
        if (veiculo.getCliente() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "O veículo não está vinculado a um cliente");
        }
        return veiculo;
    }

    // REGRA 3: ao abrir, a OS recebe a data de abertura e o status ABERTA
    public OrdemServico abrir(OrdemServico os) {
        if (os.getDescricaoProblema() == null || os.getDescricaoProblema().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe a descrição do problema");
        }
        Veiculo veiculo = validarVeiculoCadastrado(os); // usa a Regra 2
        os.setVeiculo(veiculo);
        os.setDataAbertura(LocalDateTime.now());
        os.setStatus(StatusOS.ABERTA);
        // a OS nasce sem mecânico, sem valor e sem data de fechamento
        os.setMecanico(null);
        os.setValor(null);
        os.setDataFechamento(null);
        return ordemServicoRepository.save(os);
    }

    // REGRA 4: uma OS CONCLUIDA ou CANCELADA não pode mais ser alterada
    private void validarOsNaoFinalizada(OrdemServico os) {
        if (os.getStatus() == StatusOS.CONCLUIDA
                || os.getStatus() == StatusOS.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A OS " + os.getId() + " já está " + os.getStatus());
        }
    }

    // REGRA 5: ao atribuir um mecânico, a OS passa para EM_ANDAMENTO
    public OrdemServico atribuirMecanico(Long osId, Long mecanicoId) {
        OrdemServico os = buscarPorId(osId);
        validarOsNaoFinalizada(os); // usa a Regra 4

        Mecanico mecanico = mecanicoService.buscarPorId(mecanicoId);
        if (!Boolean.TRUE.equals(mecanico.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Mecânico inativo não pode receber OS");
        }
        os.setMecanico(mecanico);
        os.setStatus(StatusOS.EM_ANDAMENTO);
        return ordemServicoRepository.save(os);
    }

    // REGRA 10: o valor ao concluir uma OS deve ser maior que zero
    private void validarValorConclusao(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O valor da OS deve ser maior que zero");
        }
    }

    // REGRA 6: só é possível concluir uma OS que tenha mecânico
    private void validarMecanicoAtribuido(OrdemServico os) {
        if (os.getMecanico() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Atribua um mecânico antes de concluir");
        }
    }

    // REGRA 7: ao concluir, a OS recebe valor, data de fechamento e status CONCLUIDA
    public OrdemServico concluir(Long osId, BigDecimal valor, String servicoExecutado) {
        OrdemServico os = buscarPorId(osId);
        validarOsNaoFinalizada(os); // usa a Regra 4
        validarMecanicoAtribuido(os); // usa a Regra 6
        validarValorConclusao(valor); // usa a Regra 10

        os.setValor(valor);
        if (servicoExecutado != null && !servicoExecutado.isBlank()) {
            os.setServicoExecutado(servicoExecutado.trim());
        }
        os.setDataFechamento(LocalDateTime.now());
        os.setStatus(StatusOS.CONCLUIDA);
        return ordemServicoRepository.save(os);
    }

    // REGRA 8: ao cancelar, a OS recebe data de fechamento e status CANCELADA
    public OrdemServico cancelar(Long osId) {
        OrdemServico os = buscarPorId(osId);
        validarOsNaoFinalizada(os); // usa a Regra 4

        os.setDataFechamento(LocalDateTime.now());
        os.setStatus(StatusOS.CANCELADA);
        return ordemServicoRepository.save(os);
    }
}
