package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    Ingredient toEntity(IngredientRequestDTO dto);

    IngredientResponseDTO toResponseDTO(Ingredient ingredient);

    List<IngredientResponseDTO> toResponseDTOList(List<Ingredient> ingredients);
}


