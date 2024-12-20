package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IIngredientService;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IIngredientService ingredientService;

    //
    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ingredientService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<IngredientResponseDTO>> findAll() {
        return ResponseEntity.ok(ingredientService.findAll());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<IngredientResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(ingredientService.findByCode(code));
    }

    @PostMapping("/save")
    public ResponseEntity<IngredientResponseDTO> save(@RequestBody @Valid IngredientRequestDTO ingredientRequest) {
        return ResponseEntity.ok(ingredientService.save(ingredientRequest));
    }
    @DeleteMapping("/delete/{code}")
    @Transactional
    public void delete(@PathVariable String code) {
        ingredientService.delete(code);
    }
}
