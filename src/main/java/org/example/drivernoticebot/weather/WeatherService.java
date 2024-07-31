package org.example.drivernoticebot.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WeatherService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public WeatherService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.baseUrl("https://api.weatherapi.com/v1").build();
        this.objectMapper = objectMapper;
    }

    public String getWeather(String city) {
        String apiKey = "5d9a27f0813d444ab5b65351243107";
        Mono<String> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", city)
                        //.queryParam("units", "metric")
                        .build())
                .retrieve()
                .bodyToMono(String.class);


        try {
            String jsonResponse = response.block();
            WeatherResponse weatherResponse = objectMapper.readValue(jsonResponse, WeatherResponse.class);

            String cityName = weatherResponse.getLocation().getName();
            String countryName = weatherResponse.getLocation().getCountry();
            double temperature = weatherResponse.getCurrent().getTempC();
            String weatherText = weatherResponse.getCurrent().getCondition().getText();
            String iconUrl = "https:" + weatherResponse.getCurrent().getCondition().getIcon();

            return String.format("City: %s\n Country: %s\n Temperature: %.2f°C\n Weather: %s\n Icon:%s",
                    cityName, countryName, temperature, weatherText, iconUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to fetch weather data.";
        }
    }
}

