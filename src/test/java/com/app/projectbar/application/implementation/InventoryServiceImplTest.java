package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private InventoryDTO inventoryDTOI;
    private InventoryDTO inventoryDTOP;
    private Inventory inventoryI;
    private Inventory inventoryP;
    private InventoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

    }

     /*
    {
        "id": 1,
        "code": "G01-ML-0001I",
        "name": "Ginebra",
        "unitOfMeasure": "ML"
    },
    {
        "id": 2,
        "code": "T01-ML-0002I",
        "name": "Tonica",
        "unitOfMeasure": "ML"
    },
    {
        "id": 3,
        "code": "L01-UN-0001I",
        "name": "Unidad de Limon",
        "unitOfMeasure": null
    },
     */


    // should return a kind of InventoryResponseDTO
    @Test
    void testSave() {
        //Given
        InventoryDTO inventoryRequest = new InventoryDTO();
        inventoryRequest.setCode("G01-ML-0001I");

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Ginebra");

        Inventory inventory = new Inventory();
        inventory.setId(1L);

        //Simular ingrediente que vamos aq enncontrar por código
        when(ingredientRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.of(ingredient));

        // Simulamos el mapeo de InventoryDTO a Inventory
        when(modelMapper.map(inventoryRequest, Inventory.class)).thenReturn(inventory);

        // Simulamos el guardado de Inventore y el mapeo de InventoryResponseDTO
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryResponseDTO inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setCode("G01-ML-0001I");
        inventoryResponse.setName("Ginebra");
        when(modelMapper.map(inventory, InventoryResponseDTO.class)).thenReturn(inventoryResponse);

        //When
        InventoryResponseDTO result = inventoryService.save(inventoryRequest);

        //then
        assertNotNull(result);
        assertEquals("Ginebra", result.getName());
        assertEquals("G01-ML-0001I", result.getCode());

    }

    @Test
    public void testSaveException(){
        //Given
        InventoryDTO inventoryRequest = new InventoryDTO();
        inventoryRequest.setCode("P01-UN-0001P");
        // Simulamos que no se encuentra ni producto ni ingrediente
        when(productRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.empty());
        when(ingredientRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.empty());

        //When y Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.save(inventoryRequest);
        });

        assertEquals("Product not found by code P01-UN-0001P", exception.getMessage());
        verify(productRepository).findByCode(inventoryRequest.getCode());
        verify(ingredientRepository).findByCode(inventoryRequest.getCode());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testAddStock() {
        // GIVEN
        String code = "B01-UN-0001P";
        Integer quantityToAdd = 10;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(code);
        existingInventory.setQuantity(5);

        //Simular el producto relacionado
        Product product = new Product();
        product.setName("Pilsen");

        //Objeto de respuesta simulado
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setQuantity(15);
        responseDTO.setName("Pilsen");

        //Configurar los mocks
        when(inventoryRepository.findByCode(code)).thenReturn(Optional.of(existingInventory));
        when(productRepository.findByCode(code)).thenReturn(Optional.of(product));
        when(modelMapper.map(existingInventory, InventoryResponseDTO.class)).thenReturn(responseDTO);

        //  WHEN
        InventoryResponseDTO result = inventoryService.addStock(quantityToAdd, code);

        //Then
        assertEquals(15, result.getQuantity());
        //assertEquals("Pilsen", result.getName());

    }

    @Test
    public void testAddStockException(){
        // ---------  GIVEN -----------
        String code = "P01-UN-0001P";
        Integer quantityToAdd = 15;
        //Simulamos que no encontramos un Inventory con este code
        when(inventoryRepository.findByCode(code)).thenReturn(Optional.empty());

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.addStock(quantityToAdd, code);
        });

        assertEquals("Inventory not found by code P01-UN-0001P", exception.getMessage());
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