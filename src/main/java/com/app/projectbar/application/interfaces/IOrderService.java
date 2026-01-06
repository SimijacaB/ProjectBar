package com.app.projectbar.application.interfaces;


import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IOrderService {

    OrderResponseDTO save(OrderRequestDTO orderRequest);

    List<OrderForListResponseDTO> findAll();

    OrderResponseDTO findById(Long id);
    OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO);

    void deleteOrder(Long id);

    List<OrderForListResponseDTO> findByClientName(String name);

    List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber);

    List<OrderForListResponseDTO> findByWaiterId(String id);

    /**
     * Obtiene las órdenes del mesero autenticado actualmente.
     * No requiere pasar el ID, lo obtiene del contexto de seguridad.
     * @return Lista de órdenes del mesero logueado
     * @throws RuntimeException si el usuario no está autenticado
     */
    List<OrderForListResponseDTO> findMyOrders();

    /**
     * Obtiene las órdenes del mesero autenticado filtradas por rango de fechas.
     * @param startDate Fecha de inicio (inicio del día)
     * @param endDate Fecha de fin (fin del día)
     * @return Lista de órdenes del mesero en el rango de fechas
     */
    List<OrderForListResponseDTO> findMyOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderForListResponseDTO> findByDate(LocalDate date);

    /**
     * Obtiene órdenes filtradas por rango de fechas.
     * @param startDate Fecha/hora de inicio
     * @param endDate Fecha/hora de fin
     * @return Lista de órdenes en el rango
     */
    List<OrderForListResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<OrderForListResponseDTO> findByStatus(OrderStatus status);

    public Map<String, List<OrderForListResponseDTO>> findPendingOrdersByTableGroupedByClient(Integer tableNumber);

    OrderResponseDTO addOrderItem(Long id, OrderItemRequestDTO orderItemToAdd);

    OrderResponseDTO removeOrderItem(Long id, Long idOrderItem, Integer quantityToRemove);

    OrderResponseDTO changeStatus(Long id, String newStatus);

    void setOrdersAsReady(List<Order> orders);

    void validateIfOrderCanBeBilled(List<Order> orders);

    List<Order> getExistingOrdersOrThrow(List<Long> orderIds);


}
