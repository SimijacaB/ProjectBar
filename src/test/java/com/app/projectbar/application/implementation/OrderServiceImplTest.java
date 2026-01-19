package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.application.mapper.OrderMapper;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderTable;
import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.OrderTableStatus;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
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
    private  IOrderTableRepository orderTableRepository;
    @Mock
    private  OrderMapper orderMapper;
    @Mock
    private  IProductRepository productRepository;
    @Mock
    private  IInventoryService inventoryService;

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
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setNotes(orderRequest.getNotes());
        order.setOrderItems(new ArrayList<>()); // Initialize to prevent NPE

        order2 = new Order();
        order2.setId(2L);
        order2.setClientName("Yesika");
        order2.setTableNumber(2);
        order2.setDate(LocalDateTime.now());
        order2.setStatus(OrderStatus.IN_PROGRESS);
        order2.setNotes("Notes2");
        order2.setOrderItems(new ArrayList<>()); // Initialize to prevent NPE

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
        OrderListResponse.setStatus(OrderStatus.IN_PROGRESS);
        OrderListResponse.setNotes("Notes1");

        OrderListResponse2 = new OrderForListResponseDTO();
        OrderListResponse2.setId(2L);
        OrderListResponse2.setClientName("Client2");
        OrderListResponse2.setTableNumber(2);
        OrderListResponse2.setDate(LocalDateTime.now());
        OrderListResponse2.setStatus(OrderStatus.IN_PROGRESS);
        OrderListResponse2.setNotes("Notes2");

        orderResponses = Arrays.asList(OrderListResponse, OrderListResponse2);

        //FindAll
        when(orderRepository.findAll()).thenReturn(orders);

        //FindByTableNumber
        when(orderRepository.findByTableNumber(3)).thenReturn(orders);
        //FindByNameClient
        when(orderRepository.findByClientName("Santiago")).thenReturn(orders);

        //Mapea cada order a OrderForListResponseDTO
        when(orderMapper.toListDTO(order)).thenReturn(OrderListResponse);
        when(orderMapper.toListDTO(order2)).thenReturn(OrderListResponse2);
        when(orderMapper.toListDTOList(orders)).thenReturn(orderResponses);

        //NOTA: CREO QUE DEBEMOS HACER OTRO RESPONSE, EL CUAL VA A GENERAR DESPUÉS DE REALIZAR LA ACTUALIZACIÓN DE LA ORDEN, YA QUE ESTA DEVOLVIENDO EL RESPONSE DEL SAVE O EL GENERAL
        //VOLVER A PROBAR EL MÉTODO UPDATEORDER
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponseDTO(order)).thenReturn(orderResponse);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    }

    @Test
    void testSave() {
        // 1. Crear el DTO del producto/ítem que va dentro de la orden
        OrderItemRequestDTO itemRequest = OrderItemRequestDTO.builder()
                .idProduct(1L)
                .quantity(2)
                .productName("Margarita")
                .build();

        // 2. Configurar el OrderRequest con su lista de productos
        orderRequest = OrderRequestDTO.builder()
                .clientName("Santiago")
                .tableNumber(3)
                .orderProducts(List.of(itemRequest))
                .build();

        // 3. Crear mock de OrderTable
        OrderTable orderTable = OrderTable.builder()
                .id(1L)
                .number(3)
                .capacity(4)
                .status(OrderTableStatus.FREE)
                .build();

        // 4. Crear mock de Product
        Product product = Product.builder()
                .id(1L)
                .name("Margarita")
                .price(15.0)
                .code("MARG-001")
                .build();

        // 5. Configurar el OrderResponseDTO esperado
        OrderItemResponseDTO itemResponse = new OrderItemResponseDTO();
        itemResponse.setProductName("Margarita");
        itemResponse.setQuantity(2);
        itemResponse.setUnitPrice(15.0);

        OrderResponseDTO expectedResponse = new OrderResponseDTO();
        expectedResponse.setId(1L);
        expectedResponse.setClientName("Santiago");
        expectedResponse.setTableNumber(3);
        expectedResponse.setOrderItemList(List.of(itemResponse));

        // 6. Configurar mocks
        when(orderTableRepository.findByNumber(3)).thenReturn(Optional.of(orderTable));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(expectedResponse);
        // Mock para descontar inventario del producto sin ingredientes
        when(inventoryService.deductStock(any(Integer.class), any(String.class))).thenReturn(null);

        // ------- WHEN --------
        OrderResponseDTO result = orderService.save(orderRequest);

        // --------- THEN ------------
        assertNotNull(result);
        assertEquals("Santiago", result.getClientName());
        assertEquals(3, result.getTableNumber());
        assertNotNull(result.getOrderItemList());
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

        assertEquals("We haven't found an order with this id.", exception.getMessage());
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

        // Update the mock response to reflect the changes
        orderResponse.setNotes(updateOrderDTO.getNotes());

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

        assertEquals("We haven't found an order with this id.", exception.getMessage());
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
