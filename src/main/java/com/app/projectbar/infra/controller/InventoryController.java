package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final IInventoryService inventoryService;

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InventoryResponseDTO> save(@RequestBody @Valid InventoryDTO inventoryDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.save(inventoryDTO));
    }

    @PatchMapping("/{code}/add-stock")
    @Transactional
    public ResponseEntity<InventoryResponseDTO> addStock(@PathVariable String code,
            @RequestParam @Valid Integer quantity) {
        return ResponseEntity.ok(inventoryService.addStock(quantity, code));
    }

    @PatchMapping("/{code}/deduct-stock")
    @Transactional
    public ResponseEntity<InventoryResponseDTO> deductStock(@PathVariable String code,
            @RequestParam @Valid Integer quantity) {
        return ResponseEntity.ok(inventoryService.deductStock(quantity, code));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<InventoryResponseDTO>> findAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InventoryResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(inventoryService.findByCode(code));
    }

    @DeleteMapping("/{code}")
    @Transactional
    public void delete(@PathVariable String code) {
        inventoryService.deleteByCode(code);
    }

}
