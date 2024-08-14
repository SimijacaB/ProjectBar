package com.app.projectbar.infra.controller;

import com.app.projectbar.application.IInventoryService;
import com.app.projectbar.domain.dto.InventoryDTO;
import com.app.projectbar.domain.dto.InventoryResponseDTO;
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

    @PostMapping("/save")
    @Transactional
    public ResponseEntity<InventoryResponseDTO> save(@RequestBody @Valid InventoryDTO inventoryDTO){
        return ResponseEntity.ok(inventoryService.save(inventoryDTO));
    }

    @PutMapping("/add-stock/{quantity}/{code}")
    @Transactional
    public ResponseEntity<InventoryResponseDTO> addStock(@PathVariable @Valid Integer quantity,@PathVariable @Valid String code){
        return ResponseEntity.ok(inventoryService.addStock(quantity, code));
    }

    @PutMapping("/deduct-stock/{quantity}/{code}")
    @Transactional
    public ResponseEntity<InventoryResponseDTO> deductStock(@PathVariable @Valid Integer quantity,@PathVariable @Valid String code){
        return ResponseEntity.ok(inventoryService.deductStock(quantity, code));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<InventoryResponseDTO>> findAll(){
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/find-by-code/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InventoryResponseDTO> findByCode(@PathVariable String code){
        return ResponseEntity.ok(inventoryService.findByCode(code));
    }

    @DeleteMapping("/delete/{code}")
    @Transactional
    public void delete(@PathVariable String code){
        inventoryService.deleteByCode(code);
    }


}
