package com.app.projectbar.application.implementation;

import com.app.projectbar.application.interfaces.IWaiterService;
import com.app.projectbar.domain.UserEntity;
import com.app.projectbar.domain.dto.waiter.WaiterWithOrdersDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.domain.enums.Role;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaiterServiceImpl implements IWaiterService {

    private final IUserRepository userRepository;
    private final IOrderRepository orderRepository;
    
    // Estados de órdenes que se consideran "activas" para un mesero
    private static final List<OrderStatus> ACTIVE_STATUSES = List.of(
            OrderStatus.ASSIGNED,
            OrderStatus.IN_PROGRESS,
            OrderStatus.READY
    );

    @Override
    public List<WaiterWithOrdersDTO> findAllWaitersWithActiveOrders() {
        // Obtener TODOS los meseros (activos e inactivos) para que el admin pueda elegir
        List<UserEntity> waiters = userRepository.findAllByRole(Role.WAITER);
        
        // Mapear a DTO con conteo de órdenes activas
        // Ordenar: primero los activos con menos órdenes, luego los inactivos
        return waiters.stream()
                .map(waiter -> {
                    boolean isActive = !waiter.getDisabled() && !waiter.getLocked();
                    return WaiterWithOrdersDTO.builder()
                            .username(waiter.getUsername())
                            .email(waiter.getEmail())
                            .activeOrdersCount(orderRepository.countByWaiterUserNameAndStatusIn(
                                    waiter.getUsername(), 
                                    ACTIVE_STATUSES
                            ))
                            .isActive(isActive)
                            .build();
                })
                .sorted(Comparator
                        .comparing(WaiterWithOrdersDTO::isActive).reversed() // Activos primero
                        .thenComparingInt(WaiterWithOrdersDTO::getActiveOrdersCount)) // Luego por menos órdenes
                .toList();
    }
}
