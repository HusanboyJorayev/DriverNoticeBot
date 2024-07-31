package org.example.drivernoticebot.repository;

import org.example.drivernoticebot.information.Drivers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Driver;
import java.util.Optional;

@Repository
public interface DriversRepository extends JpaRepository<Drivers, Integer> {
    Optional<Drivers> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
