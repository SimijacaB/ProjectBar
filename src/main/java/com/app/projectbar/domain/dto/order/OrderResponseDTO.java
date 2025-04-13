package com.app.projectbar.domain.dto.order;


import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.orderItem.OrderItemResponseDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseDTO {

    private Long id;

    private String clientName;

    private Integer tableNumber;

    private String waiterUserName;

    private String notes;

    private OrderStatus status;

    private LocalDateTime date;

    private List<OrderItemResponseDTO> orderItemList;

    private Double valueToPay;

}
