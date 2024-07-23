package com.app.projectbar.application;

import com.app.projectbar.domain.dto.OrderItemRequestDTO;
import com.app.projectbar.domain.dto.OrderItemResponseDTO;

public interface IOrderItemService {

    OrderItemResponseDTO save(OrderItemRequestDTO orderItemRequestDTO);

}
