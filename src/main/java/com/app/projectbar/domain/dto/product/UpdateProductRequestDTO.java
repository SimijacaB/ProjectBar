package com.app.projectbar.domain.dto.product;


import com.app.projectbar.domain.dto.productIngredient.ProductIngredientRequestDTO;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProductRequestDTO {

    @NotNull
    private Long id;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$", message = "This field cannot contains numbers or special characters")
    @Size(min = 3, message = "Name must contains at least 3 characteres.")
    private String name;

    @Pattern(regexp = "^[A-Z]{1}[0-9]{2}-{1}[A-Z]{2}-{1}[0-9]{4}[A-Z]{1}$", message = "Code must follow the pattern A99-AA-9999A")
    private String code;

    @NotBlank
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s\\p{Punct}]+$", message = "Description cannot contains special characters")
    @Size(min = 10, message = "Description must contains at least 10 characteres.")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    private Long photoId;

    @NotNull
    private Boolean isPrepared;

    @NotNull
    @Pattern(regexp = "^(BEER|WINE|COCKTAILS|JUICES)$", message = "The available categories to choose from are: 'BEER', 'WINE', 'COCKTAILS', 'JUICES'.")
    private String category;

    private List<ProductIngredientRequestDTO> ingredients;
}
