package ru.vlad.simpleweather;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.vlad.simpleweather.exception.WeatherSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import ru.vlad.simpleweather.model.WeatherData;
import ru.vlad.simpleweather.model.enums.WeatherMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {
    private static final Logger logger = LoggerFactory.getLogger(WeatherSDK.class);
    private static final Set<String> activeKeys = new HashSet<>();
    private String apiKey;
    private WeatherMode mode;
    private String baseUrl;
    private final RestTemplate restTemplate;
    private String units;
    private long cacheExpirationTime;
    private int maxCities;
    private long pollingInterval;
    private WeatherCache cache;
    private ScheduledExecutorService scheduler;
    private boolean isDestroyed = false;

    // Для Main: использует config.properties
    public WeatherSDK() throws WeatherSDKException {
        this.restTemplate = new RestTemplate();
        initialize(null, null, null);
    }

    // Для тестов: позволяет передать RestTemplate
    public WeatherSDK(WeatherMode mode, String baseUrl, RestTemplate restTemplate) throws WeatherSDKException {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
        initialize(mode, baseUrl, null);
    }

    private void initialize(WeatherMode mode, String baseUrl, String apiKeyOverride) throws WeatherSDKException {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        Properties config = loadConfig();
        logger.info("Configuration loaded successfully");

        this.apiKey = (apiKeyOverride != null && !apiKeyOverride.isEmpty())
                ? apiKeyOverride
                : config.getProperty("weather.api.key");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            logger.error("API key is not provided in config or constructor");
            throw new WeatherSDKException("API key must be provided in config.properties or constructor");
        }

        if (activeKeys.contains(this.apiKey)) {
            throw new WeatherSDKException("SDK instance with this API key already exists");
        }
        activeKeys.add(this.apiKey);

        this.baseUrl = (baseUrl != null && !baseUrl.isEmpty())
                ? baseUrl
                : config.getProperty("weather.api.url", "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s");

        String modeFromConfig = config.getProperty("weather.mode", "ON_DEMAND");
        this.mode = (mode != null)
                ? mode
                : WeatherMode.valueOf(modeFromConfig.toUpperCase());

        this.units = config.getProperty("weather.units", "metric");
        this.cacheExpirationTime = Long.parseLong(config.getProperty("cache.expiration.time", "600000"));
        this.maxCities = Integer.parseInt(config.getProperty("cache.max.cities", "10"));
        this.pollingInterval = Long.parseLong(config.getProperty("polling.interval.minutes", "10"));

        this.cache = new WeatherCache(maxCities, cacheExpirationTime);

        if (this.mode == WeatherMode.POLLING) {
            logger.info("Starting polling mode with interval {} minutes", pollingInterval);
            startPolling();
        }
    }

    private Properties loadConfig() throws WeatherSDKException {
        Properties properties = new Properties();
        try (InputStream input = WeatherSDK.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Configuration file 'config.properties' not found");
                throw new WeatherSDKException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Failed to load configuration: {}", e.getMessage());
            throw new WeatherSDKException("Failed to load config: " + e.getMessage());
        }
        return properties;
    }

    public WeatherData getWeather(String city) throws WeatherSDKException {
        if (isDestroyed) {
            logger.warn("Attempt to use destroyed SDK instance");
            throw new WeatherSDKException("SDK instance has been destroyed");
        }
        if (city == null || city.isEmpty()) {
            logger.warn("Invalid city name provided: {}", city);
            throw new WeatherSDKException("City name cannot be null or empty");
        }

        WeatherData cachedData = cache.get(city);
        if (cachedData != null && mode == WeatherMode.ON_DEMAND) {
            logger.info("Returning cached weather data for city {}", city);
            return cachedData;
        }

        logger.info("Fetching weather data for city {}", city);
        return fetchWeather(city);
    }

    private WeatherData fetchWeather(String city) throws WeatherSDKException {
        try {
            String url = String.format(baseUrl, city, apiKey, units);
            String jsonResponse = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            WeatherData data = mapper.readValue(jsonResponse, WeatherData.class);
            cache.put(city, data);
            return data;
        } catch (Exception e) {
            logger.error("Failed to fetch weather data for city {}: {}", city, e.getMessage());
            throw new WeatherSDKException("Failed to fetch weather data: " + e.getMessage());
        }
    }

    private void startPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            for (String city : cache.getCachedCities()) {
                try {
                    fetchWeather(city);
                } catch (WeatherSDKException e) {
                    logger.error("Failed to update weather for city {}: {}", city, e.getMessage());
                }
            }
        }, 0, pollingInterval, TimeUnit.MINUTES);
    }

    public void destroy() {
        if (!isDestroyed) {
            logger.info("Destroying SDK instance for API key {}", apiKey);
            scheduler.shutdown();
            activeKeys.remove(apiKey);
            isDestroyed = true;
        }
    }

    public static void clearActiveKeys() {
        activeKeys.clear();
    }
}