package com.app.projectbar.application;


import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.OrderStatus;
import com.app.projectbar.domain.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    OrderResponseDTO save(OrderRequestDTO orderRequest);

    List<OrderForListResponseDTO> findAll();

    OrderResponseDTO findById(Long id);
    OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO);

    void deleteOrder(Long id);

    List<OrderForListResponseDTO> findByClientName(String name);

    List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber);

    List<OrderForListResponseDTO> findByIdWaiter(Long id);

    List<OrderForListResponseDTO> findByDate(LocalDate date);

    List<OrderForListResponseDTO> findByStatus(OrderStatus status);

    OrderResponseDTO addOrderItem(Long id, OrderItemRequestDTO orderItemToAdd);

    OrderResponseDTO removeOrderItem(Long id, Long idOrderItem);

    OrderResponseDTO changeStatus(Long id, String newStatus);

   // Double calculateValueToPay(OrderRequestDTO orderRequestDTO);


}
