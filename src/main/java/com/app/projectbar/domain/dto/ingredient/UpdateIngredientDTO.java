package com.app.projectbar.domain.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateIngredientDTO {
    private Long id;

    @Pattern(regexp = "^[A-Z]{1}[0-9]{2}-{1}[A-Z]{2}-{1}[0-9]{4}[A-Z]{1}$", message =  "Code must follow the pattern A99-AA-9999A")
    private String code;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\s\\p{Punct}]+$", message = "Name can´t contains numbers and special characters")
    @Size(min = 3, message = "Name must have at least 3 characters")
    private String name;

    @NotNull(message = "UnitOfMeasure can´t be null")
    private String unitOfMeasure;
}
