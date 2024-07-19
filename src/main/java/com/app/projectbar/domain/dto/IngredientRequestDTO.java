package com.app.projectbar.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredientRequestDTO {

    private String code;
    private String name;
    private String unitOfMeasure;


}
