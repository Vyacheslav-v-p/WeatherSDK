/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.model.SystemInfo;
import com.weather.sdk.model.Temperature;
import com.weather.sdk.model.Weather;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.Wind;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK cache integration functionality. */
@DisplayName("DefaultWeatherSDK Cache Tests")
class DefaultWeatherSDKCacheTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("getWeather should check cache first")
  void getWeatherShouldCheckCacheFirst() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder()
            .apiKey(TEST_API_KEY)
            .mode(SDKMode.ON_DEMAND)
            .cacheSize(10)
            .cacheTTL(Duration.ofMinutes(10))
            .build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    // Use reflection to access cache
    java.lang.reflect.Field cacheField = DefaultWeatherSDK.class.getDeclaredField("weatherCache");
    cacheField.setAccessible(true);
    com.weather.sdk.cache.WeatherCache cache =
        (com.weather.sdk.cache.WeatherCache) cacheField.get(testSdk);

    // Pre-populate cache with weather data
    WeatherData weatherData = createTestWeatherData("London");
    cache.put("london", weatherData);

    // SDK should return cached data without making API call
    WeatherData result = testSdk.getWeather("London");
    assertEquals("London", result.getName());

    // Metrics should show a cache hit
    SDKMetrics metrics = testSdk.getMetrics();
    assertEquals(1, metrics.getTotalRequests());
    assertEquals(1, metrics.getCacheHits());
    assertEquals(0, metrics.getCacheMisses());

    testSdk.shutdown();
  }

  @Test
  @DisplayName("Metrics should track cache operations correctly")
  void metricsShouldTrackCacheOperationsCorrectly() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder()
            .apiKey(TEST_API_KEY)
            .mode(SDKMode.ON_DEMAND)
            .cacheSize(10)
            .cacheTTL(Duration.ofMinutes(10))
            .build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    // Test metrics with no operations
    SDKMetrics metrics1 = testSdk.getMetrics();
    assertEquals(0, metrics1.getTotalRequests());
    assertEquals(0, metrics1.getCacheHits());
    assertEquals(0, metrics1.getCacheMisses());

    // Use reflection to access cache and manually add data
    java.lang.reflect.Field cacheField = DefaultWeatherSDK.class.getDeclaredField("weatherCache");
    cacheField.setAccessible(true);
    com.weather.sdk.cache.WeatherCache cache =
        (com.weather.sdk.cache.WeatherCache) cacheField.get(testSdk);
    WeatherData weatherData = createTestWeatherData("Paris");
    cache.put("paris", weatherData);

    // Access cached data
    testSdk.getWeather("Paris");

    // Verify metrics were updated
    SDKMetrics metrics2 = testSdk.getMetrics();
    assertEquals(1, metrics2.getTotalRequests());
    assertEquals(1, metrics2.getCacheHits());
    assertEquals(0, metrics2.getCacheMisses());

    testSdk.shutdown();
  }

  /**
   * Creates a test WeatherData instance for the specified city.
   *
   * @param cityName the name of the city for the test data
   * @return a WeatherData instance for testing
   */
  private WeatherData createTestWeatherData(final String cityName) {
    final Weather weather = new Weather("Clear", "clear sky");
    final Temperature temperature = new Temperature(294.15, 293.15);
    final Wind wind = new Wind(3.5, 180);
    final SystemInfo systemInfo = new SystemInfo(1675751262L, 1675787560L);
    return new WeatherData(
        java.util.Collections.singletonList(weather),
        temperature,
        1000,
        wind,
        1675751262L,
        systemInfo,
        3600,
        cityName);
  }
}
