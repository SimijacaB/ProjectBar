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
    private OrderForListResponseDTO OrderListResponse;
    private OrderForListResponseDTO OrderListResponse2;
    private List<OrderForListResponseDTO> orderResponses;
    private Order order;
    private Order order2;
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

        order2 = new Order();
        order2.setId(2L);
        order2.setClientName("Yesika");
        order2.setTableNumber(2);
        order2.setDate(LocalDateTime.now());
        order2.setStatus(OrderStatus.PENDING);
        order2.setNotes("Notes2");

        List<Order> orders = Arrays.asList(order, order2);

        orderResponse = new OrderResponseDTO();
        orderResponse.setId(1L);
        orderResponse.setClientName("Santiago");
        orderResponse.setDate(LocalDateTime.now());
        orderResponse.setNotes("Cerveza fría");
        orderResponse.setTableNumber(3);

        //Inicializamos OrderForListResponseDto para los métodos: findAll, findByClientName, findByTableNumber
        OrderListResponse = new OrderForListResponseDTO();
        OrderListResponse.setId(1L);
        OrderListResponse.setClientName("Client1");
        OrderListResponse.setTableNumber(1);
        OrderListResponse.setDate(LocalDateTime.now());
        OrderListResponse.setStatus(OrderStatus.PENDING);
        OrderListResponse.setNotes("Notes1");

        OrderListResponse2 = new OrderForListResponseDTO();
        OrderListResponse2.setId(2L);
        OrderListResponse2.setClientName("Client2");
        OrderListResponse2.setTableNumber(2);
        OrderListResponse2.setDate(LocalDateTime.now());
        OrderListResponse2.setStatus(OrderStatus.PENDING);
        OrderListResponse2.setNotes("Notes2");

        orderResponses = Arrays.asList(OrderListResponse, OrderListResponse2);

        //FindAll
        when(orderRepository.findAll()).thenReturn(orders);

        //FindByTableNumber
        when(orderRepository.findByTableNumber(3)).thenReturn(orders);
        //FindByNameClient
        when(orderRepository.findByClientName("Santiago")).thenReturn(orders);

        //Mapea cada order a OrderForListResponseDTO
        when(modelMapper.map(order, OrderForListResponseDTO.class)).thenReturn(OrderListResponse);
        when(modelMapper.map(order2, OrderForListResponseDTO.class)).thenReturn(OrderListResponse2);

        when(modelMapper.map(orderRequest, Order.class)).thenReturn(order);

        //NOTA: CREO QUE DEBEMOS HACER OTRO RESPONSE, EL CUAL VA A GENERAR DESPUÉS DE REALIZAR LA ACTUALIZACIÓN DE LA ORDEN, YA QUE ESTA DEVOLVIENDO EL RESPONSE DEL SAVE O EL GENERAL
        //VOLVER A PROBAR EL MÉTODO UPDATEORDER
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

        //  ------- WHEN --------
        List<OrderForListResponseDTO> result = orderService.findAll();

        // --------- THEN ------------
        assertEquals(2, result.size());
        assertEquals(OrderListResponse, result.get(0));
        assertEquals(OrderListResponse2, result.get(1));
        assertEquals(orderResponses, result);
        verify(orderRepository).findAll();

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
        verify(orderRepository).findById(id);


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
        // --- GIVEN ---
        Long id = 1L;
        UpdateOrderDTO updateOrderDTO = new UpdateOrderDTO();
        updateOrderDTO.setId(id);
        updateOrderDTO.setClientName("Santiago");
        updateOrderDTO.setNotes("Cerveza con hielo");
        updateOrderDTO.setTableNumber(3);

        // --- WHEN ---
        OrderResponseDTO result = orderService.updateOrder(updateOrderDTO);

        // --- THEN ---
        assertEquals(id, result.getId());
        assertEquals("Santiago", result.getClientName());
        assertEquals("Cerveza con hielo", result.getNotes());
        assertEquals(3, result.getTableNumber());
        verify(orderRepository).findById(id);
        verify(orderRepository).save(any(Order.class));

        //NOTA: CREO QUE DEBEMOS HACER OTRO RESPONSE, EL CUAL VA A GENERAR DESPUÉS DE REALIZAR LA ACTUALIZACIÓN DE LA ORDEN, YA QUE ESTA DEVOLVIENDO EL RESPONSE DEL SAVE O EL GENERAL
        //VOLVER A PROBAR EL MÉTODO UPDATEORDER. USAR NUEVAMENTE EL DEBUG PARA LOCALIZAR LOS ERRORES.
    }

    @Test
    void testUpdateOrderException() {
        // --- GIVEN ---
        UpdateOrderDTO updateOrderDTO = new UpdateOrderDTO();
        updateOrderDTO.setId(8L); // id inexistente

        // ---- THEN -----
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.updateOrder(updateOrderDTO);
        });

        assertEquals("Order with id " + updateOrderDTO.getId() + " not found", exception.getMessage());
        verify(orderRepository).findById(updateOrderDTO.getId());
    }

    @Test
    void testDeleteOrder() {
        // --- GIVEN ---
        Long id = 1L;

        // --- WHEN ---
        orderService.deleteOrder(id);

        // ---- THEN ----
        verify(orderRepository).deleteById(id);
    }

    @Test
    void findByClientName() {
        // --- GIVEN ---
        String clientName = "Santiago";

        // --- WHEN ---
        List<OrderForListResponseDTO> result = orderService.findByClientName(clientName);

        // --- THEN ---
        assertEquals(orderResponses, result);
        verify(orderRepository).findByClientName(clientName);


    }

    @Test
    void findByTableNumber() {
        // --- GIVEN ---
        Integer tableNum = 3;

        // --- WHEN ---
        List<OrderForListResponseDTO> result = orderService.findByTableNumber(tableNum);

        // --- THEN ---
        assertEquals(2, result.size());
        assertEquals(OrderListResponse, result.get(0));
        assertEquals(OrderListResponse2, result.get(1));
        assertEquals(orderResponses, result);
        verify(orderRepository).findByTableNumber(tableNum);

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