package com.app.projectbar.application.impl;

import com.app.projectbar.application.IOrderItemService;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.OrderItemResponseDTO;
import com.app.projectbar.infra.repositories.IOrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IOrderItemServiceImpl implements IOrderItemService {

    private final IOrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;

    @Override
    public OrderItemResponseDTO save(OrderItemRequestDTO orderItemRequestDTO) {

        var orderItem = new OrderItem();
        orderItem.setProductName(orderItemRequestDTO.getProductName());
        orderItem.setQuantity(orderItemRequestDTO.getQuantity());
        orderItem.

        return orderItemRepository.save(orderItem);
    }
}
