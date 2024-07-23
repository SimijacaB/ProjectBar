package com.app.projectbar.domain.dto;

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
public class UpdateOrderDTO {
    @NotNull
    private Long id;

    @NotBlank
    private String clientName;

    @NotNull
    private Integer tableNumber;

    private String notes;
}
