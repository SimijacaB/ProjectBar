package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IBillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String number);


}
