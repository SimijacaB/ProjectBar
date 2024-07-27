package com.app.projectbar.domain.dto;


import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.OrderStatus;
import com.app.projectbar.domain.Status;
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
public class OrderForListResponseDTO {

    private Long id;

    private String clientName;

    private Integer tableNumber;

    private String waiterUserName;

    private String notes;

    private OrderStatus status;

    private LocalDateTime date;

    private Double valueToPay;

}
