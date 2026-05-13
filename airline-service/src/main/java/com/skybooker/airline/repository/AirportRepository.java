package com.skybooker.airline.repository;

import com.skybooker.airline.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AirportRepository extends JpaRepository<Airport, UUID> {

    Optional<Airport> findByIataCodeIgnoreCase(String iataCode);

    Optional<Airport> findByIcaoCodeIgnoreCase(String icaoCode);

    List<Airport> findByCityIgnoreCase(String city);

    List<Airport> findByCountryIgnoreCase(String country);

    List<Airport> findByNameContainingIgnoreCaseOrCityContainingIgnoreCaseOrIataCodeContainingIgnoreCase(
            String name, String city, String iataCode
    );

    boolean existsByIataCodeIgnoreCase(String iataCode);

    boolean existsByIcaoCodeIgnoreCase(String icaoCode);
}
