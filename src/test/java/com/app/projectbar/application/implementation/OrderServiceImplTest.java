package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IOrderItemRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    @Mock
    private  IOrderRepository orderRepository;
    @Mock
    private  IOrderItemRepository orderItemRepository;
    @Mock
    private  ModelMapper modelMapper;
    @Mock
    private  IProductRepository productRepository;
    @Mock
    private  IInventoryRepository inventoryRepository;

    @InjectMocks
    private OrderServiceImpl orderService;
    OrderRequestDTO orderRequest;
    OrderResponseDTO orderResponse;
    Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave(){

        Long orderId = 1L;
        //  ----------- GIVEN -----------
        orderRequest = new OrderRequestDTO();
        orderRequest.setClientName("Santiago");
        orderRequest.setNotes("Cerveza fría");
        orderRequest.setTableNumber(3);

        order = new Order();
        order.setId(orderId);
        order.setClientName(orderRequest.getClientName());
        order.setTableNumber(orderRequest.getTableNumber());
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(orderRequest.getNotes());


        orderResponse = new OrderResponseDTO();
        orderResponse.setId(orderId);
        orderResponse.setClientName("Santiago");
        orderResponse.setDate(LocalDateTime.now());
        orderResponse.setNotes("Cervea fría");
        orderResponse.setTableNumber(3);


        when(modelMapper.map(orderRequest, Order.class)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(orderResponse);


        //  ------- WHEN --------
        OrderResponseDTO result = orderService.save(orderRequest);


        // --------- THEN ------------
        assertEquals("Santiago", result.getClientName());
        assertEquals(3, result.getTableNumber());




    }

    @Test
    void findAll() {
    }

    @Test
    void findById() {
    }

    @Test
    void updateOrder() {
    }

    @Test
    void deleteOrder() {
    }

    @Test
    void findByClientName() {
    }

    @Test
    void findByTableNumber() {
    }

    @Test
    void findByWaiterId() {
    }

    @Test
    void findByDate() {
    }

    @Test
    void findByStatus() {
    }

    @Test
    void addOrderItem() {
    }

    @Test
    void removeOrderItem() {
    }

    @Test
    void changeStatus() {
    }
}