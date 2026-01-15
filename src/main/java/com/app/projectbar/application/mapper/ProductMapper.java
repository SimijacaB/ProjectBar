package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.ProductIngredient;
import com.app.projectbar.domain.dto.product.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.product.ProductRequestDTO;
import com.app.projectbar.domain.dto.product.ProductResponseDTO;
import com.app.projectbar.domain.dto.product.UpdateProductRequestDTO;
import com.app.projectbar.domain.dto.productIngredient.ProductIngredientResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductForListResponseDTO toListDTO(Product product);

    List<ProductForListResponseDTO> toListDTOList(List<Product> products);

    @Mapping(target = "ingredients", source = "productIngredients")
    ProductResponseDTO toResponseDTO(Product product);

    @Mapping(target = "ingredient_id", source = "ingredient.id")
    @Mapping(target = "ingredientName", source = "ingredient.name")
    @Mapping(target = "ingredientExtend", ignore = true)
    ProductIngredientResponseDTO toProductIngredientResponseDTO(ProductIngredient productIngredient);

    @Mapping(target = "ingredients", ignore = true)
    ProductRequestDTO updateToRequest(UpdateProductRequestDTO updateProductRequestDTO);
}

