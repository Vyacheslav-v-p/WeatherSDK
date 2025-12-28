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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.WeatherSDKBuilder;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for DefaultWeatherSDK metrics functionality. */
@DisplayName("DefaultWeatherSDK Metrics Tests")
class DefaultWeatherSDKMetricsTest {

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  @Test
  @DisplayName("getMetrics should return valid metrics object")
  void getMetricsShouldReturnValidMetricsObject() throws Exception {
    SDKConfiguration config =
        new WeatherSDKBuilder().apiKey(TEST_API_KEY).mode(SDKMode.ON_DEMAND).build();
    DefaultWeatherSDK testSdk = new DefaultWeatherSDK(config);

    SDKMetrics metrics = testSdk.getMetrics();
    assertNotNull(metrics);
    assertEquals(0, metrics.getTotalRequests());
    assertEquals(0, metrics.getCacheHits());
    assertEquals(0, metrics.getCacheMisses());
    assertEquals(0, metrics.getSuccessfulRequests());
    assertEquals(0, metrics.getFailedRequests());
    assertEquals(0.0, metrics.getAverageResponseTimeMs());
    assertEquals(0.0, metrics.getCacheHitRate());
    assertEquals(0.0, metrics.getSuccessRate());

    testSdk.shutdown();
  }

  @Test
  @DisplayName("SDK metrics should calculate rates correctly")
  void sdkMetricsShouldCalculateRatesCorrectly() {
    // Create a metrics instance with known values
    final SDKMetrics metrics =
        new SDKMetrics(
            100, // totalRequests
            90, // successCount
            10, // failedRequests
            70, // cacheHits
            30, // cacheMisses
            250.5, // avgResponseMs
            Instant.now());

    // Test cache hit rate calculation
    final double cacheHitRate = metrics.getCacheHitRate();
    assertEquals(70.0, cacheHitRate, 0.01, "Cache hit rate should be 70%");

    // Test success rate calculation
    final double successRate = metrics.getSuccessRate();
    assertEquals(90.0, successRate, 0.01, "Success rate should be 90%");

    // Test with zero operations
    final SDKMetrics emptyMetrics = new SDKMetrics(0, 0, 0, 0, 0, 0.0, null);
    assertEquals(
        0.0, emptyMetrics.getCacheHitRate(), "Cache hit rate should be 0% for empty metrics");
    assertEquals(0.0, emptyMetrics.getSuccessRate(), "Success rate should be 0% for empty metrics");
  }

  @Test
  @DisplayName("SDK metrics should handle edge cases correctly")
  void sdkMetricsShouldHandleEdgeCasesCorrectly() {
    // Test with only cache hits
    final SDKMetrics hitsOnlyMetrics = new SDKMetrics(50, 50, 0, 50, 0, 200.0, Instant.now());
    assertEquals(100.0, hitsOnlyMetrics.getCacheHitRate(), "Cache hit rate should be 100%");
    assertEquals(100.0, hitsOnlyMetrics.getSuccessRate(), "Success rate should be 100%");

    // Test with only cache misses
    final SDKMetrics missesOnlyMetrics = new SDKMetrics(50, 50, 0, 0, 50, 300.0, Instant.now());
    assertEquals(0.0, missesOnlyMetrics.getCacheHitRate(), "Cache hit rate should be 0%");
    assertEquals(100.0, missesOnlyMetrics.getSuccessRate(), "Success rate should be 100%");

    // Test with null last call time
    final SDKMetrics nullTimeMetrics = new SDKMetrics(10, 8, 2, 5, 5, 150.0, null);
    assertNull(nullTimeMetrics.getLastApiCallTime(), "Should handle null last call time");
  }

  @Test
  @DisplayName("SDK metrics should provide meaningful string representation")
  void sdkMetricsShouldProvideMeaningfulStringRepresentation() {
    final SDKMetrics metrics =
        new SDKMetrics(100, 90, 10, 70, 30, 250.5, Instant.ofEpochSecond(1675751262));

    final String metricsString = metrics.toString();
    assertTrue(metricsString.contains("totalRequests=100"));
    assertTrue(metricsString.contains("successCount=90"));
    assertTrue(metricsString.contains("failedRequests=10"));
    assertTrue(metricsString.contains("cacheHits=70"));
    assertTrue(metricsString.contains("cacheMisses=30"));
    assertTrue(metricsString.contains("avgResponseMs=250.50"));
  }
}
