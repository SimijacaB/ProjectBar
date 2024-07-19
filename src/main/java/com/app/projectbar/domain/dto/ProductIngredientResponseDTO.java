package com.app.projectbar.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductIngredientResponseDTO {

    private Long ingredient_id;
    private String ingredientName;
    private Double amount;
    private String ingredientExtend;

}
