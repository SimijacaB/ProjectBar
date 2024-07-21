package com.app.projectbar.infra.controller;

import com.app.projectbar.application.IIngredientService;
import com.app.projectbar.domain.dto.IngredientRequestDTO;
import com.app.projectbar.domain.dto.IngredientResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredient")
@RequiredArgsConstructor
public class IngredientController {

    private final IIngredientService ingredientService;

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
    public void delete(@PathVariable String code) {
        ingredientService.delete(code);
    }
}
