package com.skybooker.booking.service;

import com.skybooker.booking.dto.AddOnRequest;
import com.skybooker.booking.dto.BoardingPassResponse;
import com.skybooker.booking.dto.BookingResponse;
import com.skybooker.booking.dto.CreateBookingRequest;
import com.skybooker.booking.dto.FareSummaryResponse;
import com.skybooker.booking.dto.TicketResponse;
import com.skybooker.booking.dto.UpdateBookingStatusRequest;
import com.skybooker.booking.entity.BookingStatus;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(UUID bookingId);

    BookingResponse getBookingByPnr(String pnrCode);

    List<BookingResponse> getBookingsByUser(UUID userId);

    List<BookingResponse> getBookingsByFlight(UUID flightId);

    List<BookingResponse> getUpcomingBookings(UUID userId);

    BookingResponse cancelBooking(UUID bookingId);

    BookingResponse updateStatus(UUID bookingId, UpdateBookingStatusRequest request);

    FareSummaryResponse calculateFare(UUID flightId, Integer passengerCount, Integer luggageKg);

    BookingResponse addAddOn(UUID bookingId, AddOnRequest request);

    List<BookingResponse> getBookingsByStatus(BookingStatus status);

    TicketResponse generateETicket(UUID bookingId);

    BoardingPassResponse generateBoardingPass(UUID bookingId, UUID passengerId);
}
