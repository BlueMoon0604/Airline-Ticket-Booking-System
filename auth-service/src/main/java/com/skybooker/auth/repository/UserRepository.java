package com.skybooker.auth.repository;

import com.skybooker.auth.entity.Role;
import com.skybooker.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Optional<User> findByPassportNumber(String passportNumber);

    List<User> findAllByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByPassportNumber(String passportNumber);

    void deleteByUserId(UUID userId);
}
