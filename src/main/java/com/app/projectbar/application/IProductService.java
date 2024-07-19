package com.app.projectbar.application;

import com.app.projectbar.domain.dto.ProductForListResponseDTO;
import com.app.projectbar.domain.dto.ProductRequestDTO;
import com.app.projectbar.domain.dto.ProductResponseDTO;
import com.app.projectbar.domain.dto.UpdateProductRequestDTO;

import java.util.List;

public interface IProductService {

    List<ProductForListResponseDTO> findAll();

    ProductResponseDTO findById(Long id);

    ProductResponseDTO findByCode(String code);

    List<ProductForListResponseDTO> findByName(String name);

    ProductResponseDTO save(ProductRequestDTO productRequestDTO);

    ProductResponseDTO update(UpdateProductRequestDTO productRequestDTO);

    void delete(String code);

    List<ProductForListResponseDTO> findByCategory(String category);
}
