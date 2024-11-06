package com.app.projectbar.domain.dto.order;

import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$", message = "Can´t contains numbers and special characters")
    @Size(min = 4, message = "Must have at least 4 characters")
    private String clientName;

    @NotNull
    @Pattern(regexp = "^\\d+$", message = "Must have only numbers")
    private Integer tableNumber;

    private String notes;

    private List<OrderItemRequestDTO> orderProducts;
}
