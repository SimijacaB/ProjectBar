package com.app.projectbar.domain.dto.ingredient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredientResponseDTO {

    private Long id;

    private String code;

    private String name;

    private String unitOfMeasure;


}
