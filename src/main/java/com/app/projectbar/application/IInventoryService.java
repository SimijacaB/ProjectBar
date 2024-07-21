package com.app.projectbar.application;

import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.dto.InventoryDTO;

import java.util.List;

public interface IInventoryService {

    InventoryDTO save(InventoryDTO inventoryRequest);

    InventoryDTO addStock(Integer quantityToAdd, String code);

    InventoryDTO deductStock(Integer quantity, String code);

    List<InventoryDTO> findAll();

    InventoryDTO findByCode(String code);

    void deleteByCode(String code);

}
