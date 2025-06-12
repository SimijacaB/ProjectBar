package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IIngredientRepository extends JpaRepository<Ingredient, Long> {

        Optional<Ingredient> findByCode(String code);

        void deleteByCode(String code);
}
