package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;

public interface IOrderItemService {

    OrderItemResponseDTO save(OrderItemRequestDTO orderItemRequestDTO);

}
