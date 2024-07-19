package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository <Product, Long>{

    Optional<Product> findByCode(String code);

    Optional<List<Product>> findByName(String name);

    Optional<List<Product>> findByCategory(String category);

}
