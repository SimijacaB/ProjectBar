package com.app.projectbar.domain.dto.bill;

import com.app.projectbar.domain.OrderItem;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime billingDate;

    private List<OrderItem> items;

    private String billNumber;

    private Double totalAmount;

    private String createdBy;
}
