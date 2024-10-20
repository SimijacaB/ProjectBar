package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
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
import java.util.List;
import java.util.Arrays;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    private OrderRequestDTO orderRequest;
    private OrderResponseDTO orderResponse;
    private Order order;
    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        // Configuración común
        orderRequest = new OrderRequestDTO();
        orderRequest.setClientName("Santiago");
        orderRequest.setNotes("Cerveza fría");
        orderRequest.setTableNumber(3);

        order = new Order();
        order.setId(1L);
        order.setClientName(orderRequest.getClientName());
        order.setTableNumber(orderRequest.getTableNumber());
        order.setDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setNotes(orderRequest.getNotes());

        orderResponse = new OrderResponseDTO();
        orderResponse.setId(1L);
        orderResponse.setClientName("Santiago");
        orderResponse.setDate(LocalDateTime.now());
        orderResponse.setNotes("Cerveza fría");
        orderResponse.setTableNumber(3);

        when(modelMapper.map(orderRequest, Order.class)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(orderResponse);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    }

    @Test
    void testSave(){
        //  ------- WHEN --------
        OrderResponseDTO result = orderService.save(orderRequest);

        // --------- THEN ------------
        assertEquals("Santiago", result.getClientName());
        assertEquals(3, result.getTableNumber());
    }

    @Test
    void findAll() {
        // Configuración específica para findAll
        Order order1 = new Order();
        order1.setId(1L);
        order1.setClientName("Client1");
        order1.setTableNumber(1);
        order1.setDate(LocalDateTime.now());
        order1.setStatus(OrderStatus.PENDING);
        order1.setNotes("Notes1");

        Order order2 = new Order();
        order2.setId(2L);
        order2.setClientName("Client2");
        order2.setTableNumber(2);
        order2.setDate(LocalDateTime.now());
        order2.setStatus(OrderStatus.PENDING);
        order2.setNotes("Notes2");

        List<Order> orders = Arrays.asList(order1, order2);

        OrderForListResponseDTO orderResponse1 = new OrderForListResponseDTO();
        orderResponse1.setId(1L);
        orderResponse1.setClientName("Client1");
        orderResponse1.setTableNumber(1);
        orderResponse1.setDate(LocalDateTime.now());
        orderResponse1.setStatus(OrderStatus.PENDING);
        orderResponse1.setNotes("Notes1");

        OrderForListResponseDTO orderResponse2 = new OrderForListResponseDTO();
        orderResponse2.setId(2L);
        orderResponse2.setClientName("Client2");
        orderResponse2.setTableNumber(2);
        orderResponse2.setDate(LocalDateTime.now());
        orderResponse2.setStatus(OrderStatus.PENDING);
        orderResponse2.setNotes("Notes2");

        List<OrderForListResponseDTO> orderResponses = Arrays.asList(orderResponse1, orderResponse2);

        when(orderRepository.findAll()).thenReturn(orders);
        when(modelMapper.map(order1, OrderForListResponseDTO.class)).thenReturn(orderResponse1);
        when(modelMapper.map(order2, OrderForListResponseDTO.class)).thenReturn(orderResponse2);
        when(this.orderRepository.findById(1L)).thenReturn(Optional.of(order1));

        //  ------- WHEN --------
        List<OrderForListResponseDTO> result = orderService.findAll();

        // --------- THEN ------------
        assertEquals(2, result.size());
        assertEquals(orderResponse1, result.get(0));
        assertEquals(orderResponse2, result.get(1));
        assertEquals(orderResponses, result);
    }



    @Test
    void findById() {
        // ------- GIVEN ------
        Long id = 1L;

        // ------- WHEN --------
        OrderResponseDTO result = orderService.findById(id);

        // ------- THEN --------
        assertEquals(id, result.getId());
        assertEquals("Santiago", result.getClientName());
        assertEquals("Cerveza fría", result.getNotes());
        assertEquals(3, result.getTableNumber());

    }

    @Test
    void testFindByIdException(){
        // ----- GIVEN-----
        Long id = 8L;

        // ----- THEN ------
        RuntimeException exception = assertThrows(RuntimeException.class , () ->
                orderService.findById(id));

        assertEquals("Order with id " + id + " not found", exception.getMessage());
        verify(orderRepository).findById(id);

    }
    @Test
    void testUpdateOrder() {

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