package com.app.projectbar.application.impl;

import com.app.projectbar.application.IOrderService;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.OrderStatus;
import com.app.projectbar.domain.dto.*;
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
import java.util.Optional;

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

        List<OrderItemRequestDTO> orderProducts = orderRequest.getOrderProducts();

        order.setOrderProducts(orderProducts.stream().map(orderProduct -> modelMapper.map(orderProduct, OrderItem.class)).toList());

        order.getOrderProducts().forEach(orderItem -> {
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        });
        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public List<OrderForListResponseDTO> findAll() {
        var orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public OrderResponseDTO findById(Long id) {
        return modelMapper.map(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order with id " + id + " not found")), OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO) {
        var order = orderRepository.findById(updateOrderDTO.getId())
                .orElseThrow(() -> new RuntimeException("Order with id " + updateOrderDTO.getId() + " not found"));

        order.setClientName(updateOrderDTO.getClientName());
        order.setTableNumber(updateOrderDTO.getTableNumber());
        order.setNotes(updateOrderDTO.getNotes());

        orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderForListResponseDTO> findByClientName(String name) {
        List<Order> orders = orderRepository.findByClientName(name);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber) {
        List<Order> orders = orderRepository.findByTableNumber(tableNumber);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByIdWaiter(Long id) {
        List<Order> orders = orderRepository.findByIdWaiter(id);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {
        return null;
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public OrderResponseDTO addOrderItem(Long id, OrderItemRequestDTO orderItemToAdd) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if(orderOptional.isEmpty()){
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();
        order.getOrderProducts().add(modelMapper.map(orderItemToAdd, OrderItem.class));
        

        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO removeOrderItem(Long id, Long idOrderItem) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if(orderOptional.isEmpty()){
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();
        order.getOrderProducts().remove(modelMapper.map(idOrderItem, OrderItem.class));

        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);

    }

    @Override
    public OrderResponseDTO changeStatus(Long id, String newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if(orderOptional.isEmpty()){
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();
        order.setStatus(OrderStatus.valueOf(newStatus));


        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);
    }

//    @Override
//    public Double calculateValueToPay(OrderRequestDTO orderRequestDTO) {
//        return null;
//    }
}
