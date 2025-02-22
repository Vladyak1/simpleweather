import ru.vlad.simpleweather.WeatherSDK;
import ru.vlad.simpleweather.exception.WeatherSDKException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;
import ru.vlad.simpleweather.model.WeatherData;
import ru.vlad.simpleweather.model.enums.WeatherMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class WeatherSDKTest {
    private WeatherSDK sdk;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() throws WeatherSDKException {
        WeatherSDK.clearActiveKeys();
        restTemplate = Mockito.mock(RestTemplate.class);
        sdk = new WeatherSDK(WeatherMode.ON_DEMAND, null, restTemplate);
    }

    @AfterEach
    void tearDown() {
        sdk.destroy();
        WeatherSDK.clearActiveKeys();
    }

    @Test
    void testGetWeatherSuccess() throws WeatherSDKException {
        String mockResponse = "{\"weather\":[{\"main\":\"Clouds\",\"description\":\"broken clouds\"}],\"main\":{\"temp\":-0.76,\"feels_like\":-3.65},\"visibility\":10000,\"wind\":{\"speed\":2.25},\"dt\":1740226758,\"sys\":{\"sunrise\":1740199086,\"sunset\":1740235699},\"timezone\":10800,\"name\":\"Moscow\"}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(mockResponse);

        WeatherData data = sdk.getWeather("Moscow");
        assertEquals("Moscow", data.getName());
        assertEquals("Clouds", data.getWeather().get(0).getMain());
        assertEquals(-0.76, data.getMain().getTemp(), 0.01);
    }

    @Test
    void testInvalidCity() {
        assertThrows(WeatherSDKException.class, () -> sdk.getWeather(""));
    }
}