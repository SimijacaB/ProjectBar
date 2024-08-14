package com.app.projectbar.domain.dto.product;

import com.app.projectbar.domain.dto.productIngredient.ProductIngredientResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Double price;
    private Long photoId;
    private Boolean isPrepared;
    private String category;
    private List<ProductIngredientResponseDTO> ingredients;

}
