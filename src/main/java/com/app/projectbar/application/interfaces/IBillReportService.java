package com.app.projectbar.application.interfaces;

public interface IBillReportService {

    byte[] generateBillPDF(Long billId);
}
