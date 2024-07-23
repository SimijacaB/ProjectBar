package com.app.projectbar.application.impl;

import com.app.projectbar.application.IOrderService;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.OrderStatus;
import com.app.projectbar.domain.dto.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.OrderItemResponseDTO;
import com.app.projectbar.domain.dto.OrderRequestDTO;
import com.app.projectbar.domain.dto.OrderResponseDTO;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IOrderItemRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final IProductRepository productRepository;
    private final IInventoryRepository inventoryRepository;


    @Override
    public OrderResponseDTO save(OrderRequestDTO orderRequest) {
        var order = new Order();

        order.setClientName(orderRequest.getClientName());
        order.setTableNumber(orderRequest.getTableNumber());
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(orderRequest.getNotes());

        orderRepository.save(order);

        List <OrderItemRequestDTO> orderProducts = orderRequest.getOrderProducts();

        order.setOrderProducts(orderProducts.stream().map(orderProduct -> modelMapper.map(orderProduct, OrderItem.class)).toList());

        order.getOrderProducts().forEach(orderItem -> {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        });
    }

    @Override
    public List<OrderResponseDTO> findByClientName(String name) {
        return null;
    }

    @Override
    public List<OrderResponseDTO> findByTableNumber(Integer tableNumber) {
        return null;
    }

    @Override
    public List<OrderResponseDTO> findByIdWaiter(Long id) {
        return null;
    }

    @Override
    public List<OrderResponseDTO> findByDate(LocalDate date) {
        return null;
    }

    @Override
    public List<OrderResponseDTO> findByStatus(OrderStatus status) {
        return null;
    }

    @Override
    public OrderItemResponseDTO addOrderItem(OrderItemRequestDTO orderItemToAdd) {
        return null;
    }

    @Override
    public void removeOrderItem(Long idOrderItem) {

    }

    @Override
    public OrderResponseDTO changeStatus(String newStatus) {
        return null;
    }

    @Override
    public Double calculateValueToPay(OrderRequestDTO orderRequestDTO) {
        return null;
    }
}
