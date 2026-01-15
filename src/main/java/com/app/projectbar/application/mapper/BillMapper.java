package com.app.projectbar.application.mapper;

import com.app.projectbar.domain.Bill;
import com.app.projectbar.domain.Order;
import com.app.projectbar.domain.OrderItem;
import com.app.projectbar.domain.dto.bill.BillDTO;
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BillMapper {

    @Mapping(target = "items", source = "orders", qualifiedByName = "ordersToItems")
    BillDTO toDTO(Bill bill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Bill toEntity(BillDTO dto);

    List<BillDTO> toDTOList(List<Bill> bills);

    @Named("ordersToItems")
    default List<OrderItem> ordersToItems(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }
        List<OrderItem> items = new ArrayList<>();
        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                items.addAll(order.getOrderItems());
            }
        }
        return items;
    }

    BillReportDTO toReportDTO(Bill bill);
}

