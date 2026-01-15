package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IIngredientService;
import com.app.projectbar.application.mapper.IngredientMapper;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.domain.dto.ingredient.UpdateIngredientDTO;
import com.app.projectbar.domain.enums.UnitOfMeasure;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IIngredientService {

    private final IIngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;
    @Override
    public IngredientResponseDTO findById(Long id) {
        return ingredientMapper.toResponseDTO(ingredientRepository.findById(id).orElseThrow(() -> new RuntimeException("Ingredient with id " + id + " not found")));
    }

    @Override
    public IngredientResponseDTO findByCode(String code) {
        return ingredientMapper.toResponseDTO(ingredientRepository.findByCode(code).orElseThrow(() -> new RuntimeException("Ingredient with code " + code + " not found")));
    }

    @Override
    public List<IngredientResponseDTO> findAll() {
        return ingredientMapper.toResponseDTOList(ingredientRepository.findAll());
    }

    @Override
    public IngredientResponseDTO save(IngredientRequestDTO ingredientRequest) {
        var ingredient = ingredientRepository.save(ingredientMapper.toEntity(ingredientRequest));
        return ingredientMapper.toResponseDTO(ingredient);
    }


    @Override
    public IngredientResponseDTO update(UpdateIngredientDTO updateIngredient) {
        if (updateIngredient.getId() == null) {
            throw new RuntimeException("Ingredient id is required to update");
        }
        var existing = ingredientRepository.findByCode(updateIngredient.getCode());
        if (existing.isPresent() && !existing.get().getId().equals(updateIngredient.getId())) {
            throw new RuntimeException("Another ingredient with code " + updateIngredient.getCode() + " already exists");
        }
        var ingredient = ingredientRepository.findById(updateIngredient.getId())
                .orElseThrow(() -> new RuntimeException("Ingredient with id " + updateIngredient.getId() + " not found"));
        ingredient.setCode(updateIngredient.getCode());
        ingredient.setName(updateIngredient.getName());
        ingredient.setUnitOfMeasure(UnitOfMeasure.valueOf(updateIngredient.getUnitOfMeasure()));
        return ingredientMapper.toResponseDTO(ingredientRepository.save(ingredient));
    }

    @Override
    public void delete(String code) {
        ingredientRepository.deleteByCode(code);
    }
}

