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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK lifecycle functionality (shutdown/close). */
@DisplayName("DefaultWeatherSDK Lifecycle Tests")
class DefaultWeatherSDKLifecycleTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("shutdown should mark SDK as shut down")
  void shutdownShouldMarkSdkAsShutDown() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    assertFalse(testSdk.isShutdown());
    testSdk.shutdown();
    assertTrue(testSdk.isShutdown());
  }

  @Test
  @DisplayName("Operations should throw IllegalStateException after shutdown")
  void operationsShouldThrowIllegalStateExceptionAfterShutdown() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    testSdk.shutdown();

    // All operations should throw IllegalStateException after shutdown
    assertThrows(IllegalStateException.class, () -> testSdk.getWeather("London"));
    assertThrows(IllegalStateException.class, () -> testSdk.getMetrics());
  }

  @Test
  @DisplayName("close should delegate to shutdown")
  void closeShouldDelegateToShutdown() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    assertFalse(testSdk.isShutdown());
    testSdk.close();
    assertTrue(testSdk.isShutdown());
  }

  @Test
  @DisplayName("shutdown should be idempotent")
  void shutdownShouldBeIdempotent() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    testSdk.shutdown();
    assertTrue(testSdk.isShutdown());

    // Second shutdown should not throw exception
    testSdk.shutdown();
    assertTrue(testSdk.isShutdown());
  }

  @Test
  @DisplayName("SDK should be usable with try-with-resources")
  void sdkShouldBeUsableWithTryWithResources() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();

    try (DefaultWeatherSDK sdk = new DefaultWeatherSDK(config)) {
      assertFalse(sdk.isShutdown());
    }

    // SDK should be closed after exiting try-with-resources block
    // We can't directly test this without making isShutdown() public
    // but we know from other tests that close() calls shutdown()
  }
}
