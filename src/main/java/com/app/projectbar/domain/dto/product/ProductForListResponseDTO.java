package com.app.projectbar.domain.dto.product;

import com.app.projectbar.domain.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductForListResponseDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Category category;


}
