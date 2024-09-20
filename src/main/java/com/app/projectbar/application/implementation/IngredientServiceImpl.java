package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IIngredientService;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IIngredientService {

    private final IIngredientRepository ingredientRepository;
    private final ModelMapper modelMapper;
    @Override
    public IngredientResponseDTO findById(Long id) {
        return modelMapper.map(ingredientRepository.findById(id).orElseThrow(() -> new RuntimeException("Ingredient with id " + id + " not found")), IngredientResponseDTO.class);
    }

    @Override
    public IngredientResponseDTO findByCode(String code) {
        return modelMapper.map(ingredientRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Ingredient with code " + code + " not found")), IngredientResponseDTO.class);
    }

    @Override
    public List<IngredientResponseDTO> findAll() {
        return ingredientRepository.findAll().stream().map(ingredient -> modelMapper.map(ingredient, IngredientResponseDTO.class)).toList();
    }

    @Override
    public IngredientResponseDTO save(IngredientRequestDTO ingredientRequest) {
        var ingredient = ingredientRepository.save(modelMapper.map(ingredientRequest, Ingredient.class));
        return modelMapper.map(ingredient, IngredientResponseDTO.class);
    }

    @Override
    public void delete(String code) {
        ingredientRepository.deleteByCode(code);
    }
}
