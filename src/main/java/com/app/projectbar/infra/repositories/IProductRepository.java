package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.enums.Category;
import com.app.projectbar.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository <Product, Long>{

    Optional<Product> findByCode(String code);

    @Query("SELECT p FROM Product p WHERE p.name = :name")
    Optional<Product> findOneByName(String name);


    Optional<List<Product>> findByName(String name);

    Optional<List<Product>> findByCategory(Category category);

}
