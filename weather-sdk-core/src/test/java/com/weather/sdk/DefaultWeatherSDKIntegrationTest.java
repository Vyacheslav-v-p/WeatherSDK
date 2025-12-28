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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.weather.sdk.client.OpenWeatherApiClient;
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

/** Integration tests for DefaultWeatherSDK workflow functionality. */
@DisplayName("DefaultWeatherSDK Integration Tests")
class DefaultWeatherSDKIntegrationTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("Full workflow: cache miss then cache hit")
  void fullWorkflowCacheMissThenCacheHit() throws Exception {
    // Create a mock API client that returns specific data for testing
    OpenWeatherApiClient testMockApiClient = mock(OpenWeatherApiClient.class);

    // Create test weather data
    WeatherData testWeatherData = createTestWeatherData("London");

    // Configure mock to return test data when called
    try {
      when(testMockApiClient.fetchWeather(eq("london"), anyString())).thenReturn(testWeatherData);
    } catch (Exception e) {
      // Handle the exception by wrapping it in a RuntimeException
      throw new RuntimeException("Failed to configure mock", e);
    }

    // Create SDK configuration with real cache but mocked API client
    SDKConfiguration config;
    try {
      config =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.ON_DEMAND)
              .cacheSize(10)
              .cacheTTL(Duration.ofMinutes(10))
              .build();
    } catch (com.weather.sdk.exception.ConfigurationException e) {
      throw new RuntimeException("Failed to create configuration", e);
    }

    // Create SDK instance with the configuration
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    // Use reflection to access and replace the API client immediately after
    // creation
    java.lang.reflect.Field apiClientField = DefaultWeatherSDK.class.getDeclaredField("apiClient");
    apiClientField.setAccessible(true);
    apiClientField.set(testSdk, testMockApiClient);

    // First call - should be a cache miss, triggering API call
    WeatherData result1 = testSdk.getWeather("London");
    assertEquals("London", result1.getName());

    // Second call - should be a cache hit, no additional API call needed
    WeatherData result2 = testSdk.getWeather("London");
    assertEquals("London", result2.getName());

    // Verify that the API client was only called once (for the cache miss)
    verify(testMockApiClient, times(1)).fetchWeather(eq("london"), anyString());

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
