package org.example.drivernoticebot.bot;

import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.repository.DriverRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class DriverNoticeBotConfig {
    private final BotConfig botConfig;
    private final DriverRepository repository;

    @Bean
    public DriverNoticeBot driverNoticeBot() {
        DriverNoticeBot driverNoticeBot = new DriverNoticeBot(botConfig, repository);
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(driverNoticeBot);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driverNoticeBot;
    }
}
