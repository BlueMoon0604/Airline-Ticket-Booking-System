package com.skybooker.seat.repository;

import com.skybooker.seat.entity.Seat;
import com.skybooker.seat.entity.SeatClass;
import com.skybooker.seat.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findByFlightId(UUID flightId);
    List<Seat> findByFlightIdAndSeatClass(UUID flightId, SeatClass seatClass);
    List<Seat> findByFlightIdAndStatus(UUID flightId, SeatStatus status);
    
    Optional<Seat> findByFlightIdAndSeatNumberIgnoreCase(UUID flightId, String seatNumber);
    long countByFlightIdAndSeatClassAndStatus(UUID flightId, SeatClass seatClass, SeatStatus status);
    void deleteByFlightId(UUID flightId);
    List<Seat> findByStatusAndHoldUntilBefore(SeatStatus status, LocalDateTime time);
}
