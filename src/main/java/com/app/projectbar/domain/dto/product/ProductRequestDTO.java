package com.app.projectbar.domain.dto.product;

import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.dto.productIngredient.ProductIngredientRequestDTO;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class ProductRequestDTO {

    @NotBlank
    @Pattern(regexp = "^[a-zA-ZñÑáéíóúÁÉÍÓÚ\\s\\p{Punct}]+$", message = "Name must contain only letters, spaces, and punctuation")
    @Size(min = 3, message = "Name must have at least 10 characters")
    private String name;

    @Pattern(regexp = "^[A-Z]{1}[0-9]{2}-{1}[A-Z]{2}-{1}[0-9]{4}[A-Z]{1}$", message = "Code must follow the pattern A99-AA-9999A")
    private String code;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    private Long photoId;

    @NotNull
    private Boolean isPrepared;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Category category;

    private List<ProductIngredientRequestDTO> ingredients;

}
