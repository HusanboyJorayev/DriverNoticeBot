package org.example.drivernoticebot.repository;

import org.example.drivernoticebot.information.DriverNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<DriverNotice, Integer> {
    boolean existsByChatId(Long chatId);

    Optional<DriverNotice> findByChatId(Long chatId);
}
