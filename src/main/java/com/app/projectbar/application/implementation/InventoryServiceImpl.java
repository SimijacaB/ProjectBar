package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.inventory.InsufficientInventoryException;
import com.app.projectbar.application.exception.inventory.InventoryAlreadyExistsException;
import com.app.projectbar.application.exception.inventory.InventoryNotFoundException;
import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.application.mapper.InventoryMapper;
import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements IInventoryService {

    private final IInventoryRepository inventoryRepository;
    private final IProductRepository productRepository;
    private final IIngredientRepository ingredientRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryResponseDTO save(InventoryDTO inventoryRequest) {
        Optional<Product> product = productRepository.findByCode(inventoryRequest.getCode());
        Optional<Ingredient> ingredient = ingredientRepository.findByCode(inventoryRequest.getCode());
        if (product.isEmpty() && ingredient.isEmpty()) {
            throw new InventoryNotFoundException(
                    String.format(ErrorMessagesService.PRODUCT_OR_INGREDIENT_NOT_FOUND_BY_CODE.getMessage(), inventoryRequest.getCode()));
        }
        if (product.isPresent() && Boolean.TRUE.equals(product.get().getIsPrepared())) {
            throw new IllegalArgumentException(ErrorMessagesService.PREPARED_PRODUCT_CANNOT_HAVE_INVENTORY.getMessage());
        }
        if (inventoryRepository.findByCode(inventoryRequest.getCode()).isPresent()) {
            throw new InventoryAlreadyExistsException(
                    String.format(ErrorMessagesService.INVENTORY_ALREADY_EXISTS.getMessage(), inventoryRequest.getCode()));
        }
        Inventory inventory = inventoryRepository.save(inventoryMapper.toEntity(inventoryRequest));
        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);

        product.ifPresent(p -> {
            response.setCode(p.getCode());
            response.setName(p.getName());
        });

        ingredient.ifPresent(i -> {
            if (response.getCode() == null) {
                response.setCode(i.getCode());
            }
            if (response.getName() == null) {
                response.setName(i.getName());
            }
        });
        return response;
    }

    @Override
    public InventoryResponseDTO addStock(Integer quantityToAdd, String code) {
        Optional<Product> productOptional = productRepository.findByCode(code);
        Optional<Ingredient> ingredientOptional = ingredientRepository.findByCode(code);
        Inventory inventory = inventoryRepository.findByCode(code)
                .orElseThrow(() -> new InventoryNotFoundException(
                        String.format(ErrorMessagesService.INVENTORY_NOT_FOUND_BY_CODE.getMessage(), code)));

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        inventoryRepository.save(inventory);

        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
        productOptional.ifPresent(p -> response.setName(p.getName()));
        ingredientOptional.ifPresent(i -> response.setName(i.getName()));
        return response;
    }

    @Override
    public InventoryResponseDTO deductStock(Integer quantity, String code) {
        Optional<Product> productOptional = productRepository.findByCode(code);
        Optional<Ingredient> ingredientOptional = ingredientRepository.findByCode(code);
        Inventory inventory = inventoryRepository.findByCode(code)
                .orElseThrow(() -> new InventoryNotFoundException(
                        String.format(ErrorMessagesService.INVENTORY_NOT_FOUND_BY_CODE.getMessage(), code)));

        if (inventory.getQuantity() < quantity) {
            throw new InsufficientInventoryException(
                    String.format(ErrorMessagesService.INSUFFICIENT_INVENTORY_TO_DEDUCT.getMessage(), code));
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);

        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
        productOptional.ifPresent(p -> response.setName(p.getName()));
        ingredientOptional.ifPresent(i -> response.setName(i.getName()));
        return response;
    }

    @Override
    public List<InventoryResponseDTO> findAll() {
        List<InventoryResponseDTO> response = inventoryMapper.toResponseDTOList(inventoryRepository.findAll());
        for (InventoryResponseDTO inventory : response) {
            Optional<Product> product = productRepository.findByCode(inventory.getCode());
            Optional<Ingredient> ingredient = ingredientRepository.findByCode(inventory.getCode());
            product.ifPresent(p -> inventory.setName(p.getName()));
            ingredient.ifPresent(i -> inventory.setName(i.getName()));
        }
        return response;
    }

    @Override
    public InventoryResponseDTO findByCode(String code) {
        Inventory inventory = inventoryRepository.findByCode(code)
                .orElseThrow(() -> new InventoryNotFoundException(
                        String.format(ErrorMessagesService.INVENTORY_NOT_FOUND_BY_CODE.getMessage(), code)));

        Optional<Product> product = productRepository.findByCode(code);
        Optional<Ingredient> ingredient = ingredientRepository.findByCode(code);

        InventoryResponseDTO response = inventoryMapper.toResponseDTO(inventory);
        product.ifPresent(p -> response.setName(p.getName()));
        ingredient.ifPresent(i -> response.setName(i.getName()));
        return response;
    }

    @Override
    public void deleteByCode(String code) {
        inventoryRepository.findByCode(code)
                .orElseThrow(() -> new InventoryNotFoundException(
                        String.format(ErrorMessagesService.INVENTORY_NOT_FOUND_BY_CODE.getMessage(), code)));
        inventoryRepository.deleteByCode(code);
    }
}
