package com.app.projectbar.application;

import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;

import java.util.List;

public interface IInventoryService {

    InventoryResponseDTO save(InventoryDTO inventoryRequest);

    InventoryResponseDTO addStock(Integer quantityToAdd, String code);

    InventoryResponseDTO deductStock(Integer quantity, String code);

    List<InventoryResponseDTO> findAll();

    InventoryResponseDTO findByCode(String code);

    void deleteByCode(String code);

}
