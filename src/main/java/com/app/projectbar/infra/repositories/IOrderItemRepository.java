package com.app.projectbar.infra.repositories;

import com.app.projectbar.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IOrderItemRepository extends JpaRepository<OrderItem, Long> {}
