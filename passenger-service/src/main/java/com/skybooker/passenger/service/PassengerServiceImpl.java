package com.skybooker.passenger.service;

import com.skybooker.passenger.dto.ApiResponse;
import com.skybooker.passenger.dto.PassengerCountResponse;
import com.skybooker.passenger.dto.PassengerRequest;
import com.skybooker.passenger.dto.PassengerResponse;
import com.skybooker.passenger.dto.SeatAssignmentRequest;
import com.skybooker.passenger.entity.PassengerInfo;
import com.skybooker.passenger.entity.PassengerType;
import com.skybooker.passenger.exception.BadRequestException;
import com.skybooker.passenger.exception.ResourceNotFoundException;
import com.skybooker.passenger.repository.PassengerRepository;
import com.skybooker.passenger.service.PassengerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PassengerServiceImpl implements PassengerService {

    private final PassengerRepository passengerRepository;
    public PassengerServiceImpl(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    @Override
    public PassengerResponse addPassenger(PassengerRequest request) {
        validatePassengerData(request);

        if (passengerRepository.findByPassportNumber(request.passportNumber()).isPresent()) {
            throw new BadRequestException("Passport number already exists");
        }

        PassengerInfo passenger = new PassengerInfo();
        mapRequestToEntity(request, passenger);
        passenger.setTicketNumber(generateTicketNumber());
        PassengerInfo savedPassenger = passengerRepository.save(passenger);
        return mapToResponse(savedPassenger);
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse getPassengerById(UUID passengerId) {
        PassengerInfo passenger = findPassenger(passengerId);
        return mapToResponse(passenger);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PassengerResponse> getPassengersByBooking(UUID bookingId) {
        return passengerRepository.findByBookingId(bookingId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerResponse getByPassportNumber(String passportNumber) {
        PassengerInfo passenger = passengerRepository.findByPassportNumber(passportNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
        return mapToResponse(passenger);
    }

    @Override
    public PassengerResponse updatePassenger(UUID passengerId, PassengerRequest request) {
        validatePassengerData(request);
        PassengerInfo passenger = findPassenger(passengerId);
        PassengerInfo existingPassenger = passengerRepository.findByPassportNumber(request.passportNumber()).orElse(null);

        if (existingPassenger != null && !existingPassenger.getPassengerId().equals(passengerId)) {
            throw new BadRequestException("Passport number already exists");
        }

        String oldTicketNumber = passenger.getTicketNumber();
        UUID oldSeatId = passenger.getSeatId();
        String oldSeatNumber = passenger.getSeatNumber();
        mapRequestToEntity(request, passenger);
        passenger.setTicketNumber(oldTicketNumber);
        passenger.setSeatId(oldSeatId);
        passenger.setSeatNumber(oldSeatNumber);

        PassengerInfo updatedPassenger = passengerRepository.save(passenger);
        return mapToResponse(updatedPassenger);
    }

    @Override
    public ApiResponse assignSeat(UUID passengerId, SeatAssignmentRequest request) {
        PassengerInfo passenger = findPassenger(passengerId);
        passenger.setSeatId(request.seatId());
        passenger.setSeatNumber(request.seatNumber());
        passengerRepository.save(passenger);
        return new ApiResponse("Seat assigned successfully", mapToResponse(passenger));
    }

    @Override
    public ApiResponse deletePassenger(UUID passengerId) {
        PassengerInfo passenger = findPassenger(passengerId);
        passengerRepository.delete(passenger);
        return new ApiResponse("Passenger deleted successfully");
    }

    @Override
    public ApiResponse deletePassengersByBooking(UUID bookingId) {
        passengerRepository.deleteByBookingId(bookingId);
        return new ApiResponse("Passengers deleted successfully for booking " + bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public PassengerCountResponse getPassengerCount(UUID bookingId) {
        long count = passengerRepository.countByBookingId(bookingId);
        return new PassengerCountResponse(bookingId, count);
    }

    @Override
    public void validatePassengerData(PassengerRequest request) {
        if (request.passportExpiry().isBefore(LocalDate.now())) {
            throw new BadRequestException("Passport is expired");
        }

        int age = Period.between(request.dateOfBirth(), LocalDate.now()).getYears();
        PassengerType type = request.passengerType() == null ? PassengerType.ADULT : request.passengerType();
        switch (type) {
            case ADULT -> {
                if (age < 12) {
                    throw new BadRequestException("Adult passenger must be at least 12 years old");
                }
            }
            case CHILD -> {
                if (age < 2 || age >= 12) {
                    throw new BadRequestException("Child passenger age must be between 2 and 11");
                }
            }
            case INFANT -> {
                if (age >= 2) {
                    throw new BadRequestException("Infant passenger must be below 2 years");
                }
            }
        }
    }

    private PassengerInfo findPassenger(UUID passengerId) {
        return passengerRepository.findById(passengerId).orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
    }

    private void mapRequestToEntity(PassengerRequest request, PassengerInfo passenger) {
        passenger.setBookingId(request.bookingId());
        passenger.setTitle(request.title());
        passenger.setFirstName(request.firstName());
        passenger.setLastName(request.lastName());
        passenger.setDateOfBirth(request.dateOfBirth());
        passenger.setGender(request.gender());
        passenger.setPassportNumber(request.passportNumber().toUpperCase());
        passenger.setNationality(request.nationality());
        passenger.setPassportExpiry(request.passportExpiry());
        passenger.setPassengerType(request.passengerType() == null ? PassengerType.ADULT : request.passengerType());
    }

    private String generateTicketNumber() {
        String prefix = "TKT";
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        String ticketNumber = prefix + uniquePart;
        while (passengerRepository.findByTicketNumber(ticketNumber).isPresent()) {
            uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            ticketNumber = prefix + uniquePart;
        }
        return ticketNumber;
    }

    private PassengerResponse mapToResponse(PassengerInfo passenger) {
        return new PassengerResponse(passenger.getPassengerId(),passenger.getBookingId(),passenger.getTitle(),passenger.getFirstName(),passenger.getLastName(),
                passenger.getDateOfBirth(),
                passenger.getGender(),
                passenger.getPassportNumber(),
                passenger.getNationality(),
                passenger.getPassportExpiry(),
                passenger.getSeatId(),
                passenger.getSeatNumber(),
                passenger.getTicketNumber(),
                passenger.getPassengerType());
    }
}

