package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IIngredientService;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.domain.dto.ingredient.UpdateIngredientDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IIngredientService ingredientService;

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<IngredientResponseDTO>> findAll() {
        return ResponseEntity.ok(ingredientService.findAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<IngredientResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ingredientService.findByCode(code));
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<IngredientResponseDTO> save(@RequestBody @Valid IngredientRequestDTO ingredientRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.save(ingredientRequest));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<IngredientResponseDTO> update(@PathVariable Long id,
            @RequestBody @Valid UpdateIngredientDTO updateIngredient) {
        return ResponseEntity.ok(ingredientService.update(updateIngredient));
    }

    @DeleteMapping("/code/{code}")
    @Transactional
    public void delete(@PathVariable String code) {
        ingredientService.delete(code);
    }
}
