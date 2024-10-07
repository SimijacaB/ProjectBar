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

        // Simulamos el guardado de Inventory y el mapeo de InventoryResponseDTO
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
        // GIVEN
        String code = "B01-UN-0001P";
        Integer quantityToDeduct = 10;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(code);
        existingInventory.setQuantity(20);

        //Simular el producto relacionado
        Product product = new Product();
        product.setName("Pilsen");

        //Objeto de respuesta simulado
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();
        responseDTO.setQuantity(10);
        responseDTO.setName("Pilsen");

        //Configurar los mocks
        when(inventoryRepository.findByCode(code)).thenReturn(Optional.of(existingInventory));
        when(productRepository.findByCode(code)).thenReturn(Optional.of(product));
        when(modelMapper.map(existingInventory, InventoryResponseDTO.class)).thenReturn(responseDTO);

        //   ---- WHEN ------
        InventoryResponseDTO result = inventoryService.deductStock(quantityToDeduct, code);

        //  ----  THEN  -----
        assertEquals(10, result.getQuantity());
        assertEquals("Pilsen", result.getName());
    }

    @Test
    public void testDeductStockExceptionWhenNotFoundSomethingWithCode(){
        // ---------  GIVEN -----------
        String code = "P01-UN-0001P";
        Integer quantityToDeduck = 15;



        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock(quantityToDeduck, code);
        });

        assertEquals("Inventory not found by code P01-UN-0001P", exception.getMessage());
    }

    @Test
    public void testDeductStockExceptionWhenQuantityIsNotEnought() {
        // ---------  GIVEN -----------
        String code = "P01-UN-0001P";
        Integer quantityToDeduct = 15;

        //Simular inventario existente
        Inventory existingInventory = new Inventory();
        existingInventory.setCode(code);
        existingInventory.setQuantity(10);

        when(inventoryRepository.findByCode(code)).thenReturn(Optional.of(existingInventory));

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deductStock(quantityToDeduct, code);
        });

        assertEquals("There is not enough inventory to discount", exception.getMessage());
    }

    @Test
    void findAll() {

        //  ---- GIVEN -----
        Inventory inventory1 = new Inventory();
        inventory1.setCode("G01-ML-0001I");
        inventory1.setQuantity(1970);

        Inventory inventory2 = new Inventory();
        inventory2.setCode("B01-UN-0001P");
        inventory2.setQuantity(50);

        Inventory inventory3 = new Inventory();
        inventory3.setCode("B01-UN-0002P");
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

        assertEquals("B01-UN-0001P", result.get(1).getCode());
        assertEquals("G01-ML-0001I", result.get(0).getCode());
        assertEquals("B01-UN-0002P", result.get(2).getCode());

        assertEquals(1970, result.get(0).getQuantity());
        assertEquals(70, result.get(1).getQuantity());
        assertEquals(50, result.get(2).getQuantity());

    }

    @Test
    void findByCode() {
        // ----- GIVEN -----
        String code = "G01-ML-0001I";
        Inventory inventory1 = new Inventory();
        inventory1.setCode("G01-ML-0001I");
        inventory1.setQuantity(1970);

        when(this.inventoryRepository.findByCode("G01-ML-0001I")).thenReturn(Optional.of(inventory1));

        InventoryResponseDTO inventoryResponse1 = new InventoryResponseDTO("Ginebra", "G01-ML-0001I", 1970);

        when(modelMapper.map(inventory1, InventoryResponseDTO.class)).thenReturn(inventoryResponse1);


        //  ---- WHEN ------
        InventoryResponseDTO result = inventoryService.findByCode(code);

        // ------  THEN --------
        assertEquals("Ginebra", result.getName());
        assertEquals("G01-ML-0001I", result.getCode());
        assertEquals(1970, result.getQuantity());

    }

    @Test
    public void testFindByCodeWhenNotFound(){

        // ---------  GIVEN -----------
        String code = "P01-UN-0001P";

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.findByCode(code);
        });

        assertEquals("Inventory not found by code " +code, exception.getMessage());

    }

    @Test
    void deleteByCode() {
        // ---- GIVEN -------
        String code = "P01-UN-0001P";

        Inventory inventory = new Inventory();
        inventory.setCode(code);

        when(inventoryRepository.findByCode(code)).thenReturn(Optional.of(inventory));

        // ----  WHEN -------
        inventoryService.deleteByCode(code);

        // ---- THEN -------
        verify(inventoryRepository).deleteByCode(code);


    }

    @Test
    public void testDeleteByCodeWhenNotFound(){

        // ---------  GIVEN -----------
        String code = "P01-UN-0001P";

        //  ------- WHEN AND THEN ----------
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.deleteByCode(code);
        });

        assertEquals("Inventory not found by code " + code, exception.getMessage());

    }


}