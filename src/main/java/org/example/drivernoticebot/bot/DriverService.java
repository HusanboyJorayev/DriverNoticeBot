package org.example.drivernoticebot.bot;

import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.information.Drivers;
import org.example.drivernoticebot.repository.DriversRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriversRepository driversRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public void updateUserStatus(Integer driverId, String newStatus) {
        Drivers drivers = driversRepository.findById(driverId).orElseThrow(() -> new RuntimeException("User not found"));
        drivers.setStatus(newStatus);
        driversRepository.save(drivers);

        // Hodisani tetiklash
        eventPublisher.publishEvent(new UserStatusChangedEvent(this, drivers));
    }
}

