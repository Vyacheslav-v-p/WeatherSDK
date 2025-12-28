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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK constructor. */
@DisplayName("DefaultWeatherSDK Constructor Tests")
class DefaultWeatherSDKConstructorTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("Constructor should reject null configuration")
  void constructorShouldRejectNullConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new DefaultWeatherSDK(null));
  }
}
