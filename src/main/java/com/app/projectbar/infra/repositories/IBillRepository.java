package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IBillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String number);

    @Query(value = "SELECT b FROM Bill b WHERE " +
            "(:clientName IS NULL OR LOWER(b.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))) AND " +
            "(:startDate IS NULL OR b.billingDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.billingDate <= :endDate)", countQuery = "SELECT COUNT(b) FROM Bill b WHERE " +
                    "(:clientName IS NULL OR LOWER(b.clientName) LIKE LOWER(CONCAT('%', :clientName, '%'))) AND " +
                    "(:startDate IS NULL OR b.billingDate >= :startDate) AND " +
                    "(:endDate IS NULL OR b.billingDate <= :endDate)")
    Page<Bill> findByFilters(
            @Param("clientName") String clientName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

}
