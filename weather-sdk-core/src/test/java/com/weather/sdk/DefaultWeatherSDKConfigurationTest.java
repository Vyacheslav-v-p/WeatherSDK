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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.exception.ConfigurationException;
import com.weather.sdk.model.SystemInfo;
import com.weather.sdk.model.Temperature;
import com.weather.sdk.model.Weather;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.Wind;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK configuration-related functionality. */
@DisplayName("DefaultWeatherSDK Configuration Tests")
class DefaultWeatherSDKConfigurationTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("SDK should return configuration")
  void sdkShouldReturnConfiguration() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);
    assertEquals(config, testSdk.getConfiguration());
    testSdk.shutdown();
  }

  @Test
  @DisplayName("SDK should handle different configuration modes")
  void sdkShouldHandleDifferentConfigurationModes() {
    try {
      // Test ON_DEMAND mode
      final SDKConfiguration onDemandConfig =
          new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();

      assertEquals(SDKMode.ON_DEMAND, onDemandConfig.getMode());

      // Test POLLING mode
      final SDKConfiguration pollingConfig =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.POLLING)
              .pollingInterval(Duration.ofMinutes(5))
              .build();

      assertEquals(SDKMode.POLLING, pollingConfig.getMode());
      assertEquals(Duration.ofMinutes(5), pollingConfig.getPollingInterval());
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("SDK configuration should validate parameters")
  void sdkConfigurationShouldValidateParameters() {
    // Test invalid API key
    assertThrows(ConfigurationException.class, () -> new WeatherSDKBuilder().apiKey("").build());

    assertThrows(ConfigurationException.class, () -> new WeatherSDKBuilder().apiKey(null).build());

    // Test invalid cache size
    assertThrows(
        ConfigurationException.class,
        () -> new WeatherSDKBuilder().apiKey(TEST_API_KEY).cacheSize(0).build());

    // Test invalid TTL
    assertThrows(
        ConfigurationException.class,
        () -> new WeatherSDKBuilder().apiKey(TEST_API_KEY).cacheTTL(Duration.ofMinutes(0)).build());
  }

  @Test
  @DisplayName("SDK should provide meaningful toString representation")
  void sdkShouldProvideMeaningfulToStringRepresentation() {
    try {
      final SDKConfiguration config =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.ON_DEMAND)
              .cacheSize(20)
              .cacheTTL(Duration.ofMinutes(15))
              .build();

      final String configString = config.toString();
      assertTrue(configString.contains("mode=on-demand"));
      assertTrue(configString.contains("cacheSize=20"));
      assertTrue(configString.contains("cacheTTL="));
      assertFalse(configString.contains(TEST_API_KEY), "API key should be masked");
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("SDK configuration should implement equals and hashCode correctly")
  void sdkConfigurationShouldImplementEqualsAndHashCodeCorrectly() {
    try {
      final SDKConfiguration config1 =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.ON_DEMAND)
              .cacheSize(10)
              .build();

      final SDKConfiguration config2 =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.ON_DEMAND)
              .cacheSize(10)
              .build();

      final SDKConfiguration config3 =
          new WeatherSDKBuilder()
              .apiKey(TEST_API_KEY)
              .mode(SDKMode.POLLING) // Different mode
              .cacheSize(10)
              .build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
      assertNotEquals(config1, config3);
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
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
