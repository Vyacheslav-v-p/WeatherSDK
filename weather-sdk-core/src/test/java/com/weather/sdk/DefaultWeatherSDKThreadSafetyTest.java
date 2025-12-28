/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK thread safety functionality. */
@DisplayName("DefaultWeatherSDK Thread Safety Tests")
class DefaultWeatherSDKThreadSafetyTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("Concurrent getWeather calls should be thread-safe")
  void concurrentGetWeatherCallsShouldBeThreadSafe() throws InterruptedException {
    // Create a mock API client that returns specific data for testing
    OpenWeatherApiClient testMockApiClient = mock(OpenWeatherApiClient.class);

    // Create test weather data
    WeatherData testWeatherData = createTestWeatherData("London");

    // Configure mock to return test data when called
    try {
      when(testMockApiClient.fetchWeather(anyString(), anyString())).thenReturn(testWeatherData);
    } catch (Exception e) {
      // Handle the exception by wrapping it in a RuntimeException
      throw new RuntimeException("Failed to configure mock", e);
    }

    // Create SDK configuration
    SDKConfiguration config;
    try {
      config =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.ON_DEMAND)
              .cacheSize(100)
              .cacheTTL(Duration.ofMinutes(10))
              .build();
    } catch (com.weather.sdk.exception.ConfigurationException e) {
      throw new RuntimeException("Failed to create configuration", e);
    }

    // Create SDK instance
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    // Use reflection to access and replace the API client
    try {
      java.lang.reflect.Field apiClientField =
          DefaultWeatherSDK.class.getDeclaredField("apiClient");
      apiClientField.setAccessible(true);
      apiClientField.set(testSdk, testMockApiClient);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set mock API client", e);
    }

    final int threadCount = 10;
    final int operationsPerThread = 5;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);
    final ArrayList<Exception> exceptions = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                String cityName = "City" + (j % 3); // Cycle through 3 cities
                try {
                  testSdk.getWeather(cityName);
                } catch (Exception e) {
                  // Capture any exceptions that occur
                  synchronized (exceptions) {
                    exceptions.add(e);
                  }
                }
              }
            } finally {
              latch.countDown();
            }
          });
    }

    // Wait for all threads to complete
    assertTrue(latch.await(10, TimeUnit.SECONDS), "All threads should complete within timeout");
    executor.shutdown();
    assertTrue(
        executor.awaitTermination(5, TimeUnit.SECONDS), "Executor should terminate properly");

    // Wait a bit more to ensure all metrics are properly recorded
    Thread.sleep(100);

    // Verify that no exceptions occurred during concurrent execution
    assertTrue(
        exceptions.isEmpty(),
        "No exceptions should occur during concurrent execution. First exception: "
            + (exceptions.isEmpty() ? "none" : exceptions.get(0).getMessage()));

    // SDK should still be functional after concurrent access
    assertFalse(testSdk.isShutdown());

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
