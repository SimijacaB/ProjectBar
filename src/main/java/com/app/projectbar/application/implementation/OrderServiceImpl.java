package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ErrorMessagesService;
import com.app.projectbar.application.exception.auth.UserNotAuthenticatedException;
import com.app.projectbar.application.exception.inventory.InsufficientInventoryException;
import com.app.projectbar.application.exception.orders.*;
import com.app.projectbar.application.exception.product.ProductNotFoundException;
import com.app.projectbar.application.exception.table.TableNotFoundException;
import com.app.projectbar.application.interfaces.IInventoryService;
import com.app.projectbar.application.interfaces.IOrderNotificationService;
import com.app.projectbar.application.interfaces.IOrderService;
import com.app.projectbar.application.mapper.OrderMapper;
import com.app.projectbar.domain.*;
import com.app.projectbar.domain.dto.order.OrderForListResponseDTO;
import com.app.projectbar.domain.dto.order.OrderRequestDTO;
import com.app.projectbar.domain.dto.order.OrderResponseDTO;
import com.app.projectbar.domain.dto.order.UpdateOrderDTO;
import com.app.projectbar.domain.dto.orderItem.OrderItemRequestDTO;
import com.app.projectbar.domain.enums.OrderStatus;
import com.app.projectbar.infra.repositories.IOrderRepository;
import com.app.projectbar.infra.repositories.IOrderTableRepository;
import com.app.projectbar.infra.repositories.IProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.app.projectbar.domain.enums.OrderTableStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private static final String UNAUTHENTICATED_PRINCIPAL = "anonymousUser";

    private final IOrderRepository orderRepository;
    private final IProductRepository productRepository;
    private final IInventoryService inventoryService;
    private final IOrderTableRepository orderTableRepository;
    private final IOrderNotificationService notificationService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDTO save(OrderRequestDTO orderRequest) {

        validateOrderHasProducts(orderRequest);

        boolean isCustomerSelfServiceOrder = !isWaiterAuthenticated();
        OrderStatus initialStatus = isCustomerSelfServiceOrder ? OrderStatus.CREATED : OrderStatus.IN_PROGRESS;
        String assignedWaiter = isCustomerSelfServiceOrder ? null : getAuthenticatedWaiterUsername();

        OrderTable orderTable = getOrderTableOrThrow(orderRequest.getTableNumber());
        updateTableStatusIfFree(orderTable);

        Order newOrder = buildOrder(orderRequest, assignedWaiter, initialStatus);
        processOrderItems(orderRequest, newOrder);

        Order savedOrder = orderRepository.save(newOrder);

        // Notificar nueva orden a admin, meseros, bartender y chef
        notificationService.notifyNewOrder(savedOrder);

        log.info("Order created for table {} - Mode: {}, Status: {}",
                orderRequest.getTableNumber(),
                isCustomerSelfServiceOrder ? "Customer self-service (QR)" : "Waiter",
                initialStatus);

        return orderMapper.toResponseDTO(savedOrder);
    }

    @Override
    public List<OrderForListResponseDTO> findAll() {
        return orderMapper.toListDTOList(orderRepository.findAll());
    }

    @Override
    public Page<OrderForListResponseDTO> findAll(Pageable pageable) {
        return orderRepository.findAll(pageable).map(orderMapper::toListDTO);
    }

    @Override
    public OrderResponseDTO findById(Long id) {
        Order order = findOrderByIdOrThrow(id);
        return orderMapper.toResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrder(UpdateOrderDTO updateOrderDTO) {
        Order order = findOrderByIdOrThrow(updateOrderDTO.getId());

        order.setClientName(updateOrderDTO.getClientName());
        order.setTableNumber(updateOrderDTO.getTableNumber());
        order.setNotes(updateOrderDTO.getNotes());

        log.info("Order {} updated", updateOrderDTO.getId());
        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("Deleting order {}", id);
        orderRepository.deleteById(id);
    }

    @Override
    public List<OrderForListResponseDTO> findByClientName(String name) {
        return orderMapper.toListDTOList(orderRepository.findByClientName(name));
    }

    @Override
    public List<OrderForListResponseDTO> findByTableNumber(Integer tableNumber) {
        return orderMapper.toListDTOList(orderRepository.findByTableNumber(tableNumber));
    }

    @Override
    public List<OrderForListResponseDTO> findByWaiterId(String id) {
        return orderMapper.toListDTOList(orderRepository.findByWaiterUserName(id));
    }

    @Override
    public List<OrderForListResponseDTO> findMyOrders() {
        String waiterUsername = getAuthenticatedWaiterUsername();
        return orderMapper.toListDTOList(orderRepository.findByWaiterUserName(waiterUsername));
    }

    @Override
    public Page<OrderForListResponseDTO> findMyOrders(Pageable pageable) {
        String waiterUsername = getAuthenticatedWaiterUsername();
        return orderRepository.findByWaiterUserName(waiterUsername, pageable).map(orderMapper::toListDTO);
    }

    @Override
    public List<OrderForListResponseDTO> findMyOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String waiterUsername = getAuthenticatedWaiterUsername();
        List<Order> orders = orderRepository.findByWaiterUserNameAndDateBetween(waiterUsername, startDate, endDate);
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public Page<OrderForListResponseDTO> findMyOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        String waiterUsername = getAuthenticatedWaiterUsername();
        return orderRepository.findPagedByWaiterUserNameAndDateBetween(waiterUsername, startDate, endDate, pageable)
                .map(orderMapper::toListDTO);
    }

    @Override
    public List<OrderForListResponseDTO> findByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999999999);
        return orderMapper.toListDTOList(orderRepository.findByDateBetween(startOfDay, endOfDay));
    }

    @Override
    public List<OrderForListResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderMapper.toListDTOList(orderRepository.findByDateBetween(startDate, endDate));
    }

    @Override
    public Page<OrderForListResponseDTO> findByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return orderRepository.findPagedByDateBetween(startDate, endDate, pageable)
                .map(orderMapper::toListDTO);
    }

    @Override
    public List<OrderForListResponseDTO> findByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        if (orders.isEmpty()) {
            throw new OrdersNotFoundByStatusException(
                    ErrorMessagesService.ORDERS_NOT_FOUND_BY_STATUS_EXCEPTION.getMessage());
        }
        return orderMapper.toListDTOList(orders);
    }

    @Override
    public Map<String, List<OrderForListResponseDTO>> findPendingOrdersByTableGroupedByClient(Integer tableNumber) {
        List<Order> orders = orderRepository.findByTableNumberAndStatusNot(tableNumber, OrderStatus.BILLED);

        return orders.stream()
                .map(orderMapper::toListDTO)
                .collect(Collectors.groupingBy(OrderForListResponseDTO::getClientName));
    }

    @Override
    @Transactional
    public OrderResponseDTO addOrderItem(Long orderId, OrderItemRequestDTO orderItemToAdd) {
        Order order = findOrderByIdOrThrow(orderId);
        validateOrderCanBeModified(order);

        Product product = findProductByNameOrThrow(orderItemToAdd.getProductName());
        addOrUpdateOrderItem(order, product, orderItemToAdd.getQuantity());
        updateOrderTotalValue(order);

        log.info("Item added to order {}", orderId);
        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponseDTO removeOrderItem(Long id, Long idOrderItem, Integer quantityToRemove) {
        Order order = findOrderByIdOrThrow(id);

        OrderItem orderItem = findOrderItemOrThrow(order, idOrderItem);
        removeOrReduceQuantity(order, orderItem, quantityToRemove);
        updateOrderTotalValue(order);

        log.info("Item {} removed/reduced from order {}", idOrderItem, id);
        return orderMapper.toResponseDTO(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponseDTO changeStatus(Long id, String newStatus) {
        Order order = findOrderByIdOrThrow(id);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus targetStatus = OrderStatus.valueOf(newStatus);

        validateStatusTransition(order, targetStatus);
        order.setStatus(targetStatus);

        Order savedOrder = orderRepository.save(order);

        // Notificar cambio de estado
        if (targetStatus == OrderStatus.CANCELLED) {
            notificationService.notifyOrderCancelled(savedOrder);
        } else {
            notificationService.notifyOrderStatusChanged(savedOrder, oldStatus, targetStatus);
        }

        log.info("Order {} status changed from {} to {}", id, oldStatus, targetStatus);
        return orderMapper.toResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO assignWaiter(Long orderId, String waiterUsername) {
        Order order = findOrderByIdOrThrow(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStatusTransitionException(
                    String.format(ErrorMessagesService.ONLY_CREATED_ORDERS_CAN_BE_ASSIGNED.getMessage(),
                            order.getStatus()));
        }

        order.setWaiterUserName(waiterUsername);
        order.setStatus(OrderStatus.ASSIGNED);

        Order savedOrder = orderRepository.save(order);

        // Notificar que la orden fue asignada a un mesero específico
        notificationService.notifyOrderAssigned(savedOrder, waiterUsername);

        log.info("Waiter {} assigned to order {}", waiterUsername, orderId);
        return orderMapper.toResponseDTO(savedOrder);
    }

    @Override
    public List<OrderForListResponseDTO> findUnassignedOrders() {
        return orderMapper.toListDTOList(orderRepository.findByStatus(OrderStatus.CREATED));
    }

    @Override
    public List<OrderForListResponseDTO> findMyAssignedOrders() {
        String waiterUsername = getAuthenticatedWaiterUsername();
        List<Order> orders = orderRepository.findByWaiterUserNameAndStatus(waiterUsername, OrderStatus.ASSIGNED);
        return orderMapper.toListDTOList(orders);
    }

    /**
     * Validates that orders can be billed.
     * An order can be billed ONLY if it's in DELIVERED status and hasn't been
     * billed before.
     */
    public void validateIfOrderCanBeBilled(List<Order> orders) {
        Set<Long> notDeliveredOrderIds = new HashSet<>();
        Set<Long> alreadyBilledOrderIds = new HashSet<>();

        for (Order order : orders) {
            if (!OrderStatus.DELIVERED.equals(order.getStatus())) {
                notDeliveredOrderIds.add(order.getId());
            }
            if (order.getBill() != null) {
                alreadyBilledOrderIds.add(order.getId());
            }
        }

        if (!notDeliveredOrderIds.isEmpty()) {
            String ids = notDeliveredOrderIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new InvalidOrderStatusTransitionException(
                    String.format(ErrorMessagesService.CANNOT_BILL_NOT_DELIVERED_ORDERS.getMessage(), ids));
        }

        if (!alreadyBilledOrderIds.isEmpty()) {
            String ids = alreadyBilledOrderIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new OrdersAlreadyBilledException(
                    ErrorMessagesService.ORDER_ALREADY_BILLED_EXCEPTION.getMessage() + ids);
        }
    }

    /**
     * Marks orders as BILLED.
     * IMPORTANT: This method should only be called after validating that orders are
     * in DELIVERED status.
     */
    @Transactional
    public void setOrdersAsBilled(List<Order> orders) {
        orders.stream()
                .filter(order -> OrderStatus.DELIVERED.equals(order.getStatus()))
                .forEach(order -> order.setStatus(OrderStatus.BILLED));
        orderRepository.saveAll(orders);
        log.info("{} orders marked as BILLED", orders.size());
    }

    public List<Order> getExistingOrdersOrThrow(List<Long> orderIds) {
        List<Order> foundOrders = orderRepository.findAllById(orderIds);

        if (foundOrders.size() == orderIds.size()) {
            return foundOrders;
        }

        Set<Long> foundIds = foundOrders.stream()
                .map(Order::getId)
                .collect(Collectors.toSet());

        List<Long> notFoundIds = orderIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!notFoundIds.isEmpty()) {
            throw new OrderNotFoundByIdException(
                    String.format(ErrorMessagesService.ORDER_IDS_NOT_EXIST.getMessage(), notFoundIds));
        }

        return foundOrders;
    }

    // ==================== Private Helper Methods ====================

    private void validateOrderHasProducts(OrderRequestDTO orderRequest) {
        if (orderRequest.getOrderProducts() == null || orderRequest.getOrderProducts().isEmpty()) {
            throw new OrderMustHaveProductsException(ErrorMessagesService.ORDER_MUST_HAVE_PRODUCTS.getMessage());
        }
    }

    /**
     * Checks if the current request is made by an authenticated waiter.
     * Returns false for customer self-service orders (QR code orders).
     */
    private boolean isWaiterAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null
                    && authentication.isAuthenticated()
                    && !UNAUTHENTICATED_PRINCIPAL.equals(authentication.getPrincipal());
        } catch (Exception e) {
            log.debug("No authentication context available - treating as customer self-service order");
            return false;
        }
    }

    /**
     * Gets the username of the authenticated waiter.
     * 
     * @throws UserNotAuthenticatedException if no waiter is authenticated
     */
    private String getAuthenticatedWaiterUsername() {
        if (!isWaiterAuthenticated()) {
            throw new UserNotAuthenticatedException(ErrorMessagesService.USER_NOT_AUTHENTICATED.getMessage());
        }
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private OrderTable getOrderTableOrThrow(Integer tableNumber) {
        return orderTableRepository.findByNumber(tableNumber)
                .orElseThrow(() -> new TableNotFoundException(
                        String.format(ErrorMessagesService.TABLE_NOT_FOUND_BY_NUMBER.getMessage(), tableNumber)));
    }

    private void updateTableStatusIfFree(OrderTable orderTable) {
        if (orderTable.getStatus() == FREE) {
            orderTable.setStatus(OCCUPIED);
            orderTableRepository.save(orderTable);
        }
    }

    private Order buildOrder(OrderRequestDTO orderRequest, String waiterId, OrderStatus initialStatus) {
        return Order.builder()
                .clientName(orderRequest.getClientName())
                .tableNumber(orderRequest.getTableNumber())
                .notes(orderRequest.getNotes())
                .waiterUserName(waiterId)
                .status(initialStatus)
                .orderItems(new ArrayList<>())
                .valueToPay(0.0)
                .build();
    }

    private void processOrderItems(OrderRequestDTO orderRequest, Order newOrder) {
        if (orderRequest.getOrderProducts() == null || orderRequest.getOrderProducts().isEmpty()) {
            return;
        }

        double totalValue = 0.0;

        for (OrderItemRequestDTO itemRequest : orderRequest.getOrderProducts()) {
            Product product = findProductByIdOrName(itemRequest);
            deductIngredientsFromInventory(product, itemRequest.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .order(newOrder)
                    .build();

            newOrder.getOrderItems().add(orderItem);
            totalValue += product.getPrice() * itemRequest.getQuantity();
        }

        newOrder.setValueToPay(totalValue);
    }

    private Product findProductByIdOrName(OrderItemRequestDTO itemRequest) {
        if (itemRequest.getIdProduct() != null) {
            return productRepository.findById(itemRequest.getIdProduct())
                    .orElseThrow(() -> new ProductNotFoundException(
                            String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_ID.getMessage(),
                                    itemRequest.getIdProduct())));
        }
        if (itemRequest.getProductName() != null) {
            return findProductByNameOrThrow(itemRequest.getProductName());
        }
        throw new ProductNotFoundException(ErrorMessagesService.PRODUCT_ID_OR_NAME_REQUIRED.getMessage());
    }

    private Product findProductByNameOrThrow(String productName) {
        return productRepository.findOneByName(productName)
                .orElseThrow(() -> new ProductNotFoundException(
                        String.format(ErrorMessagesService.PRODUCT_NOT_FOUND_BY_NAME.getMessage(), productName)));
    }

    private Order findOrderByIdOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundByIdException(
                        ErrorMessagesService.ORDER_NOT_FOUND_BY_ID_EXCEPTION.getMessage()));
    }

    private OrderItem findOrderItemOrThrow(Order order, Long orderItemId) {
        return order.getOrderItems().stream()
                .filter(item -> item.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(
                        String.format(ErrorMessagesService.ORDER_ITEM_NOT_FOUND.getMessage(), orderItemId)));
    }

    private void validateOrderCanBeModified(Order order) {
        if (OrderStatus.DELIVERED.equals(order.getStatus())) {
            throw new CannotModifyDeliveredOrderException(
                    ErrorMessagesService.CANNOT_ADD_ITEMS_TO_DELIVERED_ORDER.getMessage());
        }
    }

    private void addOrUpdateOrderItem(Order order, Product product, Integer quantity) {
        Optional<OrderItem> existingItem = order.getOrderItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice());
        } else {
            OrderItem newOrderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .quantity(quantity)
                    .price(product.getPrice())
                    .order(order)
                    .build();
            order.getOrderItems().add(newOrderItem);
        }
    }

    private void removeOrReduceQuantity(Order order, OrderItem orderItem, Integer quantityToRemove) {
        if (quantityToRemove != null && quantityToRemove < orderItem.getQuantity()) {
            orderItem.setQuantity(orderItem.getQuantity() - quantityToRemove);
        } else {
            order.getOrderItems().remove(orderItem);
        }
    }

    /**
     * Deducts from inventory based on product type:
     * - If the product has ingredients (isPrepared = true): deducts ingredients
     * - If the product has NO ingredients (isPrepared = false or null): deducts the
     * product directly
     */
    private void deductIngredientsFromInventory(Product product, Integer quantity) {
        if (isProductPrepared(product)) {
            deductIngredients(product, quantity);
        } else {
            deductProductDirectly(product, quantity);
        }
    }

    private boolean isProductPrepared(Product product) {
        return product.getIsPrepared() != null && product.getIsPrepared()
                && product.getProductIngredients() != null && !product.getProductIngredients().isEmpty();
    }

    private void deductIngredients(Product product, Integer quantity) {
        for (var productIngredient : product.getProductIngredients()) {
            String ingredientCode = productIngredient.getIngredient().getCode();
            int amountToDeduct = (int) Math.ceil(productIngredient.getAmount() * quantity);

            try {
                inventoryService.deductStock(amountToDeduct, ingredientCode);
            } catch (RuntimeException e) {
                throw new InsufficientInventoryException(
                        String.format(ErrorMessagesService.INSUFFICIENT_INGREDIENT_INVENTORY.getMessage(),
                                productIngredient.getIngredient().getName(),
                                product.getName(),
                                e.getMessage()));
            }
        }
    }

    private void deductProductDirectly(Product product, Integer quantity) {
        try {
            inventoryService.deductStock(quantity, product.getCode());
        } catch (RuntimeException e) {
            throw new InsufficientInventoryException(
                    String.format(ErrorMessagesService.INSUFFICIENT_PRODUCT_INVENTORY.getMessage(),
                            product.getName(),
                            e.getMessage()));
        }
    }

    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getStatus();

        if (newStatus == OrderStatus.IN_PROGRESS && order.getWaiterUserName() == null) {
            throw new InvalidOrderStatusTransitionException(
                    ErrorMessagesService.WAITER_REQUIRED_FOR_IN_PROGRESS.getMessage());
        }

        if (currentStatus == OrderStatus.CREATED) {
            if (newStatus != OrderStatus.ASSIGNED && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusTransitionException(
                        ErrorMessagesService.CREATED_ORDER_CAN_ONLY_BE_ASSIGNED_OR_CANCELLED.getMessage());
            }
        }

        if (currentStatus == OrderStatus.ASSIGNED) {
            if (newStatus != OrderStatus.IN_PROGRESS && newStatus != OrderStatus.CANCELLED) {
                throw new InvalidOrderStatusTransitionException(
                        ErrorMessagesService.ASSIGNED_ORDER_CAN_ONLY_MOVE_TO_IN_PROGRESS_OR_CANCELLED.getMessage());
            }
        }
    }

    private void updateOrderTotalValue(Order order) {
        double total = order.getOrderItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getProduct().getPrice())
                .sum();
        order.setValueToPay(total);
    }

}
