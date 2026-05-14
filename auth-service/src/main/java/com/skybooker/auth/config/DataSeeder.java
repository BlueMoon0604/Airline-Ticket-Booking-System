package com.skybooker.auth.config;

import com.skybooker.auth.entity.AuthProvider;
import com.skybooker.auth.entity.Role;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            userRepository.save(buildUser(
                    "SkyBooker Admin",
                    "admin@skybooker.com",
                    "Admin@123",
                    "9999999991",
                    Role.ADMIN,
                    "A1234567",
                    "INDIAN",
                    passwordEncoder
            ));

            userRepository.save(buildUser(
                    "Default Passenger",
                    "passenger@skybooker.com",
                    "Passenger@123",
                    "9999999992",
                    Role.PASSENGER,
                    "P1234567",
                    "INDIAN",
                    passwordEncoder
            ));

            userRepository.save(buildUser(
                    "Airline Staff",
                    "staff@skybooker.com",
                    "Staff@123",
                    "9999999993",
                    Role.AIRLINE_STAFF,
                    "S1234567",
                    "INDIAN",
                    passwordEncoder
            ));
        };
    }

    private User buildUser(String fullName,
                           String email,
                           String rawPassword,
                           String phone,
                           Role role,
                           String passportNumber,
                           String nationality,
                           PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setRole(role);
        user.setProvider(AuthProvider.LOCAL);
        user.setIsActive(true);
        user.setPassportNumber(passportNumber);
        user.setNationality(nationality);
        return user;
    }
}
