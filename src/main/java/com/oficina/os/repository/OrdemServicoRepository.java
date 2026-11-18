package com.oficina.os.repository;

import com.oficina.os.enums.Prioridade;
import com.oficina.os.enums.StatusOS;
import com.oficina.os.model.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // SELECT * FROM ordens_servico WHERE status = ?
    List<OrdemServico> findByStatus(StatusOS status);

    // OS dos veículos deste cliente
    List<OrdemServico> findByVeiculoClienteId(Long clienteId);

    // SELECT * FROM ordens_servico WHERE veiculo_id = ?
    List<OrdemServico> findByVeiculoId(Long veiculoId);

    // SELECT * FROM ordens_servico WHERE prioridade = ?
    List<OrdemServico> findByPrioridade(Prioridade prioridade);

    boolean existsByVeiculoClienteId(Long clienteId);

    boolean existsByVeiculoId(Long veiculoId);
}
