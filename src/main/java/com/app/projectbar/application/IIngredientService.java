package com.app.projectbar.application;

import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;

import java.util.List;

public interface IIngredientService {

    IngredientResponseDTO findById(Long id);

    IngredientResponseDTO findByCode(String code);

    List<IngredientResponseDTO> findAll();

    IngredientResponseDTO save(IngredientRequestDTO ingredientRequest);

    void delete(String code);




}
