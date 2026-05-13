package com.skybooker.booking.service;

import com.skybooker.booking.client.FlightClient;
import com.skybooker.booking.client.NotificationClient;
import com.skybooker.booking.client.PassengerClient;
import com.skybooker.booking.client.PaymentClient;
import com.skybooker.booking.client.SeatClient;
import com.skybooker.booking.client.dto.BookingConfirmationRequestDto;
import com.skybooker.booking.client.dto.FlightResponse;
import com.skybooker.booking.client.dto.PassengerResponseDto;
import com.skybooker.booking.client.dto.PaymentResponseDto;
import com.skybooker.booking.client.dto.SeatResponseDto;
import com.skybooker.booking.dto.UpdateBookingStatusRequest;
import com.skybooker.booking.entity.Booking;
import com.skybooker.booking.entity.BookingStatus;
import com.skybooker.booking.entity.TripType;
import com.skybooker.booking.exception.BadRequestException;
import com.skybooker.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightClient flightClient;
    @Mock private SeatClient seatClient;
    @Mock private PassengerClient passengerClient;
    @Mock private PaymentClient paymentClient;
    @Mock private NotificationClient notificationClient;
    @Mock private PdfTicketService pdfTicketService;
    @Mock private PdfBoardingPassService pdfBoardingPassService;

    private BookingServiceImpl bookingService;

    private UUID bookingId;
    private UUID seatId;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                flightClient,
                seatClient,
                passengerClient,
                paymentClient,
                notificationClient,
                pdfTicketService,
                pdfBoardingPassService,
                BigDecimal.valueOf(0.18),
                BigDecimal.valueOf(0.05),
                BigDecimal.valueOf(100)
        );

        bookingId = UUID.randomUUID();
        seatId = UUID.randomUUID();

        booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(UUID.randomUUID());
        booking.setFlightId(UUID.randomUUID());
        booking.setPnrCode("ABC123");
        booking.setTripType(TripType.ONE_WAY);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalFare(BigDecimal.valueOf(6200));
        booking.setContactEmail("user@example.com");
        booking.setContactPhone("9876543210");
    }

    @Test
    void updateStatusShouldConfirmBookingWhenPaymentIsPaidAndSeatIsHeld() {
        PassengerResponseDto passenger = new PassengerResponseDto(
                UUID.randomUUID(),
                bookingId,
                "Ms",
                "Ananya",
                "Sharma",
                "2000-01-01",
                "FEMALE",
                "P1234567",
                "INDIAN",
                "2035-12-31",
                seatId,
                "15C",
                "TKT123",
                "ADULT"
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentClient.getPaymentByBooking(bookingId)).thenReturn(new PaymentResponseDto(
                UUID.randomUUID(), bookingId, booking.getUserId(), booking.getTotalFare(), "INR",
                "PAID", "UPI", null, null, null, null, null, null
        ));
        when(passengerClient.getPassengersByBooking(bookingId)).thenReturn(List.of(passenger));
        when(seatClient.getSeatById(seatId)).thenReturn(new SeatResponseDto(
                seatId, booking.getFlightId(), "15C", "ECONOMY", 15, "C",
                false, true, false, "HELD", BigDecimal.ONE, LocalDateTime.now().plusMinutes(10)
        ));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = bookingService.updateStatus(bookingId, new UpdateBookingStatusRequest(BookingStatus.CONFIRMED));

        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(seatClient).confirmSeat(seatId);
        verify(notificationClient).sendBookingConfirmation(any(BookingConfirmationRequestDto.class));
    }

    @Test
    void updateStatusShouldThrowWhenPaymentIsNotPaid() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(paymentClient.getPaymentByBooking(bookingId)).thenReturn(new PaymentResponseDto(
                UUID.randomUUID(), bookingId, booking.getUserId(), booking.getTotalFare(), "INR",
                "PENDING", "UPI", null, null, null, null, null, null
        ));

        assertThrows(
                BadRequestException.class,
                () -> bookingService.updateStatus(bookingId, new UpdateBookingStatusRequest(BookingStatus.CONFIRMED))
        );
    }
}
