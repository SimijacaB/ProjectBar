package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.domain.dto.ingredient.UpdateIngredientDTO;

import java.util.List;

public interface IIngredientService {

    IngredientResponseDTO findById(Long id);

    IngredientResponseDTO findByCode(String code);

    List<IngredientResponseDTO> findAll();

    IngredientResponseDTO save(IngredientRequestDTO ingredientRequest);

    IngredientResponseDTO update(UpdateIngredientDTO updateIngredient);

    void delete(String code);




}
