package org.example.drivernoticebot.currency;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CurrencyService {

    private final WebClient webClient;

    public CurrencyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://v6.exchangerate-api.com/v6/2bbdcd113af69bfe73ebadc7/latest/").build();
    }
    public Mono<String> getExchangeRate(String baseCurrency) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(baseCurrency)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }
}

