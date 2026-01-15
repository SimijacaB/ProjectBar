package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "waiterUserName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "bill", ignore = true)
    @Mapping(target = "valueToPay", ignore = true)
    Order toEntity(OrderRequestDTO dto);

    @Mapping(target = "orderItemList", source = "orderItems")
    OrderResponseDTO toResponseDTO(Order order);

    OrderForListResponseDTO toListDTO(Order order);

    List<OrderForListResponseDTO> toListDTOList(List<Order> orders);
}

