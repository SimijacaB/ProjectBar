package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;

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
    OrderRequestDTO orderRequestDTO;
    OrderResponseDTO orderResponseDTO;
    Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void testSave() {


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