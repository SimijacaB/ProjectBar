package com.app.projectbar.application.impl;

import com.app.projectbar.application.IProductService;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.ProductIngredient;
import com.app.projectbar.domain.dto.*;
import com.app.projectbar.infra.repositories.IProductIngredientRepository;
import com.app.projectbar.domain.Category;
import com.app.projectbar.domain.Product;
import com.app.projectbar.infra.repositories.IProductRepository;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final IIngredientRepository ingredientRepository;
    private final IProductIngredientRepository productIngredientRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ProductForListResponseDTO> findAll() {
        List<Product> productList = productRepository.findAll();
        return productList.stream()
                .map(product -> modelMapper.map(product, ProductForListResponseDTO.class)).toList();
    }


    @Override
    public ProductResponseDTO findById(Long id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with id " + id + " not found"));
        return modelMapper.map(product, ProductResponseDTO.class);
    }

    @Override
    public ProductResponseDTO findByCode(String code) {
        var product = productRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Product with code " + code + " not found"));
        return modelMapper.map(product, ProductResponseDTO.class);
    }

    @Override
    public List<ProductForListResponseDTO> findByName(String name) {
        var productList = productRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Products with name " + name + " not found"));

        return productList.stream()
                .map(product -> modelMapper.map(product, ProductForListResponseDTO.class)).toList();
    }

    @Override
    public ProductResponseDTO save(ProductRequestDTO productRequest) {
        var product = new Product();
        return saveOrUpdate(product, productRequest);
    }

    @Override
    public ProductResponseDTO update(UpdateProductRequestDTO productRequestDTO) {
        var product = productRepository.findById(productRequestDTO.getId())
                .orElseThrow(() -> new RuntimeException("Product with id " + productRequestDTO.getId() + " not found"));

        ProductRequestDTO mapped = modelMapper.map(productRequestDTO, ProductRequestDTO.class);

        return saveOrUpdate(product, mapped);
    }

    @Override
    public void delete(String code) {
        var product = productRepository.findByCode(code);

        product.ifPresentOrElse(productRepository::delete, () -> {
            throw new RuntimeException("Product with code " + code + " not found");
        });
    }

    @Override
    public List<ProductForListResponseDTO> findByCategory(String category) {
        var productList = productRepository.findByCategory(category)
                .orElseThrow(() -> new RuntimeException("Products with name " + category + " not found"));

        return productList.stream()
                .map(product -> modelMapper.map(product, ProductForListResponseDTO.class)).toList();
    }

    public ProductResponseDTO saveOrUpdate(Product product, ProductRequestDTO productRequest) {

        product.setName(productRequest.getName());
        product.setCode(productRequest.getCode());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setPhotoId(productRequest.getPhotoId());
        product.setIsPrepared(productRequest.getIsPrepared());
        product.setCategory(Category.valueOf(productRequest.getCategory()));

        // Eliminar las relaciones actuales si es una actualización
        if (product.getId() != null) {
            productIngredientRepository.deleteAll(product.getProductIngredients());
        }

        // Crear las nuevas relaciones de ProductIngredient
        List<ProductIngredient> productIngredients = productRequest.getIngredients()
                .stream()
                .map(piRequest -> {
                    Ingredient ingredient = ingredientRepository.findById(piRequest.getIngredientId())
                            .orElseThrow(() -> new RuntimeException("Ingredient with ID " + piRequest.getIngredientId() + " not found"));
                    return ProductIngredient.builder()
                            .product(product)
                            .ingredient(ingredient)
                            .amount(piRequest.getAmount())
                            .build();
                })
                .collect(Collectors.toList());

        product.setProductIngredients(productIngredients);
        Product savedProduct = productRepository.save(product);

        // Guarda las relaciones de ProductIngredient
        productIngredientRepository.saveAll(productIngredients);

        ProductResponseDTO productResponseDTO = modelMapper.map(savedProduct, ProductResponseDTO.class);
        for (int i = 0; i < productResponseDTO.getIngredients().size() ; i++) {
            productResponseDTO.getIngredients().get(i).setIngredient_id(savedProduct.getProductIngredients().get(i).getIngredient().getId());
            productResponseDTO.getIngredients().get(i).setIngredientExtend(savedProduct.getProductIngredients().get(i).getIngredient().getUnitOfMeasure().toString());
        }
        return productResponseDTO;

    }


}


