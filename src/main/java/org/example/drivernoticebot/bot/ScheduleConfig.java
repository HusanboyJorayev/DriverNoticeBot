package org.example.drivernoticebot.bot;

import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.information.Drivers;
import org.example.drivernoticebot.repository.DriversRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ScheduleConfig {

    private final DriversRepository repository;

    @Scheduled(fixedRate = 5000)
    public void config() {
        List<Drivers> drivers = this.repository.findAll();
        for (Drivers driver : drivers) {
            if (driver.getChatId() == null)
                repository.delete(driver);
        }
    }
}
