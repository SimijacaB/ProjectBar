package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IOrderTableService;
import com.app.projectbar.application.mapper.OrderTableMapper;
import com.app.projectbar.domain.OrderTable;
import com.app.projectbar.domain.dto.orderTable.OrderTableRequestDTO;
import com.app.projectbar.domain.dto.orderTable.OrderTableResponseDTO;
import com.app.projectbar.domain.dto.orderTable.UpdateOrderTableStatusDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.OrderTableStatus;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTableServiceImpl implements IOrderTableService {

    private final IOrderTableRepository orderTableRepository;
    private final IOrderRepository orderRepository;
    private final OrderTableMapper orderTableMapper;

    // Estados de órdenes que se consideran "activas" para una mesa
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.ASSIGNED,
            OrderStatus.IN_PROGRESS,
            OrderStatus.READY
    );

    /**
     * Crea una nueva mesa en el restaurante.
     * Este método es usado por el ADMINISTRADOR para registrar las mesas físicas del restaurante.
     * Las mesas deben crearse ANTES de que los clientes puedan hacer pedidos.
     *
     * @param orderTableRequest DTO con número de mesa, capacidad y notas opcionales
     * @return OrderTableResponseDTO con la información de la mesa creada
     * @throws RuntimeException si ya existe una mesa con ese número
     */
    @Override
    @Transactional
    public OrderTableResponseDTO create(OrderTableRequestDTO orderTableRequest) {
        // Verificar si ya existe una mesa con ese número
        if (orderTableRepository.existsByNumber(orderTableRequest.getNumber())) {
            throw new RuntimeException("Ya existe una mesa con el número " + orderTableRequest.getNumber());
        }

        // Crear la nueva mesa con estado FREE (disponible)
        OrderTable orderTable = OrderTable.builder()
                .number(orderTableRequest.getNumber())
                .capacity(orderTableRequest.getCapacity())
                .status(OrderTableStatus.FREE) // Las mesas nuevas inician como disponibles
                .notes(orderTableRequest.getNotes())
                .build();

        OrderTable savedTable = orderTableRepository.save(orderTable);
        return enrichWithActiveOrdersCount(orderTableMapper.toResponseDTO(savedTable));
    }

    @Override
    public List<OrderTableResponseDTO> findAll() {
        List<OrderTable> orderTables = orderTableRepository.findAll();
        return orderTableMapper.toResponseDTOList(orderTables).stream()
                .map(this::enrichWithActiveOrdersCount)
                .collect(Collectors.toList());
    }

    @Override
    public OrderTableResponseDTO findById(Long id) {
        OrderTable orderTable = orderTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        return enrichWithActiveOrdersCount(orderTableMapper.toResponseDTO(orderTable));
    }

    @Override
    public OrderTableResponseDTO findByNumber(Integer number) {
        OrderTable orderTable = orderTableRepository.findByNumber(number)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con número: " + number));
        return enrichWithActiveOrdersCount(orderTableMapper.toResponseDTO(orderTable));
    }

    @Override
    @Transactional
    public OrderTableResponseDTO update(Long id, OrderTableRequestDTO orderTableRequest) {
        OrderTable orderTable = orderTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        // Verificar si el nuevo número ya existe en otra mesa
        if (!orderTable.getNumber().equals(orderTableRequest.getNumber())
                && orderTableRepository.existsByNumber(orderTableRequest.getNumber())) {
            throw new RuntimeException("Ya existe una mesa con el número " + orderTableRequest.getNumber());
        }

        orderTable.setNumber(orderTableRequest.getNumber());
        orderTable.setCapacity(orderTableRequest.getCapacity());
        orderTable.setNotes(orderTableRequest.getNotes());

        OrderTable savedTable = orderTableRepository.save(orderTable);
        return enrichWithActiveOrdersCount(orderTableMapper.toResponseDTO(savedTable));
    }

    @Override
    @Transactional
    public OrderTableResponseDTO updateStatus(Long id, UpdateOrderTableStatusDTO updateStatusDTO) {
        return updateStatus(id, updateStatusDTO.getStatus());
    }

    @Override
    @Transactional
    public OrderTableResponseDTO updateStatus(Long id, OrderTableStatus status) {
        OrderTable orderTable = orderTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        orderTable.setStatus(status);

        OrderTable savedTable = orderTableRepository.save(orderTable);
        return enrichWithActiveOrdersCount(orderTableMapper.toResponseDTO(savedTable));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        OrderTable orderTable = orderTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));

        // Verificar si hay órdenes activas en esta mesa
        List<com.app.projectbar.domain.Order> activeOrders = orderRepository.findByTableNumberAndStatusNot(
                orderTable.getNumber(),
                OrderStatus.BILLED
        );

        if (!activeOrders.isEmpty()) {
            throw new RuntimeException("No se puede eliminar la mesa. Tiene órdenes activas.");
        }

        orderTableRepository.delete(orderTable);
    }

    @Override
    public List<OrderTableResponseDTO> findByStatus(OrderTableStatus status) {
        List<OrderTable> orderTables = orderTableRepository.findByStatus(status);
        return orderTableMapper.toResponseDTOList(orderTables).stream()
                .map(this::enrichWithActiveOrdersCount)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumber(Integer number) {
        return orderTableRepository.existsByNumber(number);
    }

    /**
     * Enriquece el DTO con el conteo de órdenes activas de la mesa.
     */
    private OrderTableResponseDTO enrichWithActiveOrdersCount(OrderTableResponseDTO dto) {
        int activeCount = (int) orderRepository.findByTableNumber(dto.getNumber()).stream()
                .filter(order -> ACTIVE_STATUSES.contains(order.getStatus()))
                .count();
        dto.setActiveOrdersCount(activeCount);
        return dto;
    }
}
