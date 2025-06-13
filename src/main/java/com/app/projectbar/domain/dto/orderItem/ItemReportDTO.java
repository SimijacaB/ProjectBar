package com.app.projectbar.domain.dto.orderItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemReportDTO {

    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}
