package com.app.projectbar.domain.dto.bill;

import com.app.projectbar.domain.dto.orderItem.ItemReportDTO;
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
public class BillReportDTO {
    private Long id;
    private String clientName;
    private LocalDateTime billingDate;
    private String billNumber;
    private Double totalAmount;
    private String createdBy;
    private List<ItemReportDTO> items;
}
