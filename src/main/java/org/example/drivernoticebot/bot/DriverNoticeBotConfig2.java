package org.example.drivernoticebot.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.drivernoticebot.currency.CurrencyService;
import org.example.drivernoticebot.weather.WeatherService;
import org.example.drivernoticebot.repository.DriversRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class DriverNoticeBotConfig2 {

    private final BotConfig botConfig;
    private final DriversRepository repository;
    private final WeatherService weatherService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public DriverNoticeBot2 driverNoticeBot2() {
        DriverNoticeBot2 driverNoticeBot = new DriverNoticeBot2(botConfig, repository,
                weatherService, restTemplate, objectMapper);
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(driverNoticeBot);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driverNoticeBot;
    }
}
