package com.app.projectbar.domain.dto;


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
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$")
    @Min(10)
    private String name;

    @Pattern(regexp = "^[A-Z0-9-]{9}$")
    private String code;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$")
    @Min(10)
    private String description;

    @NotBlank
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    private Long photoId;
    @NotNull
    private Boolean isPrepared;
    @NotNull
    private String category;

    private List<ProductIngredientRequestDTO> ingredients;
}
