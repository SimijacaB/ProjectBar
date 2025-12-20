package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository <Product, Long>{

    @Query("""
    SELECT p
    FROM Product p
    LEFT JOIN FETCH p.productIngredients pi
    LEFT JOIN FETCH pi.ingredient
    WHERE p.code = :code
    """)
    Optional<Product> findByCode(@Param("code")String code);

    @Query("SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findOneByName(@Param("name")String name);


    List<Product> findByName(String name);

    List<Product> findByCategory(Category category);

    @Query("""
    SELECT p
    FROM Product p
    WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<Product> findByNameContaining(@Param("name") String name);


}
