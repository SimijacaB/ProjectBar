package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.orderTable.OrderTableRequestDTO;
import com.app.projectbar.domain.dto.orderTable.OrderTableResponseDTO;
import com.app.projectbar.domain.dto.orderTable.UpdateOrderTableStatusDTO;
import com.app.projectbar.domain.enums.OrderTableStatus;

import java.util.List;

public interface IOrderTableService {

    /**
     * Crea una nueva mesa.
     */
    OrderTableResponseDTO create(OrderTableRequestDTO orderTableRequest);

    /**
     * Obtiene todas las mesas.
     */
    List<OrderTableResponseDTO> findAll();

    /**
     * Obtiene una mesa por su ID.
     */
    OrderTableResponseDTO findById(Long id);

    /**
     * Obtiene una mesa por su número.
     */
    OrderTableResponseDTO findByNumber(Integer number);

    /**
     * Actualiza una mesa.
     */
    OrderTableResponseDTO update(Long id, OrderTableRequestDTO orderTableRequest);

    /**
     * Actualiza el estado de una mesa.
     */
    OrderTableResponseDTO updateStatus(Long id, UpdateOrderTableStatusDTO updateStatusDTO);

    /**
     * Actualiza el estado de una mesa directamente.
     */
    OrderTableResponseDTO updateStatus(Long id, OrderTableStatus status);

    /**
     * Elimina una mesa.
     */
    void delete(Long id);

    /**
     * Obtiene todas las mesas por estado.
     */
    List<OrderTableResponseDTO> findByStatus(OrderTableStatus status);

    /**
     * Verifica si una mesa existe por su número.
     */
    boolean existsByNumber(Integer number);
}
