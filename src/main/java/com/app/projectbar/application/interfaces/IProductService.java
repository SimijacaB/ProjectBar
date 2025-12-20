package com.app.projectbar.application.interfaces;

import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.dto.product.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.product.ProductRequestDTO;
import com.app.projectbar.domain.dto.product.ProductResponseDTO;
import com.app.projectbar.domain.dto.product.UpdateProductRequestDTO;

import java.util.List;

public interface IProductService {

    List<ProductForListResponseDTO> findAll();

    ProductResponseDTO findById(Long id);

    ProductResponseDTO findByCode(String code);

    List<ProductForListResponseDTO> findByName(String name);

    ProductResponseDTO save(ProductRequestDTO productRequestDTO);

    ProductResponseDTO update(UpdateProductRequestDTO productRequestDTO);

    void delete(String code);

    List<ProductForListResponseDTO> findByCategory(Category category);

    List<ProductForListResponseDTO> findByNameContaining(String name);
}
