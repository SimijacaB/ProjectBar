package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.Product;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceImplTest {
    @Mock
    private IIngredientRepository ingredientRepository;

    @Mock
    private IInventoryRepository inventoryRepository;

    @Mock
    private IProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    void save() {
        Ingredient ingredient = new Ingredient();


        Product product = new Product();
    }

    @Test
    void addStock() {
    }

    @Test
    void deductStock() {
    }

    @Test
    void findAll() {
    }

    @Test
    void findByCode() {
    }

    @Test
    void deleteByCode() {
    }
}