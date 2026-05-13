package com.skybooker.booking.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.skybooker.booking.client.dto.FlightResponse;
import com.skybooker.booking.client.dto.PassengerResponseDto;
import com.skybooker.booking.entity.Booking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfBoardingPassService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final QrCodeService qrCodeService;

    public PdfBoardingPassService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] generateBoardingPass(Booking booking,
                                       FlightResponse flight,
                                       PassengerResponseDto passenger) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("SkyBooker Boarding Pass")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Passenger Boarding Document")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            Table table = new Table(2);
            table.useAllAvailableWidth();

            addRow(table, "Passenger", passenger.title() + " " + passenger.firstName() + " " + passenger.lastName());
            addRow(table, "PNR", booking.getPnrCode());
            addRow(table, "Flight Number", flight.flightNumber());
            addRow(table, "Route", flight.originAirportCode() + " -> " + flight.destinationAirportCode());
            addRow(table, "Departure", format(flight.departureTime()));
            addRow(table, "Arrival", format(flight.arrivalTime()));
            addRow(table, "Seat", passenger.seatNumber() == null ? "-" : passenger.seatNumber());
            addRow(table, "Ticket Number", passenger.ticketNumber() == null ? "-" : passenger.ticketNumber());
            addRow(table, "Passenger Type", passenger.passengerType());
            addRow(table, "Booking Status", booking.getStatus().name());

            document.add(table);
            document.add(new Paragraph(" ").setMarginBottom(12));

            String qrText = "PNR:" + booking.getPnrCode()
                    + "|PASSENGER:" + passenger.passengerId()
                    + "|TICKET:" + passenger.ticketNumber()
                    + "|SEAT:" + passenger.seatNumber();

            byte[] qrBytes = qrCodeService.generateQrCode(qrText, 200, 200);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            qrImage.setWidth(130);
            qrImage.setHeight(130);

            document.add(new Paragraph("Boarding QR")
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER));

            document.add(new Paragraph("Please report at the boarding gate 45 minutes before departure.")
                    .setMarginTop(15)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate boarding pass PDF", ex);
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(label);
        table.addCell(value == null || value.isBlank() ? "-" : value);
    }

    private String format(java.time.LocalDateTime value) {
        return value == null ? "-" : value.format(FORMATTER);
    }
}
