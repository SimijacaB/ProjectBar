package com.app.projectbar.application.implementation;

import com.app.projectbar.application.exception.ResourceNotFoundException;
import com.app.projectbar.application.interfaces.IBillReportService;
import com.app.projectbar.application.mapper.BillMapper;
import com.app.projectbar.domain.dto.bill.BillReportDTO;
import com.app.projectbar.infra.repositories.IBillRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BillReportServiceImpl implements IBillReportService {

    private static final Logger log = LoggerFactory.getLogger(BillReportServiceImpl.class);

    private final IBillRepository billRepository;
    private final BillMapper billMapper;

    @Override
    public byte[] generateBillPDF(Long billId) {
        var bill = billRepository.findById(billId)
                .orElseThrow(() -> {
                    log.error("Bill not found with id: {}", billId);
                    return new ResourceNotFoundException("Bill not found with id: " + billId);
                });

        BillReportDTO billDTO = billMapper.toReportDTO(bill);

        InputStream jasperStream = getClass().getResourceAsStream("/reports/templates/invoice.jasper");
        if (jasperStream == null) {
            throw new ResourceNotFoundException("Report template not found: /reports/templates/invoice.jasper");
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("billNumber", billDTO.getBillNumber());
        parameters.put("clientName", billDTO.getClientName());
        parameters.put("createdBy", billDTO.getCreatedBy());
        parameters.put("billingDate", billDTO.getBillingDate());
        parameters.put("totalAmount", billDTO.getTotalAmount());

        InputStream logoStream = getClass().getResourceAsStream("/static/images/logoEmpresa.jpg");
        if (logoStream == null) {
            log.warn("Logo image not found, proceeding without logo");
        }
        parameters.put("logoEmpresa", logoStream);

        JRBeanCollectionDataSource itemsDataSource = new JRBeanCollectionDataSource(billDTO.getItems());
        parameters.put("itemsDataSource", itemsDataSource);
        log.debug("DataSource created with {} items", billDTO.getItems().size());

        try {
            JasperPrint print = JasperFillManager.fillReport(jasperStream, parameters, new JREmptyDataSource());
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(print);
            log.debug("PDF generated successfully with {} bytes", pdfBytes.length);
            return pdfBytes;
        } catch (JRException e) {
            log.error("Error during report generation: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}
