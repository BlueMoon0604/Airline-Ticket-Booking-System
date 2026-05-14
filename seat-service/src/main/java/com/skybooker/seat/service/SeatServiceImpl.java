package com.skybooker.seat.service;

import com.skybooker.seat.dto.ApiResponse;
import com.skybooker.seat.dto.SeatCountByClassResponse;
import com.skybooker.seat.dto.SeatMapResponse;
import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.dto.SeatResponse;
import com.skybooker.seat.entity.Seat;
import com.skybooker.seat.entity.SeatClass;
import com.skybooker.seat.entity.SeatStatus;
import com.skybooker.seat.exception.BadRequestException;
import com.skybooker.seat.exception.ResourceNotFoundException;
import com.skybooker.seat.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;

    public SeatServiceImpl(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Override
    public SeatResponse addSeat(SeatRequest request) {
        validateSeatRequest(request);

        if (seatRepository.findByFlightIdAndSeatNumberIgnoreCase(request.flightId(), request.seatNumber()).isPresent()) {
            throw new BadRequestException("Seat number already exists for this flight");
        }

        Seat seat = new Seat();
        mapRequestToEntity(request, seat);
        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    public List<SeatResponse> addSeatsForFlight(List<SeatRequest> requests) {
        return requests.stream().map(this::addSeat).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByFlight(UUID flightId) {
        return seatRepository.findByFlightId(flightId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableSeats(UUID flightId) {
        return seatRepository.findByFlightIdAndStatus(flightId, SeatStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getAvailableByClass(UUID flightId, SeatClass seatClass) {
        return seatRepository.findByFlightIdAndSeatClass(flightId, seatClass).stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatResponse getSeatById(UUID seatId) {
        return mapToResponse(findSeat(seatId));
    }

    @Override
    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(UUID flightId) {
        return new SeatMapResponse(flightId, getSeatsByFlight(flightId));
    }

    @Override
    public ApiResponse holdSeat(UUID seatId) {
        Seat seat = findSeat(seatId);

        if (seat.getStatus() == SeatStatus.HELD
                && seat.getHoldUntil() != null
                && seat.getHoldUntil().isBefore(LocalDateTime.now())) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHoldUntil(null);
        }

        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new BadRequestException("Seat is not available");
        }

        seat.setStatus(SeatStatus.HELD);
        seat.setHoldUntil(LocalDateTime.now().plusMinutes(15));
        seatRepository.save(seat);

        return new ApiResponse("Seat held successfully", mapToResponse(seat));
    }

    @Override
    public ApiResponse releaseSeat(UUID seatId) {
        Seat seat = findSeat(seatId);

        if (seat.getStatus() == SeatStatus.CONFIRMED) {
            throw new BadRequestException("Confirmed seat cannot be released");
        }

        if (seat.getStatus() == SeatStatus.BLOCKED) {
            throw new BadRequestException("Blocked seat cannot be released");
        }

        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setHoldUntil(null);
        seatRepository.save(seat);

        return new ApiResponse("Seat released successfully", mapToResponse(seat));
    }

    @Override
    public ApiResponse confirmSeat(UUID seatId) {
        Seat seat = findSeat(seatId);

        if (seat.getStatus() != SeatStatus.HELD) {
            throw new BadRequestException("Only held seat can be confirmed");
        }

        seat.setStatus(SeatStatus.CONFIRMED);
        seat.setHoldUntil(null);
        seatRepository.save(seat);

        return new ApiResponse("Seat confirmed successfully", mapToResponse(seat));
    }

    @Override
    public SeatResponse updateSeat(UUID seatId, SeatRequest request) {
        validateSeatRequest(request);

        Seat seat = findSeat(seatId);

        if (seat.getStatus() == SeatStatus.HELD
                || seat.getStatus() == SeatStatus.CONFIRMED
                || seat.getStatus() == SeatStatus.BLOCKED) {
            throw new BadRequestException("Held, confirmed, or blocked seat cannot be updated");
        }

        Seat existing = seatRepository.findByFlightIdAndSeatNumberIgnoreCase(request.flightId(), request.seatNumber())
                .orElse(null);

        if (existing != null && !existing.getSeatId().equals(seatId)) {
            throw new BadRequestException("Seat number already exists for this flight");
        }

        mapRequestToEntity(request, seat);
        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatCountByClassResponse> countAvailableByClass(UUID flightId) {
        return Arrays.stream(SeatClass.values())
                .map(seatClass -> new SeatCountByClassResponse(
                        seatClass,
                        seatRepository.countByFlightIdAndSeatClassAndStatus(
                                flightId,
                                seatClass,
                                SeatStatus.AVAILABLE
                        )
                ))
                .toList();
    }

    @Override
    public ApiResponse deleteSeatsForFlight(UUID flightId) {
        seatRepository.deleteByFlightId(flightId);
        return new ApiResponse("Seats deleted successfully for flight " + flightId);
    }

    @Override
    public ApiResponse releaseExpiredHolds() {
        List<Seat> expiredSeats = seatRepository.findByStatusAndHoldUntilBefore(
                SeatStatus.HELD,
                LocalDateTime.now()
        );

        expiredSeats.forEach(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHoldUntil(null);
        });

        seatRepository.saveAll(expiredSeats);
        return new ApiResponse("Expired holds released successfully", expiredSeats.size());
    }

    private Seat findSeat(UUID seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
    }

    private void validateSeatRequest(SeatRequest request) {
        if (request.rowNumber() < 1) {
            throw new BadRequestException("Row number must be greater than 0");
        }
    }

    private void mapRequestToEntity(SeatRequest request, Seat seat) {
        seat.setFlightId(request.flightId());
        seat.setSeatNumber(request.seatNumber().toUpperCase());
        seat.setSeatClass(request.seatClass());
        seat.setRowNumber(request.rowNumber());
        seat.setSeatColumn(request.seatColumn().toUpperCase());
        seat.setIsWindow(request.isWindow());
        seat.setIsAisle(request.isAisle());
        seat.setHasExtraLegroom(request.hasExtraLegroom());
        seat.setPriceMultiplier(request.priceMultiplier());
    }

    private SeatResponse mapToResponse(Seat seat) {
        return new SeatResponse(
                seat.getSeatId(),
                seat.getFlightId(),
                seat.getSeatNumber(),
                seat.getSeatClass(),
                seat.getRowNumber(),
                seat.getSeatColumn(),
                seat.getIsWindow(),
                seat.getIsAisle(),
                seat.getHasExtraLegroom(),
                seat.getStatus(),
                seat.getPriceMultiplier(),
                seat.getHoldUntil()
        );
    }
}
