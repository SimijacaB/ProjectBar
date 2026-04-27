package com.app.projectbar.domain.dto.orderTable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableRequestDTO {

    @NotNull(message = "El número de mesa es requerido")
    @Min(value = 1, message = "El número de mesa debe ser mayor a 0")
    private Integer number;

    @NotNull(message = "La capacidad es requerida")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacity;

    /**
     * Ubicación de la mesa dentro del bar (ej. "terraza", "patio", "comedor", "barra").
     */
    private String location;

    private String notes;
}
