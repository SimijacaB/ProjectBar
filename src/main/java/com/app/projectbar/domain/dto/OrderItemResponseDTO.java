package com.app.projectbar.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemResponseDTO {

    private Long id;
    private String productName;
    private Integer quantity;

}
