package com.app.projectbar.application.impl;

import com.app.projectbar.application.IOrderService;
import com.app.projectbar.domain.*;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.infra.repositories.IInventoryRepository;
import com.app.projectbar.infra.repositories.IOrderItemRepository;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final IProductRepository productRepository;
    private final IInventoryRepository inventoryRepository;


    @Override
    public OrderResponseDTO save(OrderRequestDTO orderRequest) {
        Order order = Order.builder()
                .clientName(orderRequest.getClientName())
                .tableNumber(orderRequest.getTableNumber())
                .date(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .notes(orderRequest.getNotes())
                .build();
        orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public List<OrderForListResponseDTO> findAll() {
        var orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public OrderResponseDTO findById(Long id) {
        return modelMapper.map(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order with id " + id + " not found")), OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO) {
        var order = orderRepository.findById(updateOrderDTO.getId())
                .orElseThrow(() -> new RuntimeException("Order with id " + updateOrderDTO.getId() + " not found"));

        order.setClientName(updateOrderDTO.getClientName());
        order.setTableNumber(updateOrderDTO.getTableNumber());
        order.setNotes(updateOrderDTO.getNotes());

        orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderForListResponseDTO> findByClientName(String name) {
        List<Order> orders = orderRepository.findByClientName(name);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber) {
        List<Order> orders = orderRepository.findByTableNumber(tableNumber);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByWaiterId(String id) {
        List<Order> orders = orderRepository.findByWaiterId(id);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {
        return null;
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream().map(order -> modelMapper.map(order, OrderForListResponseDTO.class)).toList();
    }

    @Override
    public OrderResponseDTO addOrderItem(Long id, OrderItemRequestDTO orderItemToAdd) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + " id"));

        OrderItem orderItem = OrderItem.builder()
                .productName(orderItemToAdd.getProductName())
                .quantity(orderItemToAdd.getQuantity())
                .order(order)
                .build();

        OrderItem orderItemExists = null;


        for (OrderItem oi : order.getOrderProducts()) {
            if (oi.getProductName().equalsIgnoreCase(orderItem.getProductName())) {
                orderItemExists = oi;
                break;
            }
        }

        if (orderItemExists != null) {
            orderItemExists.setQuantity(orderItemToAdd.getQuantity() + orderItemExists.getQuantity());
            orderItemRepository.save(orderItemExists);

        } else {
            order.getOrderProducts().add(orderItem);
            orderItemRepository.save(orderItem);
        }

        double total = order.getOrderProducts().stream()
                .mapToDouble(item -> item.getQuantity() * productRepository.findByName(item.getProductName()).get().stream().findFirst().get().getPrice())
                .sum();


        order.setValueToPay(total);
        
        order.setStatus(OrderStatus.PENDING);

        order = orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public OrderResponseDTO removeOrderItem(Long id, Long idOrderItem) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();

        OrderItem orderItem = order.getOrderProducts()
                .stream()
                .filter(findOrderItem -> findOrderItem.getId() == idOrderItem)
                .findFirst().get();


        order.getOrderProducts().remove(modelMapper.map(orderItem, OrderItem.class));
        order.setOrderProducts(order.getOrderProducts());

        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);

    }

    @Override
    public OrderResponseDTO changeStatus(Long id, String newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(id);
        if (orderOptional.isEmpty()) {
            throw new RuntimeException("Order with id " + id + " not found");
        }
        Order order = orderOptional.get();

        //Solo entra aquí si el estado a cambiar  a ready(Cambiar la opcion de estado), es decir una vez esa
        // orden este en ese estado va a tomar todos los orderItem y va a inventory y  descontar esas cantidades.
        if (newStatus.equals(OrderStatus.READY.toString())) {
            for (OrderItem item : order.getOrderProducts()) {
                deductInventory(item);
            }
        }

        order.setStatus(OrderStatus.valueOf(newStatus));
        orderRepository.save(order);

        return modelMapper.map(orderRepository.save(order), OrderResponseDTO.class);
    }

    private void deductInventory(OrderItem orderItem) {

        String code = productRepository.findByName(orderItem.getProductName())
                .stream().findFirst().get().get(0).getCode();

        Product product = productRepository.findByCode(code).get();
        if (!product.getIsPrepared()) {
            Optional<Inventory> inventory = inventoryRepository.findByCode(code);

            if (inventory.isEmpty()) {
                throw new RuntimeException("Product with id " + code + " not found");
            }

            inventory.get().setQuantity(inventory.get().getQuantity() - orderItem.getQuantity());
            inventoryRepository.save(inventory.get());
        } else {
            for (ProductIngredient productIngredient : product.getProductIngredients()) {
                Optional<Inventory> inventory = inventoryRepository.findByCode(productIngredient.getIngredient().getCode());

                if (inventory.isEmpty()) {
                    throw new RuntimeException("Ingredient with id " + code + " not found");
                }
                inventory.get().setQuantity((int) (inventory.get().getQuantity() - (orderItem.getQuantity() * productIngredient.getAmount())));
                inventoryRepository.save(inventory.get());
            }

        }


    }


}
