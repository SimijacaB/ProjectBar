package com.app.projectbar.domain.dto.bill;

import com.app.projectbar.domain.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillDTO {

    private String clientName;

    private LocalDateTime billingDate;

    private List<OrderItem> items;

    private Long billNumber;

    private Double totalAmount;

    private String createdBy;
}
