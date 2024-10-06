package com.app.projectbar.domain.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryResponseDTO {

   private String name;

    private String code;

    private Integer quantity;


}
