package com.app.projectbar.infra.repositories;


import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientName(String clientName);

    List<Order> findByTableNumber(Integer tableNumber);

    List<Order> findByWaiterUserName(String username);

    List<Order> findByStatus(OrderStatus status);


}
