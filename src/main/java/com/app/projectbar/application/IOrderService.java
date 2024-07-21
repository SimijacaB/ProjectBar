package com.app.projectbar.application;


import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    OrderResponseDTO save(OrderRequestDTO orderRequest);

    List<OrderResponseDTO> findByClientName(String name);

    List<OrderResponseDTO> findByTableNumber(Integer tableNumber);

    List<OrderResponseDTO> findByIdWaiter(Long id);

    List<OrderResponseDTO> findByDate(LocalDate date);

    List<OrderResponseDTO> findByStatus(Status status);



}
