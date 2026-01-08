package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IOrderTableService;
import com.app.projectbar.domain.OrderTable;
import com.app.projectbar.domain.dto.orderTable.OrderTableRequestDTO;
import com.app.projectbar.domain.dto.orderTable.OrderTableResponseDTO;
import com.app.projectbar.domain.dto.orderTable.UpdateOrderTableStatusDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.OrderTableStatus;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderTableServiceImpl implements IOrderTableService {

    private final IOrderTableRepository orderTableRepository;
    private final IOrderRepository orderRepository;
    private final ModelMapper modelMapper;

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

        OrderTable savedOrderTable = orderTableRepository.save(orderTable);
        return buildOrderTableResponseDTO(savedOrderTable);
    }

    @Override
    public List<OrderTableResponseDTO> findAll() {
        List<OrderTable> orderTables = orderTableRepository.findAll();
        return orderTables.stream()
                .map(this::buildOrderTableResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public OrderTableResponseDTO findById(Long id) {
        OrderTable orderTable = orderTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        return buildOrderTableResponseDTO(orderTable);
    }

    @Override
    public OrderTableResponseDTO findByNumber(Integer number) {
        OrderTable orderTable = orderTableRepository.findByNumber(number)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con número: " + number));
        return buildOrderTableResponseDTO(orderTable);
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

        OrderTable updatedOrderTable = orderTableRepository.save(orderTable);
        return buildOrderTableResponseDTO(updatedOrderTable);
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
        OrderTable updatedOrderTable = orderTableRepository.save(orderTable);
        return buildOrderTableResponseDTO(updatedOrderTable);
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
        return orderTables.stream()
                .map(this::buildOrderTableResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNumber(Integer number) {
        return orderTableRepository.existsByNumber(number);
    }

    /**
     * Construye el DTO de respuesta con información adicional.
     * Este método:
     * 1. Mapea la entidad OrderTable a OrderTableResponseDTO
     * 2. Cuenta las órdenes activas (no facturadas) asociadas a esta mesa
     * 3. Agrega el conteo al DTO para mostrar información útil en el frontend
     * Nota: La mesa solo almacena número, capacidad y estado (FREE/OCCUPIED/RESERVED).
     * El estado se gestiona automáticamente:
     * - FREE → OCCUPIED cuando se crea una orden en esa mesa
     * - OCCUPIED → FREE cuando se facturan todas las órdenes de esa mesa
     */
    private OrderTableResponseDTO buildOrderTableResponseDTO(OrderTable orderTable) {
        int activeOrdersCount;
        
        try {
            // Contar órdenes activas (no facturadas) en esta mesa
            // Usamos findByTableNumberAndStatusNot para obtener todas las órdenes
            // que NO están en estado BILLED (facturadas)
            List<com.app.projectbar.domain.Order> allOrders = orderRepository.findByTableNumber(
                    orderTable.getNumber()
            );
            
            // Filtrar solo las órdenes que tienen estados válidos y no están facturadas
            activeOrdersCount = (int) allOrders.stream()
                    .filter(order -> {
                        try {
                            // Verificar que el estado sea válido y no sea BILLED
                            return order.getStatus() != null 
                                    && order.getStatus() != OrderStatus.BILLED
                                    && order.getStatus() != OrderStatus.CANCELLED;
                        } catch (Exception e) {
                            // Si hay un error al acceder al status (orden con estado inválido),
                            // la ignoramos
                            return false;
                        }
                    })
                    .count();
        } catch (Exception e) {
            // Si hay un error al cargar órdenes (por ejemplo, estados inválidos en BD como 'PENDING'),
            // simplemente retornamos 0 y continuamos
            // Esto puede pasar si hay órdenes antiguas con status 'PENDING' en la BD
            // que no pueden ser mapeadas al enum OrderStatus actual
            activeOrdersCount = 0;
        }

        OrderTableResponseDTO dto = modelMapper.map(orderTable, OrderTableResponseDTO.class);
        dto.setActiveOrdersCount(activeOrdersCount);
        return dto;
    }
}
