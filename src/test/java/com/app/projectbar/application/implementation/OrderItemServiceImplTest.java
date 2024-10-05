package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;
import com.app.projectbar.infra.repositories.IOrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderItemServiceImplTest {

    @Mock
    private IOrderItemRepository orderItemRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItemRequestDTO itemRequestDTO;
    private OrderItem orderItem;
    private OrderItemResponseDTO itemResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        Long id = 1L;
        itemRequestDTO = new OrderItemRequestDTO();
        itemRequestDTO.setProductName("Margarita");
        itemRequestDTO.setQuantity(8);

        orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setProductName("Margarita");
        orderItem.setQuantity(8);

        itemResponseDTO = new OrderItemResponseDTO();
        itemResponseDTO.setId(id);
        itemResponseDTO.setProductName("Margarita");
        itemResponseDTO.setQuantity(8);
    }

    @Test
    void save_ShouldReturnSavedOrderItem() {
        // Given
        when(modelMapper.map(itemRequestDTO, OrderItem.class)).thenReturn(orderItem);
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);
        when(modelMapper.map(orderItem, OrderItemResponseDTO.class)).thenReturn(itemResponseDTO);

        // When
        OrderItemResponseDTO result = orderItemService.save(itemRequestDTO);

        // Then
        assertNotNull(result);
        assertEquals("Margarita", result.getProductName());
        assertEquals(8, result.getQuantity());

        // Verify interactions with mocks or dependencies
        verify(modelMapper).map(itemRequestDTO, OrderItem.class);
        verify(orderItemRepository).save(orderItem);
        verify(modelMapper).map(orderItem, OrderItemResponseDTO.class);
    }
}