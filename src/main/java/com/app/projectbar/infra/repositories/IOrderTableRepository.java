package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.OrderTable;
import com.app.projectbar.domain.enums.OrderTableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IOrderTableRepository extends JpaRepository<OrderTable, Long> {

    /**
     * Busca una mesa por su número.
     */
    Optional<OrderTable> findByNumber(Integer number);

    /**
     * Busca todas las mesas por estado.
     */
    List<OrderTable> findByStatus(OrderTableStatus status);

    /**
     * Verifica si existe una mesa con el número dado.
     */
    boolean existsByNumber(Integer number);
}
