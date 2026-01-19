package com.app.projectbar.domain.dto.waiter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaiterWithOrdersDTO {
    
    private String username;
    private String email;
    private int activeOrdersCount;
    private boolean isActive; // Indica si el mesero está habilitado
    
}
