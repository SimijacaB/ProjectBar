package com.app.projectbar.infra.controller;

import com.app.projectbar.application.interfaces.IProductService;
import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.dto.product.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.product.ProductRequestDTO;
import com.app.projectbar.domain.dto.product.ProductResponseDTO;
import com.app.projectbar.domain.dto.product.UpdateProductRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/products")
@RequiredArgsConstructor
@RestController
public class ProductController {

    private final IProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProductResponseDTO> save(@RequestBody @Valid ProductRequestDTO productRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.save(productRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id,
            @RequestBody @Valid UpdateProductRequestDTO productToUpdate) {
        return ResponseEntity.ok(productService.update(productToUpdate));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductForListResponseDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/code/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(productService.findByCode(code));
    }

    @GetMapping("/name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseDTO> findByNameExact(@PathVariable String name) {
        return ResponseEntity.ok(productService.findByNameExact(name));
    }

    @GetMapping("/category/{category}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductForListResponseDTO>> findByCategory(@PathVariable String category) {
        Category categoryEnum = Category.valueOf(category.toUpperCase());
        return ResponseEntity.ok(productService.findByCategory(categoryEnum));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ProductForListResponseDTO>> findByNameContaining(@PathVariable String name) {
        return ResponseEntity.ok(productService.findByNameContaining(name));
    }

    @DeleteMapping("/code/{code}")
    public void delete(@PathVariable String code) {
        productService.delete(code);
    }
}
