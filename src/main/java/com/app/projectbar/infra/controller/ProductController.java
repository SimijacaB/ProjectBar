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

@RequestMapping("/api/product")
@RequiredArgsConstructor
@RestController
public class ProductController {

    private final IProductService productService;

    @PostMapping("/save")
    public ResponseEntity<ProductResponseDTO> save(@RequestBody @Valid ProductRequestDTO productRequest){
        return ResponseEntity.ok(productService.save(productRequest));
    }

    @PutMapping("/update")
    public ResponseEntity<ProductResponseDTO> update(@RequestBody @Valid UpdateProductRequestDTO productToUpdate){
        return ResponseEntity.ok(productService.update(productToUpdate));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductForListResponseDTO>> findAll(){
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/find-by-id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id){
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/find-by-code/{code}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ProductResponseDTO> findByCode(@PathVariable String code){
        return ResponseEntity.ok(productService.findByCode(code));
    }

    @GetMapping("/find-by-name/{name}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductForListResponseDTO>> findByName(@PathVariable String name){
        return ResponseEntity.ok(productService.findByName(name));
    }

    @GetMapping("/find-by-category/{category}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ProductForListResponseDTO>> findByCategory(@PathVariable String category){
        Category categoryEnum = Category.valueOf(category.toUpperCase());
        return ResponseEntity.ok(productService.findByCategory(categoryEnum));
    }

    @DeleteMapping("/delete/{code}")
    public void delete(@PathVariable String code){
        productService.delete(code);
    }
}
