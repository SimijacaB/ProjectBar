package com.app.projectbar.domain.dto.inventory;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryDTO {

    @Pattern(regexp = "^[A-Z]{1}[0-9]{2}-{1}[A-Z]{2}-{1}[0-9]{4}[A-Z]{1}$", message =  "Code must follow the pattern A99-AA-9999A")
    private String code;

    @Min(value = 0, message = "Value must be zero or positive")
    private Integer quantity;

}
