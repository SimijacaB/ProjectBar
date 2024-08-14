package com.app.projectbar.domain.dto.orderItem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemRequestDTO {

    @NotBlank
    private String productName;

    @NotNull
    private Integer quantity;

}
