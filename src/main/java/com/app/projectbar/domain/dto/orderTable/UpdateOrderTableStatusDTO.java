package com.app.projectbar.domain.dto.orderTable;

import com.app.projectbar.domain.enums.OrderTableStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderTableStatusDTO {

    @NotNull(message = "El estado es requerido")
    private OrderTableStatus status;
}
