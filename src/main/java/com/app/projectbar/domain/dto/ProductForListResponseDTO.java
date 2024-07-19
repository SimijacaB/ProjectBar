package com.app.projectbar.domain.dto;

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


}
