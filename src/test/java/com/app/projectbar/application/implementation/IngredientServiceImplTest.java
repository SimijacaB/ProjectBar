package com.app.projectbar.application.implementation;

import com.app.projectbar.domain.Ingredient;
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
        Ingredient ingredient = new Ingredient(1L, "G01-ML-0001I", "Ginebra", UnitOfMeasure.ML, null);

        when(this.ingredientRepository.findById( anyLong() )).thenReturn(Optional.of(ingredient));

        //When
        Ingredient result = modelMapper.map(ingredientService.findById(1L), Ingredient.class);
        
        //Then
        Assertions.assertEquals("Ginebra", result.getName());
        Assertions.assertEquals("G01-ML-0001I", result.getCode());
        Assertions.assertEquals(UnitOfMeasure.ML, result.getUnitOfMeasure());
    }




}