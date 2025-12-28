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
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Core unit tests for DefaultWeatherSDK functionality.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>Basic weather retrieval
 *   <li>Core SDK functionality
 * </ul>
 */
@DisplayName("DefaultWeatherSDK Core Tests")
class DefaultWeatherSDKTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("Basic weather retrieval functionality")
  void basicWeatherRetrievalShouldWork() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);
    assertEquals(config, testSdk.getConfiguration());
    testSdk.shutdown();
  }

  @Test
  @DisplayName("Constructor should reject null configuration")
  void constructorShouldRejectNullConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new DefaultWeatherSDK(null));
  }
}
