package ru.vlad.simpleweather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vlad.simpleweather.model.WeatherData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WeatherCache {
    private static final Logger logger = LoggerFactory.getLogger(WeatherCache.class);
    private final Map<String, CacheEntry> cache;
    private final int maxCities;
    private final long expirationTime;

    public WeatherCache(int maxCities, long expirationTime) {
        this.maxCities = maxCities;
        this.expirationTime = expirationTime;
        cache = new LinkedHashMap<String, CacheEntry>(maxCities, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                if (size() > WeatherCache.this.maxCities) {
                    logger.info("Removing oldest cache entry for city {}", eldest.getKey());
                    return true;
                }
                return false;
            }
        };
    }

    public WeatherData get(String city) {
        CacheEntry entry = cache.get(city);
        if (entry != null && !entry.isExpired()) {
            return entry.data;
        }
        if (entry != null) {
            logger.warn("Cache entry for city {} has expired", city);
        }
        return null;
    }

    public void put(String city, WeatherData data) {
        logger.info("Caching weather data for city {}", city);
        cache.put(city, new CacheEntry(data));
    }

    public Set<String> getCachedCities() {
        return cache.keySet();
    }

    private class CacheEntry {
        WeatherData data;
        long timestamp;

        CacheEntry(WeatherData data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > expirationTime;
        }
    }
}