package com.app.projectbar.infra.repositories;


import com.app.projectbar.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderRepository extends JpaRepository<Order, Long> {
}
