package ru.vlad.simpleweather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherData {
    @JsonProperty("weather")
    private List<Weather> weather; // Сделал массивом не по заданию под JSON
    @JsonProperty("main")
    private Main main; // Переименовали temperature в main
    @JsonProperty("visibility")
    private int visibility;
    @JsonProperty("wind")
    private Wind wind;
    @JsonProperty("dt")
    private long datetime; // "dt" вместо "datetime"
    @JsonProperty("sys")
    private Sys sys;
    @JsonProperty("timezone")
    private int timezone;
    @JsonProperty("name")
    private String name;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        private String main;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main { // Заменили Temperature на Main
        private double temp;
        @JsonProperty("feels_like")
        private double feelsLike;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        private double speed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        private long sunrise;
        private long sunset;
    }
}