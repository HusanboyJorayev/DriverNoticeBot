package org.example.drivernoticebot.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeatherResponse {
    private Location location;
    private Current current;


    @Getter
    @Setter
    public static class Location {
        private String name;
        private String country;

    }

    @Getter
    @Setter
    public static class Current {
        @JsonProperty("temp_c")
        private double tempC;
        private Condition condition;


        @Setter
        @Getter
        public static class Condition {
            private String text;
            private String icon;

        }
    }
}

