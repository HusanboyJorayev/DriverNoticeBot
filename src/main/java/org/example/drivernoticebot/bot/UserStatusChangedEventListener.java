package org.example.drivernoticebot.bot;

import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.information.Drivers;
import org.example.drivernoticebot.repository.DriversRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusChangedEventListener {
    private final DriverNoticeBot2 bot2;
    private final DriversRepository driversRepository;

    @EventListener
    public void handleUserStatusChangedEvent(UserStatusChangedEvent event) {
        Drivers driver = event.getDrivers();
        Long chatId = driver.getChatId();
        if (driversRepository.existsByChatId(chatId)) {
            Drivers drivers = driversRepository.findByChatId(chatId)
                    .orElseThrow(() -> new RuntimeException("Could not find chat with id " + chatId));
            bot2.sendMessage(chatId.toString(), drivers.toString());
            bot2.sendMessageToChannel(drivers.toString());
        }
    }
}
