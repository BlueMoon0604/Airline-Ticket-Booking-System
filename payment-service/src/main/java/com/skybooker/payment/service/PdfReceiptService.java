package com.skybooker.payment.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.skybooker.payment.entity.Payment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public byte[] generateReceiptPdf(Payment payment) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("SkyBooker Payment Receipt")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Payment confirmation document")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            Table table = new Table(2);
            table.useAllAvailableWidth();

            addRow(table, "Payment ID", payment.getPaymentId().toString());
            addRow(table, "Booking ID", payment.getBookingId().toString());
            addRow(table, "User ID", payment.getUserId().toString());
            addRow(table, "Amount", payment.getCurrency() + " " + payment.getAmount());
            addRow(table, "Status", payment.getStatus().name());
            addRow(table, "Payment Mode", payment.getPaymentMode().name());
            addRow(table, "Gateway", safe(payment.getGatewayName()));
            addRow(table, "Order ID", safe(payment.getGatewayOrderId()));
            addRow(table, "Payment ID (Gateway)", safe(payment.getTransactionId()));
            addRow(table, "Paid At", format(payment.getPaidAt()));
            addRow(table, "Refunded At", format(payment.getRefundedAt()));
            addRow(table, "Refund Amount", payment.getRefundAmount() == null ? "-" : payment.getRefundAmount().toString());

            document.add(table);
            document.add(new Paragraph("Thank you for choosing SkyBooker.")
                    .setMarginTop(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate receipt PDF", ex);
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label)));
        table.addCell(new Cell().add(new Paragraph(value == null || value.isBlank() ? "-" : value)));
    }

    private String format(java.time.LocalDateTime value) {
        return value == null ? "-" : value.format(FORMATTER);
    }

    private String safe(String value) {
        return value == null ? "-" : value;
    }
}
