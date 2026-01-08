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
        // Obtener todos los meseros activos
        List<UserEntity> waiters = userRepository.findByRoleAndActive(Role.WAITER);
        
        // Mapear a DTO con conteo de órdenes activas
        return waiters.stream()
                .map(waiter -> WaiterWithOrdersDTO.builder()
                        .username(waiter.getUsername())
                        .email(waiter.getEmail())
                        .activeOrdersCount(orderRepository.countByWaiterUserNameAndStatusIn(
                                waiter.getUsername(), 
                                ACTIVE_STATUSES
                        ))
                        .build())
                .sorted(Comparator.comparingInt(WaiterWithOrdersDTO::getActiveOrdersCount))
                .toList();
    }
}
