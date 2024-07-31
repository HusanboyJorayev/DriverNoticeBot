package org.example.drivernoticebot.bot;

import lombok.Getter;
import org.example.drivernoticebot.information.Drivers;
import org.springframework.context.ApplicationEvent;


@Getter
public class UserStatusChangedEvent extends ApplicationEvent {
    private final Drivers drivers;

    public UserStatusChangedEvent(Object source, Drivers drivers) {
        super(source);
        this.drivers = drivers;
    }
}

