package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
import com.app.projectbar.domain.dto.ingredient.*;
import com.app.projectbar.domain.enums.UnitOfMeasure;
import com.app.projectbar.infra.repositories.IIngredientRepository;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
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

        Long id = 1L;
        Ingredient ingredient1 = new Ingredient(1L, "G01-ML-0001I", "Ginebra", UnitOfMeasure.ML, null);
        IngredientResponseDTO ingredientResponseDTO1 = new IngredientResponseDTO();
        ingredientResponseDTO1.setId(1L);
        ingredientResponseDTO1.setCode("G01-ML-0001I");
        ingredientResponseDTO1.setName("Ginebra");
        ingredientResponseDTO1.setUnitOfMeasure("ML");

        when(this.ingredientRepository.findById( anyLong() )).thenReturn(Optional.of(ingredient1));
        when(this.ingredientRepository.findByCode("G01-ML-0001I")).thenReturn(Optional.of(ingredient1));
        when(modelMapper.map(ingredient1, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO1);

        Ingredient ingredient2 = new Ingredient(2L, "T01-ML-0002I", "Tonica", UnitOfMeasure.ML, null);
        IngredientResponseDTO ingredientResponseDTO2 = new IngredientResponseDTO();
        ingredientResponseDTO2.setId(2L);
        ingredientResponseDTO2.setCode("T01-ML-0002I");
        ingredientResponseDTO2.setName("Tonica");
        ingredientResponseDTO2.setUnitOfMeasure("ML");

        when(this.ingredientRepository.findById( anyLong() )).thenReturn(Optional.of(ingredient1));
        when(this.ingredientRepository.findByCode("T01-ML-0002I")).thenReturn(Optional.of(ingredient2));
        when(modelMapper.map(ingredient2, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO2);


        Ingredient ingredient3 = new Ingredient(3L, "L01-UN-0001I", "Unidad de Limon", UnitOfMeasure.ML, null);
        IngredientResponseDTO ingredientResponseDTO3 = new IngredientResponseDTO();
        ingredientResponseDTO3.setId(2L);
        ingredientResponseDTO3.setCode("L01-UN-0001I");
        ingredientResponseDTO3.setName("Unidad de Limon");
        ingredientResponseDTO3.setUnitOfMeasure("ML");

        List<Ingredient> resultList = List.of(
                ingredient1, ingredient2, ingredient3);

        when(this.ingredientRepository.findAll()).thenReturn(resultList);
        when(modelMapper.map(ingredient2, IngredientResponseDTO.class)).thenReturn(ingredientResponseDTO2);
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
        Assertions.assertEquals("Ginebra", result.getName());
        Assertions.assertEquals("G01-ML-0001I", result.getCode());
        Assertions.assertEquals(UnitOfMeasure.ML.toString(), result.getUnitOfMeasure());
    }

    @Test
    public void testFindByCode(){
        //Given
        String code = "G01-ML-0001I";

        //When
        IngredientResponseDTO result = ingredientService.findByCode(code);

        //Then
        Assertions.assertEquals("Ginebra", result.getName());
        Assertions.assertEquals("G01-ML-0001I", result.getCode());
        Assertions.assertEquals(UnitOfMeasure.ML.toString(), result.getUnitOfMeasure());
    }

    @Test
    public void testFindAll(){

        //When

        List<IngredientResponseDTO> result = ingredientService.findAll();

        //Then
        Assertions.assertEquals(3, result.size());
        Assertions.assertEquals("Ginebra", result.get(0).getName());
        Assertions.assertEquals("Tonica", result.get(1).getName());
        Assertions.assertEquals("Unidad de Limon", result.get(2).getName());




    }

}