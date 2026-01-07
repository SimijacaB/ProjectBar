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

    /**
     * Asigna un mesero a una orden en estado CREATED.
     * Solo puede ser ejecutado por ADMIN.
     * Cambia el estado de CREATED a ASSIGNED.
     * 
     * @param orderId ID de la orden
     * @param waiterUsername Username del mesero a asignar
     * @return Orden actualizada
     * @throws RuntimeException si la orden no está en estado CREATED
     */
    OrderResponseDTO assignWaiter(Long orderId, String waiterUsername);

    /**
     * Obtiene todas las órdenes en estado CREATED (pendientes de asignación).
     * Solo para uso de ADMIN.
     * 
     * @return Lista de órdenes sin mesero asignado
     */
    List<OrderForListResponseDTO> findUnassignedOrders();

    /**
     * Obtiene las órdenes asignadas al mesero autenticado que están en estado ASSIGNED.
     * Estas son las órdenes que el mesero puede empezar a atender.
     * 
     * @return Lista de órdenes asignadas pendientes de atención
     */
    List<OrderForListResponseDTO> findMyAssignedOrders();

    void setOrdersAsReady(List<Order> orders);

    void validateIfOrderCanBeBilled(List<Order> orders);

    List<Order> getExistingOrdersOrThrow(List<Long> orderIds);


}
