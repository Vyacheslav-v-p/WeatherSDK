/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK edge cases and error conditions. */
@DisplayName("DefaultWeatherSDK Edge Cases Tests")
class DefaultWeatherSDKEdgeCasesTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("getWeather should validate city name")
  void getWeatherShouldValidateCityName() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    // Test null city name
    assertThrows(IllegalArgumentException.class, () -> testSdk.getWeather(null));

    // Test empty city name
    assertThrows(IllegalArgumentException.class, () -> testSdk.getWeather(""));

    // Test whitespace city name
    assertThrows(IllegalArgumentException.class, () -> testSdk.getWeather("   "));

    testSdk.shutdown();
  }
}
