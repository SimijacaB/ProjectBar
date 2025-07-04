package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IProductService;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.ProductIngredient;
import com.app.projectbar.domain.dto.product.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.product.ProductRequestDTO;
import com.app.projectbar.domain.dto.product.ProductResponseDTO;
import com.app.projectbar.domain.dto.product.UpdateProductRequestDTO;
import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.Product;
import com.app.projectbar.infra.repositories.IProductRepository;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;
    private final IIngredientRepository ingredientRepository;
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
        ProductResponseDTO productResponseDTO = modelMapper.map(product, ProductResponseDTO.class);

        // Map ingredient_id for each ingredient in the response DTO
        for (int i = 0; i < productResponseDTO.getIngredients().size(); i++) {
            productResponseDTO.getIngredients().get(i).setIngredient_id(product.getProductIngredients().get(i).getIngredient().getId());
            productResponseDTO.getIngredients().get(i).setIngredientExtend(product.getProductIngredients().get(i).getIngredient().getUnitOfMeasure().toString());
        }

        return productResponseDTO;
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
    public List<ProductForListResponseDTO> findByCategory(Category category) {
        var productList = productRepository.findByCategory(category)
                .orElseThrow(() -> new RuntimeException("Products with name " + category + " not found"));

        return productList.stream()
                .map(product -> modelMapper.map(product, ProductForListResponseDTO.class)).toList();
    }

   /* public ProductResponseDTO saveOrUpdate(Product product, ProductRequestDTO productRequest) {

        product.setName(productRequest.getName());
        product.setCode(productRequest.getCode());
        product.setDescription(productRequest.getDescription());
        product.setPrice(productRequest.getPrice());
        product.setPhotoId(productRequest.getPhotoId());
        product.setIsPrepared(productRequest.getIsPrepared());
        product.setCategory(productRequest.getCategory());

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

    }*/
   public ProductResponseDTO saveOrUpdate(Product product, ProductRequestDTO productRequest) {

       product.setName(productRequest.getName());
       product.setCode(productRequest.getCode());
       product.setDescription(productRequest.getDescription());
       product.setPrice(productRequest.getPrice());
       product.setPhotoId(productRequest.getPhotoId());
       product.setIsPrepared(productRequest.getIsPrepared());
       product.setCategory(productRequest.getCategory());

       // Actualizar las relaciones de ProductIngredient
       List<ProductIngredient> existingIngredients = product.getProductIngredients();
       List<ProductIngredient> newIngredients = productRequest.getIngredients()
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
               .toList();

       // Eliminar ingredientes que ya no están en la lista
       existingIngredients.removeIf(existingIngredient ->
               newIngredients.stream().noneMatch(newIngredient ->
                       newIngredient.getIngredient().getId().equals(existingIngredient.getIngredient().getId())
               )
       );

       // Agregar o actualizar ingredientes
       for (ProductIngredient newIngredient : newIngredients) {
           existingIngredients.stream()
                   .filter(existingIngredient -> existingIngredient.getIngredient().getId().equals(newIngredient.getIngredient().getId()))
                   .findFirst()
                   .ifPresentOrElse(
                           existingIngredient -> existingIngredient.setAmount(newIngredient.getAmount()),
                           () -> existingIngredients.add(newIngredient)
                   );
       }

       product.setProductIngredients(existingIngredients);
       Product savedProduct = productRepository.save(product);

       ProductResponseDTO productResponseDTO = modelMapper.map(savedProduct, ProductResponseDTO.class);
       for (int i = 0; i < productResponseDTO.getIngredients().size(); i++) {
           productResponseDTO.getIngredients().get(i).setIngredient_id(savedProduct.getProductIngredients().get(i).getIngredient().getId());
           productResponseDTO.getIngredients().get(i).setIngredientExtend(savedProduct.getProductIngredients().get(i).getIngredient().getUnitOfMeasure().toString());
       }
       return productResponseDTO;
   }


}


