package com.app.projectbar.domain.dto.order;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateOrderDTO {
    @NotNull(message = "Can´t be null")
    private Long id;

    @NotBlank(message = "Can´t be blank")
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$", message = "Can´t contains numbers and special characters")
    @Size(min = 4, message = "Must have at least 4 characters")
    private String clientName;

    @NotNull(message = "Can´t be null")
    @Min(value = 1, message = "Must have only numbers")
    private Integer tableNumber;

    private String notes;
}
