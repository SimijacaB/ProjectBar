package com.app.projectbar.application.impl;

import com.app.projectbar.application.IInventoryService;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.dto.InventoryDTO;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final IInventoryRepository inventoryRepository;
    private final IProductRepository productRepository;
    private final IIngredientRepository ingredientRepository;
    private final ModelMapper modelMapper;

    @Override
    public InventoryDTO save(InventoryDTO inventoryRequest) {
        Optional<Product> product = productRepository.findByCode(inventoryRequest.getCode());
        Optional<Ingredient> ingredient = ingredientRepository.findByCode(inventoryRequest.getCode());
        if(product.isEmpty() && ingredient.isEmpty()){
            throw new RuntimeException("Product not found by code " + inventoryRequest.getCode());
        }
        Inventory inventory = inventoryRepository.save(modelMapper.map(inventoryRequest, Inventory.class));
        return modelMapper.map(inventory, InventoryDTO.class);

    }

    @Override
    public InventoryDTO addStock(Integer quantityToAdd, String code) {
        Optional<Inventory> inventory = inventoryRepository.findByCode(code);
        if(inventory.isEmpty()){
            throw new RuntimeException("Inventory not found by code " + code);
        }
        Inventory inventory1 = inventory.get();
        inventory1.setQuantity(inventory1.getQuantity() + quantityToAdd);

        return modelMapper.map(inventoryRepository.save(inventory1), InventoryDTO.class);
    }

    @Override
    public InventoryDTO deductStock(Integer quantity, String code) {
        Optional<Inventory> inventory = inventoryRepository.findByCode(code);
        if(inventory.isEmpty()){
            throw new RuntimeException("Inventory not found by code " + code);
        }

        Inventory inventory1 = inventory.get();
        if(inventory1.getQuantity() < quantity){
            throw new RuntimeException("There is not enough inventory to discount");
        }

        inventory1.setQuantity(inventory1.getQuantity() - quantity);
        return modelMapper.map(inventoryRepository.save(inventory1), InventoryDTO.class);
    }

    @Override
    public List<InventoryDTO> findAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(inventory -> modelMapper.map(inventory, InventoryDTO.class)).toList();
    }

    @Override
    public InventoryDTO findByCode(String code) {
        Optional<Inventory> inventory = inventoryRepository.findByCode(code);

        if(inventory.isEmpty()){
            throw new RuntimeException("Inventory not found by code " + code);
        }

        return modelMapper.map(inventory.get(), InventoryDTO.class);
    }

    @Override
    public void deleteByCode(String code) {
        inventoryRepository.deleteByCode(code);
    }
}
