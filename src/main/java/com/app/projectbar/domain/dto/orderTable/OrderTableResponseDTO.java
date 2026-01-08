package com.app.projectbar.domain.dto.orderTable;

import com.app.projectbar.domain.enums.OrderTableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTableResponseDTO {

    private Long id;
    private Integer number;
    private Integer capacity;
    private OrderTableStatus status;
    private String notes;
    private Integer activeOrdersCount; // Número de órdenes activas en esta mesa
}
