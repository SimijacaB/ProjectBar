package com.app.projectbar.application;


import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.OrderStatus;
import com.app.projectbar.domain.dto.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.OrderItemResponseDTO;
import com.app.projectbar.domain.dto.OrderRequestDTO;
import com.app.projectbar.domain.dto.OrderResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    OrderResponseDTO save(OrderRequestDTO orderRequest);

    List<OrderResponseDTO> findByClientName(String name);

    List<OrderResponseDTO> findByTableNumber(Integer tableNumber);

    List<OrderResponseDTO> findByIdWaiter(Long id);

    List<OrderResponseDTO> findByDate(LocalDate date);

    List<OrderResponseDTO> findByStatus(OrderStatus status);

    OrderItemResponseDTO addOrderItem(OrderItemRequestDTO orderItemToAdd);

    void removeOrderItem(Long idOrderItem);

    OrderResponseDTO changeStatus(String newStatus);

    Double calculateValueToPay(OrderRequestDTO orderRequestDTO);


}
