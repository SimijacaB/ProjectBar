package com.app.projectbar.application;

import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;

public interface IOrderItemService {

    OrderItemResponseDTO save(OrderItemRequestDTO orderItemRequestDTO);

}
