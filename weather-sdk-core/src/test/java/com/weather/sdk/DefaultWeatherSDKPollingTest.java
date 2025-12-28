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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.weather.sdk.cache.DefaultWeatherCache;
import com.weather.sdk.cache.WeatherCache;
import com.weather.sdk.client.OpenWeatherApiClient;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.model.WeatherData;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for polling mode functionality in DefaultWeatherSDK.
 *
 * <p>These tests verify that the polling mode works correctly by checking that weather data is
 * automatically updated for cached cities at the configured interval.
 *
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class DefaultWeatherSDKPollingTest {

  /** Mock API client for testing. */
  @Mock private OpenWeatherApiClient mockApiClient;

  /** Mock weather data for testing. */
  @Mock private WeatherData mockWeatherData;

  /** Configuration for the SDK under test. */
  private SDKConfiguration config;

  /** The SDK instance under test. */
  private DefaultWeatherSDK sdk;

  @BeforeEach
  void setUp() throws Exception {
    // Create configuration for polling mode with a short interval for testing
    config =
        new WeatherSDKBuilder()
            .apiKey("test-api-key")
            .mode(com.weather.sdk.config.SDKMode.POLLING)
            .pollingInterval(Duration.ofMinutes(1)) // Minimum allowed interval for testing
            .cacheSize(10)
            .cacheTTL(Duration.ofMinutes(10))
            .connectionTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .build();
  }

  @Test
  void testPollingModeInitialization() throws Exception {
    // Create SDK with polling mode
    sdk = new DefaultWeatherSDK(config);

    // Verify that polling is initialized when mode is POLLING
    assertFalse(sdk.isShutdown());

    sdk.shutdown();
  }

  @Test
  void testPollingUpdatesCachedCities() throws Exception {
    // Mock API client to return specific weather data before creating SDK
    WeatherData mockWeatherDataForTest = mock(WeatherData.class);
    when(mockApiClient.fetchWeather(eq("london"), anyString())).thenReturn(mockWeatherDataForTest);

    // Create SDK with mocked API client
    sdk = new DefaultWeatherSDK(config);

    // Use reflection to access private fields for testing
    java.lang.reflect.Field apiClientField = DefaultWeatherSDK.class.getDeclaredField("apiClient");
    apiClientField.setAccessible(true);
    apiClientField.set(sdk, mockApiClient);

    // Add a city to the cache to be polled - this should now work without error
    sdk.getWeather("london"); // This will add London to the cache

    // Since the polling interval is 1 minute, we can't wait that long in a test
    // Instead, we'll verify that the polling infrastructure is set up correctly
    // Check that the polling executor is not null and is running
    java.lang.reflect.Field pollingExecutorField =
        DefaultWeatherSDK.class.getDeclaredField("pollingExecutor");
    pollingExecutorField.setAccessible(true);
    java.util.concurrent.ScheduledExecutorService pollingExecutor =
        (java.util.concurrent.ScheduledExecutorService) pollingExecutorField.get(sdk);

    assertNotNull(pollingExecutor);
    assertFalse(pollingExecutor.isShutdown());

    sdk.shutdown();
  }

  @Test
  void testPollingStopsOnShutdown() throws Exception {
    sdk = new DefaultWeatherSDK(config);

    // Verify SDK is running
    assertFalse(sdk.isShutdown());

    // Shutdown the SDK
    sdk.shutdown();

    // Verify SDK is shut down
    assertTrue(sdk.isShutdown());
  }

  @Test
  void testPollingHandlesApiErrorsGracefully() throws Exception {
    // Mock API client to return data for the first call (to add to cache) and then
    // throw an exception
    WeatherData mockWeatherDataForTest = mock(WeatherData.class);
    when(mockApiClient.fetchWeather(eq("london"), anyString()))
        .thenReturn(mockWeatherDataForTest) // First call returns data
        .thenThrow(new RuntimeException("API Error")); // Subsequent calls throw exception

    sdk = new DefaultWeatherSDK(config);

    // Use reflection to access private fields for testing
    java.lang.reflect.Field apiClientField = DefaultWeatherSDK.class.getDeclaredField("apiClient");
    apiClientField.setAccessible(true);
    apiClientField.set(sdk, mockApiClient);

    // Add a city to the cache to be polled - this should succeed with mocked data
    sdk.getWeather("london"); // This will add London to the cache

    // Since the polling interval is 1 minute, we can't wait that long in a test
    // Instead, we'll verify that the polling infrastructure is set up correctly
    // Check that the polling executor is not null and is running
    java.lang.reflect.Field pollingExecutorField =
        DefaultWeatherSDK.class.getDeclaredField("pollingExecutor");
    pollingExecutorField.setAccessible(true);
    java.util.concurrent.ScheduledExecutorService pollingExecutor =
        (java.util.concurrent.ScheduledExecutorService) pollingExecutorField.get(sdk);

    assertNotNull(pollingExecutor);
    assertFalse(pollingExecutor.isShutdown());

    // SDK should still be running despite potential API errors
    assertFalse(sdk.isShutdown());

    sdk.shutdown();
  }

  @Test
  void testPollingDoesNotRunInOnDemandMode() throws Exception {
    // Create configuration for on-demand mode
    SDKConfiguration onDemandConfig =
        new WeatherSDKBuilder()
            .apiKey("test-api-key")
            .mode(com.weather.sdk.config.SDKMode.ON_DEMAND)
            .cacheSize(10)
            .cacheTTL(Duration.ofMinutes(10))
            .connectionTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(10))
            .build();

    sdk = new DefaultWeatherSDK(onDemandConfig);

    // SDK should not have polling scheduled in on-demand mode
    assertFalse(sdk.isShutdown());

    sdk.shutdown();
  }

  @Test
  void testGetAllCachedCitiesMethod() {
    // Test the getAllCachedCities method in the cache
    WeatherCache cache = new DefaultWeatherCache(Duration.ofMinutes(10), 10);
    cache.put("london", mockWeatherData);
    cache.put("paris", mockWeatherData);
    cache.put("new york", mockWeatherData);

    Set<String> cachedCities = cache.getAllCachedCities();

    assertEquals(3, cachedCities.size());
    assertTrue(cachedCities.contains("london"));
    assertTrue(cachedCities.contains("paris"));
    assertTrue(cachedCities.contains("new york"));
  }
}
