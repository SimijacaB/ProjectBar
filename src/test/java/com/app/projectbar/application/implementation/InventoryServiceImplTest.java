package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.Inventory;
import com.app.projectbar.domain.Product;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.domain.dto.inventory.InventoryDTO;
import com.app.projectbar.domain.dto.inventory.InventoryResponseDTO;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
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

    private InventoryDTO inventoryRequest;
    private static final String CODE_GINEBRA = "G01-ML-0001I";
    private static final String CODE_PILSEN = "B01-UN-0001P";
    private static final String CODE_AGUILA = "B01-UN-0002P";

    private InventoryResponseDTO inventoryResponse;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        inventoryRequest = new InventoryDTO();
        inventoryRequest.setCode(CODE_GINEBRA);
    }


    @Test
    @DisplayName("Test save method with valid ingredient")
    void testSave() {
        //Given

        Ingredient ingredient = new Ingredient();
        ingredient.setName("Ginebra");

        Inventory inventory = new Inventory();
        inventory.setId(1L);

        //Simular ingrediente que vamos a encontrar por código
        when(ingredientRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.of(ingredient));

        // Simulamos el mapeo de InventoryDTO a Inventory
        when(modelMapper.map(inventoryRequest, Inventory.class)).thenReturn(inventory);

        // Simulamos el guardado de Inventory y el mapeo de InventoryResponseDTO
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

         inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setCode(CODE_GINEBRA);
        inventoryResponse.setName("Ginebra");
        when(modelMapper.map(inventory, InventoryResponseDTO.class)).thenReturn(inventoryResponse);

        //When
        InventoryResponseDTO result = inventoryService.save(inventoryRequest);

        //then
        assertNotNull(result);
        assertEquals("Ginebra", result.getName());
        assertEquals(CODE_GINEBRA, result.getCode());

    }
    @Test
    void testSaveAssignsIngredientNameIfProductNameNotPresent() {
        // Given
        InventoryDTO inventoryRequest = new InventoryDTO();
        inventoryRequest.setCode(CODE_GINEBRA);

        Product product = new Product();
        product.setCode(CODE_GINEBRA);

        Ingredient ingredient = new Ingredient();
        ingredient.setCode(CODE_GINEBRA);
        ingredient.setName("Ginebra");

        Inventory inventory = new Inventory();
        inventory.setId(1L);

        when(productRepository.findByCode(CODE_GINEBRA)).thenReturn(Optional.of(product));
        when(ingredientRepository.findByCode(CODE_GINEBRA)).thenReturn(Optional.of(ingredient));
        when(modelMapper.map(inventoryRequest, Inventory.class)).thenReturn(inventory);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        when(modelMapper.map(inventory, InventoryResponseDTO.class)).thenReturn(new InventoryResponseDTO());

        // When
        InventoryResponseDTO result = inventoryService.save(inventoryRequest);

        // Then
        assertEquals(CODE_GINEBRA, result.getCode());
        assertEquals("Ginebra", result.getName());
    }

    @Test
    public void testSaveException(){
        //Given

        // Simulamos que no se encuentra ni producto ni ingrediente
        when(productRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.empty());
        when(ingredientRepository.findByCode(inventoryRequest.getCode())).thenReturn(Optional.empty());

        //When y Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.save(inventoryRequest);
        });

        assertEquals("Product not found by code " + CODE_GINEBRA, exception.getMessage());
        verify(productRepository).findByCode(inventoryRequest.getCode());
        verify(ingredientRepository).findByCode(inventoryRequest.getCode());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void testAddStock() {
        // GIVEN
        Integer quantityToAdd = 10;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(CODE_PILSEN);
        existingInventory.setQuantity(5);

        //Simular el producto relacionado
        Product product = new Product();
        product.setName("Pilsen");

        //Objeto de respuesta simulado
        inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setQuantity(15);
        inventoryResponse.setName("Pilsen");

        //Configurar los mocks
        when(inventoryRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(existingInventory));
        when(productRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(product));
        when(modelMapper.map(existingInventory, InventoryResponseDTO.class)).thenReturn(inventoryResponse);

        //  WHEN
        InventoryResponseDTO result = inventoryService.addStock(quantityToAdd, CODE_PILSEN);

        //Then
        assertEquals(15, result.getQuantity());
        //assertEquals("Pilsen", result.getName());

    }

    @Test
    public void testAddStockException(){
        // ---------  GIVEN -----------
        Integer quantityToAdd = 15;
        //Simulamos que no encontramos un Inventory con este code
        when(inventoryRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.empty());

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.addStock(quantityToAdd, CODE_PILSEN);
        });

        assertEquals("Inventory not found by code " + CODE_PILSEN, exception.getMessage());
    }

    @Test
    void deductStock() {
        // GIVEN
        Integer quantityToDeduct = 10;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(CODE_PILSEN);
        existingInventory.setQuantity(20);

        //Simular el producto relacionado
        Product product = new Product();
        product.setName("Pilsen");

        //Objeto de respuesta simulado
        inventoryResponse = new InventoryResponseDTO();
        inventoryResponse.setQuantity(10);
        inventoryResponse.setName("Pilsen");

        //Configurar los mocks
        when(inventoryRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(existingInventory));
        when(productRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(product));
        when(modelMapper.map(existingInventory, InventoryResponseDTO.class)).thenReturn(inventoryResponse);

        //   ---- WHEN ------
        InventoryResponseDTO result = inventoryService.deductStock(quantityToDeduct, CODE_PILSEN);

        //  ----  THEN  -----
        assertEquals(10, result.getQuantity());
        assertEquals("Pilsen", result.getName());
    }

    @Test
    public void testDeductStockExceptionWhenNotFoundSomethingWithCode(){
        // ---------  GIVEN -----------
        Integer quantityToDeduck = 15;



        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock(quantityToDeduck, CODE_PILSEN);
        });

        assertEquals("Inventory not found by code " + CODE_PILSEN, exception.getMessage());
    }

    @Test
    public void testDeductStockExceptionWhenQuantityIsNotEnought() {
        // ---------  GIVEN -----------
        Integer quantityToDeduct = 15;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(CODE_PILSEN);
        existingInventory.setQuantity(10);

        when(inventoryRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(existingInventory));

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock(quantityToDeduct, CODE_PILSEN);
        });

        assertEquals("There is not enough inventory to discount", exception.getMessage());
    }

    @Test
    void findAll() {

        //  ---- GIVEN -----
        Inventory inventory1 = new Inventory();
        inventory1.setCode(CODE_GINEBRA);
        inventory1.setQuantity(1970);

        Inventory inventory2 = new Inventory();
        inventory2.setCode(CODE_PILSEN);
        inventory2.setQuantity(50);

        Inventory inventory3 = new Inventory();
        inventory3.setCode(CODE_AGUILA);
        inventory3.setQuantity(30);

        InventoryResponseDTO inventoryResponse1 = new InventoryResponseDTO("Ginebra", "G01-ML-0001I", 1970);
        InventoryResponseDTO inventoryResponse2 = new InventoryResponseDTO("Pilsen", "B01-UN-0001P", 70);
        InventoryResponseDTO inventoryResponse3= new InventoryResponseDTO("Aguila", "B01-UN-0002P", 50);

        when(inventoryRepository.findAll()).thenReturn(List.of(inventory1, inventory2, inventory3));

        when(modelMapper.map(inventory1, InventoryResponseDTO.class)).thenReturn(inventoryResponse1);
        when(modelMapper.map(inventory2, InventoryResponseDTO.class)).thenReturn(inventoryResponse2);
        when(modelMapper.map(inventory3, InventoryResponseDTO.class)).thenReturn(inventoryResponse3);

        // ----- WHEN ------
        List<InventoryResponseDTO> result = inventoryService.findAll();

        // ------ THEN -------
        assertEquals(3, result.size());

        assertEquals("Ginebra", result.get(0).getName());
        assertEquals("Pilsen", result.get(1).getName());
        assertEquals("Aguila", result.get(2).getName());

        assertEquals(CODE_GINEBRA, result.get(0).getCode());
        assertEquals(CODE_PILSEN, result.get(1).getCode());
        assertEquals(CODE_AGUILA, result.get(2).getCode());

        assertEquals(1970, result.get(0).getQuantity());
        assertEquals(70, result.get(1).getQuantity());
        assertEquals(50, result.get(2).getQuantity());

    }

    @Test
    void findByCode() {
        // ----- GIVEN -----
        Inventory inventory1 = new Inventory();
        inventory1.setCode(CODE_GINEBRA);
        inventory1.setQuantity(1970);

        when(this.inventoryRepository.findByCode(CODE_GINEBRA)).thenReturn(Optional.of(inventory1));

        inventoryResponse = new InventoryResponseDTO("Ginebra", CODE_GINEBRA, 1970);

        when(modelMapper.map(inventory1, InventoryResponseDTO.class)).thenReturn(inventoryResponse);


        //  ---- WHEN ------
        InventoryResponseDTO result = inventoryService.findByCode(CODE_GINEBRA);

        // ------  THEN --------
        assertEquals("Ginebra", result.getName());
        assertEquals(CODE_GINEBRA, result.getCode());
        assertEquals(1970, result.getQuantity());

    }

    @Test
    public void testFindByCodeWhenNotFound(){

        // ---------  GIVEN -----------


        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.findByCode(CODE_PILSEN);
        });

        assertEquals("Inventory not found by code " +CODE_PILSEN, exception.getMessage());

    }

    @Test
    void deleteByCode() {
        // ---- GIVEN -------

        Inventory inventory = new Inventory();
        inventory.setCode(CODE_PILSEN);

        when(inventoryRepository.findByCode(CODE_PILSEN)).thenReturn(Optional.of(inventory));

        // ----  WHEN -------
        inventoryService.deleteByCode(CODE_PILSEN);

        // ---- THEN -------
        verify(inventoryRepository).deleteByCode(CODE_PILSEN);


    }

    @Test
    public void testDeleteByCodeWhenNotFound(){

        // ---------  GIVEN -----------

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deleteByCode(CODE_PILSEN);
        });

        assertEquals("Inventory not found by code " + CODE_PILSEN, exception.getMessage());

    }


}