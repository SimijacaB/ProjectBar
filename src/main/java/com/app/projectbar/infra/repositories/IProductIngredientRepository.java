package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IProductIngredientRepository extends JpaRepository<ProductIngredient, Long>{
    void deleteByProductId(Long productId);

}
