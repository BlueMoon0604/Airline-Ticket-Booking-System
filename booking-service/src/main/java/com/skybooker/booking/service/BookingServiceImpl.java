package com.skybooker.booking.service;

import com.skybooker.booking.client.dto.SeatResponseDto;

import com.skybooker.booking.client.FlightClient;
import com.skybooker.booking.client.NotificationClient;
import com.skybooker.booking.client.PassengerClient;
import com.skybooker.booking.client.PaymentClient;
import com.skybooker.booking.client.SeatClient;
import com.skybooker.booking.client.dto.*;
import com.skybooker.booking.dto.*;
import com.skybooker.booking.entity.Booking;
import com.skybooker.booking.entity.BookingStatus;
import com.skybooker.booking.exception.BadRequestException;
import com.skybooker.booking.exception.ResourceNotFoundException;
import com.skybooker.booking.repository.BookingRepository;
import com.skybooker.booking.service.BookingService;
import com.skybooker.booking.service.PdfBoardingPassService;
import com.skybooker.booking.service.PdfTicketService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final SeatClient seatClient;
    private final PassengerClient passengerClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;
    private final PdfTicketService pdfTicketService;
    private final PdfBoardingPassService pdfBoardingPassService;

    private final BigDecimal gstRate;
    private final BigDecimal fuelSurchargeRate;
    private final BigDecimal baggageRatePerKg;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              FlightClient flightClient,
                              SeatClient seatClient,
                              PassengerClient passengerClient,
                              PaymentClient paymentClient,
                              NotificationClient notificationClient,
                              PdfTicketService pdfTicketService,
                              PdfBoardingPassService pdfBoardingPassService,
                              @Value("${application.booking.gst-rate}") BigDecimal gstRate,
                              @Value("${application.booking.fuel-surcharge-rate}") BigDecimal fuelSurchargeRate,
                              @Value("${application.booking.baggage-rate-per-kg}") BigDecimal baggageRatePerKg) {
        this.bookingRepository = bookingRepository;
        this.flightClient = flightClient;
        this.seatClient = seatClient;
        this.passengerClient = passengerClient;
        this.paymentClient = paymentClient;
        this.notificationClient = notificationClient;
        this.pdfTicketService = pdfTicketService;
        this.pdfBoardingPassService = pdfBoardingPassService;
        this.gstRate = gstRate;
        this.fuelSurchargeRate = fuelSurchargeRate;
        this.baggageRatePerKg = baggageRatePerKg;
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        FlightResponse flight = flightClient.getFlightById(request.flightId());

        int passengerCount = request.passengers().size();

        if (flight.availableSeats() < passengerCount) {
            throw new BadRequestException("Not enough seats available");
        }

        FareSummaryResponse fare = calculateFare(request.flightId(), passengerCount, request.luggageKg());

        Booking booking = new Booking();
        booking.setUserId(request.userId());
        booking.setFlightId(request.flightId());
        booking.setTripType(request.tripType());
        booking.setPnrCode(generatePnr());
        booking.setStatus(BookingStatus.PENDING);
        booking.setBaseFare(fare.baseFare());
        booking.setTaxes(fare.gstAmount().add(fare.fuelSurcharge()));
        booking.setTotalFare(fare.totalFare());
        booking.setMealPreference(request.mealPreference() == null ? "STANDARD" : request.mealPreference());
        booking.setLuggageKg(request.luggageKg());
        booking.setContactEmail(request.contactEmail());
        booking.setContactPhone(request.contactPhone());

        Booking savedBooking = bookingRepository.save(booking);
        boolean seatsDecremented = false;

        try {
            for (PassengerBookingRequest passenger : request.passengers()) {
                seatClient.holdSeat(passenger.seatId());

                PassengerResponseDto createdPassenger = passengerClient.addPassenger(
                        new PassengerRequestDto(
                                savedBooking.getBookingId(),
                                passenger.title(),
                                passenger.firstName(),
                                passenger.lastName(),
                                passenger.dateOfBirth(),
                                passenger.gender(),
                                passenger.passportNumber(),
                                passenger.nationality(),
                                passenger.passportExpiry(),
                                passenger.passengerType()
                        )
                );

                passengerClient.assignSeat(
                        createdPassenger.passengerId(),
                        new SeatAssignmentRequestDto(passenger.seatId(), passenger.seatNumber())
                );
            }

            flightClient.decrementSeats(
                    savedBooking.getFlightId(),
                    new FlightClient.SeatCountRequest(passengerCount)
            );
            seatsDecremented = true;

            PaymentResponseDto payment = paymentClient.initiatePayment(
                    new PaymentInitiateRequestDto(
                            savedBooking.getBookingId(),
                            savedBooking.getUserId(),
                            savedBooking.getTotalFare(),
                            request.paymentMode()
                    )
            );

            savedBooking.setPaymentId(payment.paymentId());
            savedBooking = bookingRepository.save(savedBooking);

            return map(savedBooking, payment.paymentSessionUrl());

        } catch (Exception ex) {
            try {
                passengerClient.deletePassengersByBooking(savedBooking.getBookingId());
            } catch (Exception ignored) {
            }

            for (PassengerBookingRequest passenger : request.passengers()) {
                try {
                    seatClient.releaseSeat(passenger.seatId());
                } catch (Exception ignored) {
                }
            }

            if (seatsDecremented) {
                try {
                    flightClient.incrementSeats(
                            savedBooking.getFlightId(),
                            new FlightClient.SeatCountRequest(passengerCount)
                    );
                } catch (Exception ignored) {
                }
            }

            throw new BadRequestException("Booking orchestration failed: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId) {
        Booking booking = findBooking(bookingId);
        return map(booking, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByPnr(String pnrCode) {
        Booking booking = bookingRepository.findByPnrCode(pnrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return map(booking, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(booking -> map(booking, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByFlight(UUID flightId) {
        return bookingRepository.findByFlightId(flightId).stream()
                .map(booking -> map(booking, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING
                        || booking.getStatus() == BookingStatus.CONFIRMED)
                .map(booking -> map(booking, null))
                .toList();
    }

    @Override
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = findBooking(bookingId);

        List<PassengerResponseDto> passengers = passengerClient.getPassengersByBooking(booking.getBookingId());

        for (PassengerResponseDto passenger : passengers) {
            if (passenger.seatId() != null) {
                try {
                    seatClient.releaseSeat(passenger.seatId());
                } catch (Exception ignored) {
                }
            }
        }

        flightClient.incrementSeats(
                booking.getFlightId(),
                new FlightClient.SeatCountRequest(passengers.size())
        );

        if (booking.getPaymentId() != null) {
            try {
                paymentClient.refundPayment(
                        new RefundRequestDto(
                                booking.getPaymentId(),
                                booking.getTotalFare()
                        )
                );
            } catch (Exception ignored) {
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);

        return map(savedBooking, null);
    }

    @Override
    public BookingResponse updateStatus(UUID bookingId, UpdateBookingStatusRequest request) {
        Booking booking = findBooking(bookingId);

        if (request.status() == BookingStatus.CONFIRMED) {
            PaymentResponseDto payment = paymentClient.getPaymentByBooking(booking.getBookingId());

            if (!"PAID".equalsIgnoreCase(payment.status())) {
                throw new BadRequestException("Booking cannot be confirmed until payment is PAID");
            }

            List<PassengerResponseDto> passengers = passengerClient.getPassengersByBooking(booking.getBookingId());

            for (PassengerResponseDto passenger : passengers) {
                if (passenger.seatId() != null) {
                    SeatResponseDto seat = seatClient.getSeatById(passenger.seatId());

                    if ("HELD".equalsIgnoreCase(seat.status())) {
                        seatClient.confirmSeat(passenger.seatId());
                    } else if ("AVAILABLE".equalsIgnoreCase(seat.status())) {
                        // Payment can complete after the original 15-minute hold expires.
                        // If the seat is still free and already assigned to this booking's passenger,
                        // recover it for the same passenger and confirm it now.
                        seatClient.holdSeat(passenger.seatId());
                        seatClient.confirmSeat(passenger.seatId());
                    } else if ("CONFIRMED".equalsIgnoreCase(seat.status())) {
                        // already confirmed, skip
                    } else {
                        throw new BadRequestException(
                                "Seat " + passenger.seatId() + " is not in HELD state for confirmation"
                        );
                    }
                }
            }
        }

        booking.setStatus(request.status());
        Booking savedBooking = bookingRepository.save(booking);

        if (request.status() == BookingStatus.CONFIRMED) {
            try {
                notificationClient.sendBookingConfirmation(
                        new BookingConfirmationRequestDto(
                                booking.getUserId(),
                                booking.getBookingId(),
                                booking.getContactEmail(),
                                booking.getContactPhone(),
                                booking.getPnrCode(),
                                ""
                        )
                );
            } catch (Exception ignored) {
                // booking confirmation should not fail if notification/email fails
            }
        }

        return map(savedBooking, null);
    }

    @Override
    public FareSummaryResponse calculateFare(UUID flightId, Integer passengerCount, Integer luggageKg) {
        FlightResponse flight = flightClient.getFlightById(flightId);

        BigDecimal passengerCountValue = BigDecimal.valueOf(passengerCount);
        BigDecimal baseFare = flight.basePrice().multiply(passengerCountValue);

        BigDecimal gstAmount = baseFare.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fuelSurcharge = baseFare.multiply(fuelSurchargeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ancillaryAmount = baggageRatePerKg
                .multiply(BigDecimal.valueOf(luggageKg))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalFare = baseFare
                .add(gstAmount)
                .add(fuelSurcharge)
                .add(ancillaryAmount)
                .setScale(2, RoundingMode.HALF_UP);

        return new FareSummaryResponse(baseFare, gstAmount, fuelSurcharge, ancillaryAmount, totalFare);
    }

    @Override
    public BookingResponse addAddOn(UUID bookingId, AddOnRequest request) {
        Booking booking = findBooking(bookingId);

        FareSummaryResponse recalculated = calculateFare(
                booking.getFlightId(),
                passengerClient.getPassengersByBooking(booking.getBookingId()).size(),
                request.luggageKg()
        );

        booking.setMealPreference(request.mealPreference());
        booking.setLuggageKg(request.luggageKg());
        booking.setTotalFare(recalculated.totalFare());

        Booking saved = bookingRepository.save(booking);
        return map(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(booking -> map(booking, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse generateETicket(UUID bookingId) {
        Booking booking = findBooking(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("E-ticket can only be generated for confirmed bookings");
        }

        FlightResponse flight = flightClient.getFlightById(booking.getFlightId());
        List<PassengerResponseDto> passengers = passengerClient.getPassengersByBooking(booking.getBookingId());

        byte[] pdfBytes = pdfTicketService.generateETicket(booking, flight, passengers);
        String encoded = Base64.getEncoder().encodeToString(pdfBytes);

        return new TicketResponse(
                booking.getBookingId(),
                "eticket-" + booking.getPnrCode() + ".pdf",
                "application/pdf",
                encoded
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BoardingPassResponse generateBoardingPass(UUID bookingId, UUID passengerId) {
        Booking booking = findBooking(bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Boarding pass can only be generated for confirmed bookings");
        }

        FlightResponse flight = flightClient.getFlightById(booking.getFlightId());
        List<PassengerResponseDto> passengers = passengerClient.getPassengersByBooking(booking.getBookingId());

        PassengerResponseDto passenger = passengers.stream()
                .filter(p -> p.passengerId().equals(passengerId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found for booking"));

        if (passenger.seatNumber() == null || passenger.seatNumber().isBlank()) {
            throw new BadRequestException("Boarding pass cannot be generated without seat assignment");
        }

        byte[] pdfBytes = pdfBoardingPassService.generateBoardingPass(booking, flight, passenger);
        String encoded = Base64.getEncoder().encodeToString(pdfBytes);

        return new BoardingPassResponse(
                booking.getBookingId(),
                passenger.passengerId(),
                "boarding-pass-" + booking.getPnrCode() + "-" + passenger.passengerId() + ".pdf",
                "application/pdf",
                encoded
        );
    }

    private Booking findBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    }

    private String generatePnr() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String pnr;

        do {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                builder.append(chars.charAt(random.nextInt(chars.length())));
            }
            pnr = builder.toString();
        } while (bookingRepository.existsByPnrCode(pnr));

        return pnr;
    }

    private BookingResponse map(Booking booking, String paymentSessionUrl) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getUserId(),
                booking.getFlightId(),
                booking.getPnrCode(),
                booking.getTripType(),
                booking.getStatus(),
                booking.getTotalFare(),
                booking.getBaseFare(),
                booking.getTaxes(),
                booking.getMealPreference(),
                booking.getLuggageKg(),
                booking.getContactEmail(),
                booking.getContactPhone(),
                booking.getBookedAt(),
                booking.getPaymentId(),
                paymentSessionUrl
        );
    }
}
