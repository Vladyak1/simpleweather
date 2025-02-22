# SimpleWeather SDK

A Java SDK for accessing the OpenWeather API to retrieve weather data for a given location.

## Overview
This SDK provides a simple interface to query the OpenWeather API, supporting both on-demand and polling modes. It caches weather data for up to 10 cities with a configurable expiration time and ensures unique API key usage within a single JVM instance.

## Features
- **Modes**: Supports `ON_DEMAND` (fetches weather on request) and `POLLING` (periodically updates weather in the background).
- **Caching**: Stores weather data for up to 10 cities, refreshing every 10 minutes by default.
- **Error Handling**: Throws `WeatherSDKException` with detailed messages for failures (e.g., invalid API key, network issues).
- **Configurable**: Settings like API key, base URL, and cache parameters are loaded from `config.properties`.
- **Logging**: Uses SLF4J with Logback for detailed logging of operations and errors.

## Installation
1. Clone the repository:
   git clone https://github.com/Vladyak1/simpleweather.git
2. Build the project with Maven:
   mvn clean install
3. Add it as a dependency in your `pom.xml`:

```
<dependency>
    <groupId>ru.vlad</groupId>
    <artifactId>simpleweather</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Configuration
The SDK uses a config.properties file located in src/main/resources. Below is an example configuration:
```xml
# Base URL for the OpenWeather API.
# Format: %s for city, %s for API key, %s for units.
weather.api.url=https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=%s

# API key for accessing the OpenWeather API.
# Replace with your own key from openweathermap.org.
weather.api.key=your-api-key-here

# Mode of operation for the SDK.
# Possible values: ON_DEMAND (updates weather only on request), POLLING (updates weather periodically).
weather.mode=ON_DEMAND

# Units for temperature and other measurements.
# Possible values: metric (Celsius), imperial (Fahrenheit), standard (Kelvin).
weather.units=metric

# Cache expiration time in milliseconds.
# Weather data is considered fresh if less than this time has passed since the last update.
# Default: 600000 (10 minutes).
cache.expiration.time=600000

# Maximum number of cities stored in the cache.
# When exceeded, the oldest entry is removed (LRU policy).
# Default: 10.
```
### Obtaining an API Key
1. Register at [openweathermap.org](https://openweathermap.org/).
2. Go to "API keys" in your profile and generate a new key.
3. Replace `your-api-key-here` in `config.properties` with your key.

## Usage

### Basic Example
Fetches weather data using settings from `config.properties`:
```java
import ru.vlad.simpleweather.WeatherSDK;
import ru.vlad.simpleweather.WeatherData;

public class Main {
    public static void main(String[] args) {
        try {
            WeatherSDK sdk = new WeatherSDK();
            WeatherData weather = sdk.getWeather("Moscow");
            System.out.println("City: " + weather.getName());
            System.out.println("Temperature: " + weather.getMain().getTemp() + "°C");
            System.out.println("Weather: " + weather.getWeather().get(0).getMain() + " (" + weather.getWeather().get(0).getDescription() + ")");
            System.out.println("Wind Speed: " + weather.getWind().getSpeed() + " m/s");
            sdk.destroy();
        } catch (WeatherSDKException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
```
### Dependencies
The SDK relies on the following libraries (included via Maven):
- **Spring Web** (`5.3.31`): For HTTP requests via `RestTemplate`.
- **Jackson Databind** (`2.15.2`): For JSON parsing.
- **Lombok** (`1.18.30`): For reducing boilerplate code in data classes.
- **SLF4J** (`1.7.36`) with **Logback** (`1.2.11`): For logging.
- **JUnit Jupiter** (`5.10.1`) and **Mockito** (`5.7.0`): For unit testing.

See `pom.xml` for the full list:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
        <version>5.3.31</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.30</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.2.11</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.10.1</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>5.7.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```
## Running Tests
Unit tests are included to verify SDK functionality:
1. Build the project:
   mvn clean install
2. Run tests:
   mvn test

Tests include:
- `testGetWeatherSuccess`: Verifies successful weather data retrieval.
- `testInvalidCity`: Ensures an exception is thrown for an empty city name.

## Building and Running
- Build the JAR:
  mvn clean install
- Run the example:
  java -cp target/simpleweather-1.0.0.jar ru.vlad.simpleweather.Main

## Notes
- Ensure an active internet connection and a valid API key in `config.properties`.
- The SDK requires Java 11 or higher (configured in `pom.xml`).

## License
This project is unlicensed and provided as-is for educational purposes.