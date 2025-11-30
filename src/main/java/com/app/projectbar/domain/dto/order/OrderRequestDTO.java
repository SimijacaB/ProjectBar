package com.app.projectbar.domain.dto.order;

import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
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
    @Min(value = 1, message = "Must be a positive number")
    private Integer tableNumber;

    private String notes;

    @JsonAlias("products")
    private List<OrderItemRequestDTO> orderProducts;
}
