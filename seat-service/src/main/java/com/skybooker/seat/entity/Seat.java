package com.skybooker.seat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_flight_seat_number", columnNames = {"flight_id", "seat_number"})
        }
)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seat_id")
    private UUID seatId;

    @Column(name = "flight_id", nullable = false)
    private UUID flightId;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "seat_row", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_column", nullable = false, length = 5)
    private String seatColumn;

    @Column(name = "is_window", nullable = false)
    private Boolean isWindow;

    @Column(name = "is_aisle", nullable = false)
    private Boolean isAisle;

    @Column(name = "has_extra_legroom", nullable = false)
    private Boolean hasExtraLegroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeatStatus status;

    @Column(name = "price_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal priceMultiplier;

    @Column(name = "hold_until")
    private LocalDateTime holdUntil;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = SeatStatus.AVAILABLE;
        }

        if (isWindow == null) {
            isWindow = false;
        }

        if (isAisle == null) {
            isAisle = false;
        }

        if (hasExtraLegroom == null) {
            hasExtraLegroom = false;
        }

        if (priceMultiplier == null) {
            priceMultiplier = BigDecimal.ONE;
        }
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
    }

    public UUID getFlightId() {
        return flightId;
    }

    public void setFlightId(UUID flightId) {
        this.flightId = flightId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatClass getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(SeatClass seatClass) {
        this.seatClass = seatClass;
    }

    public Integer getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(Integer rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getSeatColumn() {
        return seatColumn;
    }

    public void setSeatColumn(String seatColumn) {
        this.seatColumn = seatColumn;
    }

    public Boolean getIsWindow() {
        return isWindow;
    }

    public void setIsWindow(Boolean window) {
        isWindow = window;
    }

    public Boolean getIsAisle() {
        return isAisle;
    }

    public void setIsAisle(Boolean aisle) {
        isAisle = aisle;
    }

    public Boolean getHasExtraLegroom() {
        return hasExtraLegroom;
    }

    public void setHasExtraLegroom(Boolean hasExtraLegroom) {
        this.hasExtraLegroom = hasExtraLegroom;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(BigDecimal priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(LocalDateTime holdUntil) {
        this.holdUntil = holdUntil;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}