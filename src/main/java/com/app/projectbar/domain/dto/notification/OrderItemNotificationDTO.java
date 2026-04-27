package com.app.projectbar.domain.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemNotificationDTO {
    private Long id;
    private String productName;
    private Integer quantity;
    private Double price;
}