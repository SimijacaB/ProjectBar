package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IInventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByCode(String code);

    void deleteByCode(String code);
}
