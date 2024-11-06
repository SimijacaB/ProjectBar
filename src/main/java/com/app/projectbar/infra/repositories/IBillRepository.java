package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IBillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByNumber(Long number);


}
