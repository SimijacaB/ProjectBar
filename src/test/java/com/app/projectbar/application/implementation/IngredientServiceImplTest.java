package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.dto.ingredient.IngredientRequestDTO;
import com.app.projectbar.domain.dto.ingredient.IngredientResponseDTO;
import com.app.projectbar.domain.enums.UnitOfMeasure;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class IngredientServiceImplTest {


    @Mock
    private IIngredientRepository ingredientRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private IngredientServiceImpl ingredientService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        Ingredient ingredient1 = new Ingredient(1L, "G01-ML-0001I", "Ginebra", UnitOfMeasure.ML, null);
        Ingredient ingredient2 = new Ingredient(2L, "T01-ML-0002I", "Tonica", UnitOfMeasure.ML, null);
        Ingredient ingredient3 = new Ingredient(3L, "L01-UN-0001I", "Unidad de Limon", null, null);

        IngredientResponseDTO ingredientResponseDTO1 = new IngredientResponseDTO();
        ingredientResponseDTO1.setId(1L);
        ingredientResponseDTO1.setCode("G01-ML-0001I");
        ingredientResponseDTO1.setName("Ginebra");
        ingredientResponseDTO1.setUnitOfMeasure("ML");

        IngredientResponseDTO ingredientResponseDTO2 = new IngredientResponseDTO();
        ingredientResponseDTO2.setId(2L);
        ingredientResponseDTO2.setCode("T01-ML-0002I");
        ingredientResponseDTO2.setName("Tonica");
        ingredientResponseDTO2.setUnitOfMeasure("ML");

        IngredientResponseDTO ingredientResponseDTO3 = new IngredientResponseDTO();
        ingredientResponseDTO3.setId(3L);
        ingredientResponseDTO3.setCode("L01-UN-0001I");
        ingredientResponseDTO3.setName("Unidad de Limon");
        ingredientResponseDTO3.setUnitOfMeasure(null);

        when(this.ingredientRepository.findById(1L)).thenReturn(Optional.of(ingredient1));
        when(this.ingredientRepository.findById(2L)).thenReturn(Optional.of(ingredient2));
        when(this.ingredientRepository.findById(3L)).thenReturn(Optional.of(ingredient3));

        when(this.ingredientRepository.findByCode("G01-ML-0001I")).thenReturn(Optional.of(ingredient1));
        when(this.ingredientRepository.findByCode("T01-ML-0002I")).thenReturn(Optional.of(ingredient2));
        when(this.ingredientRepository.findByCode("L01-UN-0001I")).thenReturn(Optional.of(ingredient3));

        when(this.ingredientRepository.findAll()).thenReturn(List.of(ingredient1, ingredient2, ingredient3));

        when(modelMapper.map(ingredient1, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO1);
        when(modelMapper.map(ingredient2, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO2);
        when(modelMapper.map(ingredient3, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO3);
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


    @Test
    public void testFindById(){
        //Given
        Long id = 1L;

        //When
        IngredientResponseDTO result = ingredientService.findById(id);
        
        //Then
        assertEquals("Ginebra", result.getName());
        assertEquals("G01-ML-0001I", result.getCode());
        assertEquals("ML", result.getUnitOfMeasure());

    }

    @Test
    public void testFindByIdError(){
        //Given
        Long id = 8L;  //Un id que no existe

        //Then
        assertThrows(RuntimeException.class , () ->
                ingredientService.findById(id));

    }

    @Test
    public void testFindByCodeError(){
        //Given
        String code ="K01-ML-0101I";  //Un code que no existe

        //Then
        assertThrows(RuntimeException.class , () ->
                ingredientService.findByCode(code));

    }


    @Test
    public void testFindByCode(){
        //Given
        String code = "G01-ML-0001I";

        //When
        IngredientResponseDTO result = ingredientService.findByCode(code);

        //Then
        assertEquals("Ginebra", result.getName());
        assertEquals("G01-ML-0001I", result.getCode());
        assertEquals("ML", result.getUnitOfMeasure());
    }


    @Test
    public void testFindAll(){

        //When

        List<IngredientResponseDTO> result = ingredientService.findAll();

        //Then
        assertEquals(3, result.size());
        // Verifying the first ingredient
        assertEquals("Ginebra", result.get(0).getName());
        assertEquals("G01-ML-0001I", result.get(0).getCode());
        assertEquals("ML", result.get(0).getUnitOfMeasure());

        // Verifying the second ingredient
        assertEquals("Tonica", result.get(1).getName());
        assertEquals("T01-ML-0002I", result.get(1).getCode());
        assertEquals("ML", result.get(1).getUnitOfMeasure());

        // Verifying the third ingredient
        assertEquals("Unidad de Limon", result.get(2).getName());
        assertEquals("L01-UN-0001I", result.get(2).getCode());
        Assertions.assertNull(result.get(2).getUnitOfMeasure());

    }

    @Test
    public void testSave() {
        // Given
        IngredientRequestDTO ingredientRequestDTO = new IngredientRequestDTO();
        ingredientRequestDTO.setCode("B01-ML-0006I");
        ingredientRequestDTO.setName("Brandy");
        ingredientRequestDTO.setUnitOfMeasure("ML");

        Ingredient ingredient = new Ingredient();
        ingredient.setId(4L);
        ingredient.setCode("B01-ML-0006I");
        ingredient.setName("Brandy");
        ingredient.setUnitOfMeasure(UnitOfMeasure.ML);

        IngredientResponseDTO ingredientResponseDTO = new IngredientResponseDTO();
        ingredientResponseDTO.setId(4L);
        ingredientResponseDTO.setCode("B01-ML-0006I");
        ingredientResponseDTO.setName("Brandy");
        ingredientResponseDTO.setUnitOfMeasure("ML");

        when(modelMapper.map(ingredientRequestDTO, Ingredient.class)).thenReturn(ingredient);
        when(ingredientRepository.save(ingredient)).thenReturn(ingredient);
        when(modelMapper.map(ingredient, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO);

        // When
        IngredientResponseDTO result = ingredientService.save(ingredientRequestDTO);

        // Then
        assertEquals("Brandy", result.getName());
        assertEquals("B01-ML-0006I", result.getCode());
        assertEquals("ML", result.getUnitOfMeasure());
    }

    @Test
    public void testDelete(){

        //Given
        String code = "G01-ML-0001I";

        //When
        ingredientService.delete(code);

        //Then
        verify(ingredientRepository).deleteByCode(code);

    }

}