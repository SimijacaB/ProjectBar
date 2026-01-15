package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "price", ignore = true)
    OrderItem toEntity(OrderItemRequestDTO dto);

    @Mapping(target = "unitPrice", source = "price")
    @Mapping(target = "totalPrice", expression = "java(orderItem.getPrice() * orderItem.getQuantity())")
    OrderItemResponseDTO toResponseDTO(OrderItem orderItem);

    List<OrderItemResponseDTO> toResponseDTOList(List<OrderItem> orderItems);
}

