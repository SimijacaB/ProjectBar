package com.app.projectbar.application.interfaces;


import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;

import java.time.LocalDate;
import java.util.List;

public interface IOrderService {

    OrderResponseDTO save(OrderRequestDTO orderRequest);

    List<OrderForListResponseDTO> findAll();

    OrderResponseDTO findById(Long id);
    OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO);

    void deleteOrder(Long id);

    List<OrderForListResponseDTO> findByClientName(String name);

    List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber);

    List<OrderForListResponseDTO> findByWaiterId(String id);

    List<OrderForListResponseDTO> findByDate(LocalDate date);

    List<OrderForListResponseDTO> findByStatus(OrderStatus status);

    OrderResponseDTO addOrderItem(Long id, OrderItemRequestDTO orderItemToAdd);

    OrderResponseDTO removeOrderItem(Long id, Long idOrderItem, Integer quantityToRemove);

    OrderResponseDTO changeStatus(Long id, String newStatus);

    void setOrdersAsReady(List<Order> orders);

    void validateIfOrderCanBeBilled(List<Order> orders);

    List<Order> getExistingOrdersOrThrow(List<Long> orderIds);


}
