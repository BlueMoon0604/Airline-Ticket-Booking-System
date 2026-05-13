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
import java.util.List;

@Service
public class PdfTicketService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final QrCodeService qrCodeService;

    public PdfTicketService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] generateETicket(Booking booking,
                                  FlightResponse flight,
                                  List<PassengerResponseDto> passengers) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            document.add(new Paragraph("SkyBooker E-Ticket")
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Booking Confirmed")
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY)
                    .setMarginBottom(20));

            Table bookingTable = new Table(2);
            bookingTable.useAllAvailableWidth();

            addRow(bookingTable, "Booking ID", booking.getBookingId().toString());
            addRow(bookingTable, "PNR", booking.getPnrCode());
            addRow(bookingTable, "Trip Type", booking.getTripType().name());
            addRow(bookingTable, "Status", booking.getStatus().name());
            addRow(bookingTable, "Contact Email", booking.getContactEmail());
            addRow(bookingTable, "Contact Phone", booking.getContactPhone());
            addRow(bookingTable, "Meal Preference", booking.getMealPreference());
            addRow(bookingTable, "Luggage", booking.getLuggageKg() + " kg");
            addRow(bookingTable, "Total Fare", booking.getTotalFare().toString());

            document.add(bookingTable);
            document.add(new Paragraph(" ").setMarginBottom(10));

            Table flightTable = new Table(2);
            flightTable.useAllAvailableWidth();

            addRow(flightTable, "Flight Number", flight.flightNumber());
            addRow(flightTable, "Route", flight.originAirportCode() + " -> " + flight.destinationAirportCode());
            addRow(flightTable, "Departure", format(flight.departureTime()));
            addRow(flightTable, "Arrival", format(flight.arrivalTime()));
            addRow(flightTable, "Aircraft", flight.aircraftType());
            addRow(flightTable, "Status", flight.status());

            document.add(new Paragraph("Flight Details").setFontSize(14));
            document.add(flightTable);
            document.add(new Paragraph(" ").setMarginBottom(10));

            document.add(new Paragraph("Passenger Details").setFontSize(14));

            Table passengerTable = new Table(5);
            passengerTable.useAllAvailableWidth();
            passengerTable.addHeaderCell("Name");
            passengerTable.addHeaderCell("Passenger Type");
            passengerTable.addHeaderCell("Seat");
            passengerTable.addHeaderCell("Ticket Number");
            passengerTable.addHeaderCell("Passport");

            for (PassengerResponseDto passenger : passengers) {
                passengerTable.addCell(passenger.title() + " " + passenger.firstName() + " " + passenger.lastName());
                passengerTable.addCell(passenger.passengerType());
                passengerTable.addCell(passenger.seatNumber() == null ? "-" : passenger.seatNumber());
                passengerTable.addCell(passenger.ticketNumber() == null ? "-" : passenger.ticketNumber());
                passengerTable.addCell(passenger.passportNumber() == null ? "-" : passenger.passportNumber());
            }

            document.add(passengerTable);
            document.add(new Paragraph(" ").setMarginBottom(12));

            String qrText = "PNR:" + booking.getPnrCode()
                    + "|BOOKING:" + booking.getBookingId()
                    + "|FLIGHT:" + flight.flightNumber();

            byte[] qrBytes = qrCodeService.generateQrCode(qrText, 180, 180);
            Image qrImage = new Image(ImageDataFactory.create(qrBytes));
            qrImage.setAutoScale(false);
            qrImage.setWidth(120);
            qrImage.setHeight(120);

            document.add(new Paragraph("Ticket QR").setFontSize(14).setTextAlignment(TextAlignment.CENTER));
            document.add(qrImage.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER));

            document.add(new Paragraph("Please carry a valid photo ID at the airport.")
                    .setMarginTop(15)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate e-ticket PDF", ex);
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
