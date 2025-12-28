/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.weather.sdk.model.SystemInfo;
import com.weather.sdk.model.Temperature;
import com.weather.sdk.model.Weather;
import com.weather.sdk.model.WeatherData;
import com.weather.sdk.model.Wind;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for DefaultWeatherCache.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>LRU eviction behavior
 *   <li>TTL expiration scenarios
 *   <li>Cache hit/miss patterns
 *   <li>Thread-safety and concurrent access
 *   <li>Edge cases and boundary conditions
 * </ul>
 */
@DisplayName("DefaultWeatherCache Tests")
class DefaultWeatherCacheTest {

  /** The cache instance under test. */
  private WeatherCache cache;

  /** Sample weather data for London. */
  private WeatherData londonWeather;

  /** Sample weather data for Paris. */
  private WeatherData parisWeather;

  /** Sample weather data for Tokyo. */
  private WeatherData tokyoWeather;

  @BeforeEach
  void setUp() {
    cache = new DefaultWeatherCache(Duration.ofMinutes(10), 3);
    londonWeather = createWeatherData("London", 288.15); // 15°C in Kelvin
    parisWeather = createWeatherData("Paris", 291.15); // 18°C in Kelvin
    tokyoWeather = createWeatherData("Tokyo", 295.15); // 22°C in Kelvin
  }

  // ==================== Constructor Tests ====================

  @Test
  @DisplayName("Constructor should reject null TTL")
  void constructorShouldRejectNullTtl() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> new DefaultWeatherCache(null, 10));
    assertEquals("TTL must be positive and not null", exception.getMessage());
  }

  @Test
  @DisplayName("Constructor should reject negative TTL")
  void constructorShouldRejectNegativeTtl() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new DefaultWeatherCache(Duration.ofMinutes(-1), 10));
    assertEquals("TTL must be positive and not null", exception.getMessage());
  }

  @Test
  @DisplayName("Constructor should reject zero TTL")
  void constructorShouldRejectZeroTtl() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> new DefaultWeatherCache(Duration.ZERO, 10));
    assertEquals("TTL must be positive and not null", exception.getMessage());
  }

  @Test
  @DisplayName("Constructor should reject zero max size")
  void constructorShouldRejectZeroMaxSize() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new DefaultWeatherCache(Duration.ofMinutes(10), 0));
    assertEquals("Max size must be positive", exception.getMessage());
  }

  @Test
  @DisplayName("Constructor should reject negative max size")
  void constructorShouldRejectNegativeMaxSize() {
    final IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> new DefaultWeatherCache(Duration.ofMinutes(10), -1));
    assertEquals("Max size must be positive", exception.getMessage());
  }

  // ==================== Basic Cache Operations ====================

  @Test
  @DisplayName("Get should return empty for non-existent key")
  void getShouldReturnEmptyForNonExistentKey() {
    final Optional<WeatherData> result = cache.get("NonExistent");
    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Put and get should work correctly")
  void putAndGetShouldWorkCorrectly() {
    cache.put("London", londonWeather);
    final Optional<WeatherData> result = cache.get("London");

    assertTrue(result.isPresent());
    assertEquals("London", result.get().getName());
    assertEquals(288.15, result.get().getTemperature().getTemp(), 0.01);
  }

  @Test
  @DisplayName("Put should reject null weather data")
  void putShouldRejectNullWeatherData() {
    final IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> cache.put("London", null));
    assertEquals("Weather data must not be null", exception.getMessage());
  }

  @Test
  @DisplayName("Size should return correct count")
  void sizeShouldReturnCorrectCount() {
    assertEquals(0, cache.size());

    cache.put("London", londonWeather);
    assertEquals(1, cache.size());

    cache.put("Paris", parisWeather);
    assertEquals(2, cache.size());

    cache.put("Tokyo", tokyoWeather);
    assertEquals(3, cache.size());
  }

  @Test
  @DisplayName("Clear should remove all entries")
  void clearShouldRemoveAllEntries() {
    cache.put("London", londonWeather);
    cache.put("Paris", parisWeather);
    cache.put("Tokyo", tokyoWeather);
    assertEquals(3, cache.size());

    cache.clear();
    assertEquals(0, cache.size());
    assertFalse(cache.get("London").isPresent());
    assertFalse(cache.get("Paris").isPresent());
    assertFalse(cache.get("Tokyo").isPresent());
  }

  @Test
  @DisplayName("Evict should remove specific entry")
  void evictShouldRemoveSpecificEntry() {
    cache.put("London", londonWeather);
    cache.put("Paris", parisWeather);
    assertEquals(2, cache.size());

    cache.evict("London");
    assertEquals(1, cache.size());
    assertFalse(cache.get("London").isPresent());
    assertTrue(cache.get("Paris").isPresent());
  }

  @Test
  @DisplayName("Evict should be no-op for non-existent key")
  void evictShouldBeNoOpForNonExistentKey() {
    cache.put("London", londonWeather);
    assertEquals(1, cache.size());

    cache.evict("NonExistent");
    assertEquals(1, cache.size());
    assertTrue(cache.get("London").isPresent());
  }

  // ==================== Cache Key Normalization ====================

  @Test
  @DisplayName("Cache keys should be case-insensitive")
  void cacheKeysShouldBeCaseInsensitive() {
    cache.put("London", londonWeather);

    assertTrue(cache.get("london").isPresent());
    assertTrue(cache.get("LONDON").isPresent());
    assertTrue(cache.get("LoNdOn").isPresent());
  }

  @Test
  @DisplayName("Cache keys should be trimmed")
  void cacheKeysShouldBeTrimmed() {
    cache.put("  London  ", londonWeather);

    assertTrue(cache.get("London").isPresent());
    assertTrue(cache.get("  London").isPresent());
    assertTrue(cache.get("London  ").isPresent());
  }

  @Test
  @DisplayName("Put should update existing entry")
  void putShouldUpdateExistingEntry() {
    cache.put("London", londonWeather);
    final WeatherData updatedWeather = createWeatherData("London", 293.15); // 20°C
    cache.put("London", updatedWeather);

    assertEquals(1, cache.size());
    final Optional<WeatherData> result = cache.get("London");
    assertTrue(result.isPresent());
    assertEquals(293.15, result.get().getTemperature().getTemp(), 0.01);
  }

  // ==================== LRU Eviction Tests ====================

  @Test
  @DisplayName("LRU eviction should remove least recently used entry when cache is full")
  void lruEvictionShouldRemoveLeastRecentlyUsedEntry() {
    // Cache max size is 3
    cache.put("London", londonWeather);
    cache.put("Paris", parisWeather);
    cache.put("Tokyo", tokyoWeather);
    assertEquals(3, cache.size());

    // Add 4th entry, should evict London (least recently used)
    final WeatherData newYorkWeather = createWeatherData("NewYork", 283.15); // 10°C
    cache.put("NewYork", newYorkWeather);

    assertEquals(3, cache.size());
    assertFalse(cache.get("London").isPresent(), "London should be evicted");
    assertTrue(cache.get("Paris").isPresent());
    assertTrue(cache.get("Tokyo").isPresent());
    assertTrue(cache.get("NewYork").isPresent());
  }

  @Test
  @DisplayName("LRU eviction should consider get() as access")
  void lruEvictionShouldConsiderGetAsAccess() {
    cache.put("London", londonWeather);
    cache.put("Paris", parisWeather);
    cache.put("Tokyo", tokyoWeather);

    // Access London to make it most recently used
    cache.get("London");

    // Add 4th entry, should evict Paris (now least recently used)
    final WeatherData newYorkWeather = createWeatherData("NewYork", 283.15);
    cache.put("NewYork", newYorkWeather);

    assertEquals(3, cache.size());
    assertTrue(cache.get("London").isPresent(), "London should not be evicted");
    assertFalse(cache.get("Paris").isPresent(), "Paris should be evicted");
    assertTrue(cache.get("Tokyo").isPresent());
    assertTrue(cache.get("NewYork").isPresent());
  }

  @Test
  @DisplayName("LRU eviction should work with multiple evictions")
  void lruEvictionShouldWorkWithMultipleEvictions() {
    cache.put("City1", createWeatherData("City1", 283.15));
    cache.put("City2", createWeatherData("City2", 284.15));
    cache.put("City3", createWeatherData("City3", 285.15));

    // Add more entries to trigger multiple evictions
    cache.put("City4", createWeatherData("City4", 286.15));
    cache.put("City5", createWeatherData("City5", 287.15));

    assertEquals(3, cache.size());
    assertFalse(cache.get("City1").isPresent());
    assertFalse(cache.get("City2").isPresent());
    assertTrue(cache.get("City3").isPresent());
    assertTrue(cache.get("City4").isPresent());
    assertTrue(cache.get("City5").isPresent());
  }

  // ==================== TTL Expiration Tests ====================

  @Test
  @DisplayName("TTL expiration should remove expired entries")
  void ttlExpirationShouldRemoveExpiredEntries() throws InterruptedException {
    final WeatherCache shortTtlCache = new DefaultWeatherCache(Duration.ofMillis(100), 10);
    shortTtlCache.put("London", londonWeather);

    // Entry should exist immediately
    assertTrue(shortTtlCache.get("London").isPresent());

    // Wait for TTL to expire
    Thread.sleep(150);

    // Entry should be expired and removed
    assertFalse(shortTtlCache.get("London").isPresent());
    assertEquals(0, shortTtlCache.size(), "Expired entry should be removed from cache");
  }

  @Test
  @DisplayName("TTL expiration should not affect non-expired entries")
  void ttlExpirationShouldNotAffectNonExpiredEntries() throws InterruptedException {
    final WeatherCache shortTtlCache = new DefaultWeatherCache(Duration.ofMillis(150), 10);
    shortTtlCache.put("London", londonWeather);

    Thread.sleep(50);
    shortTtlCache.put("Paris", parisWeather);

    Thread.sleep(120);

    // London should be expired (170ms elapsed), Paris should still be valid (120ms
    // elapsed)
    assertFalse(shortTtlCache.get("London").isPresent());
    assertTrue(shortTtlCache.get("Paris").isPresent());
  }

  @Test
  @DisplayName("TTL expiration should work with cache updates")
  void ttlExpirationShouldWorkWithCacheUpdates() throws InterruptedException {
    final WeatherCache shortTtlCache = new DefaultWeatherCache(Duration.ofMillis(100), 10);
    shortTtlCache.put("London", londonWeather);

    Thread.sleep(60);

    // Update entry with new data (resets TTL)
    final WeatherData updatedWeather = createWeatherData("London", 293.15);
    shortTtlCache.put("London", updatedWeather);

    Thread.sleep(60);

    // Entry should still be valid (TTL was reset)
    final Optional<WeatherData> result = shortTtlCache.get("London");
    assertTrue(result.isPresent());
    assertEquals(293.15, result.get().getTemperature().getTemp(), 0.01);
  }

  // ==================== Thread Safety Tests ====================

  @Test
  @DisplayName("Concurrent puts should be thread-safe")
  void concurrentPutsShouldBeThreadSafe() throws InterruptedException {
    final int threadCount = 10;
    final int operationsPerThread = 100;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < operationsPerThread; j++) {
                final String cityName = "City" + threadId + "_" + j;
                cache.put(cityName, createWeatherData(cityName, 283.15 + j));
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    // Cache should not exceed max size due to LRU eviction
    assertTrue(cache.size() <= 3, "Cache size should not exceed max size");
  }

  @Test
  @DisplayName("Concurrent gets and puts should be thread-safe")
  void concurrentGetsAndPutsShouldBeThreadSafe() throws InterruptedException {
    final int threadCount = 10;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);
    final List<Exception> exceptions = new ArrayList<>();

    // Pre-populate cache
    cache.put("SharedCity", londonWeather);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < 100; j++) {
                if (j % 2 == 0) {
                  cache.get("SharedCity");
                } else {
                  cache.put("SharedCity", createWeatherData("SharedCity", 283.15 + j));
                }
              }
            } catch (final Exception e) {
              exceptions.add(e);
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    assertTrue(exceptions.isEmpty(), "No exceptions should occur during concurrent access");
    assertTrue(cache.get("SharedCity").isPresent(), "Entry should still exist");
  }

  @Test
  @DisplayName("Concurrent evictions should be thread-safe")
  void concurrentEvictionsShouldBeThreadSafe() throws InterruptedException {
    final int threadCount = 5;
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final CountDownLatch latch = new CountDownLatch(threadCount);

    // Pre-populate cache
    for (int i = 0; i < 10; i++) {
      cache.put("City" + i, createWeatherData("City" + i, 283.15 + i));
    }

    for (int i = 0; i < threadCount; i++) {
      executor.submit(
          () -> {
            try {
              for (int j = 0; j < 10; j++) {
                cache.evict("City" + j);
              }
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

    // All entries should be evicted
    assertEquals(0, cache.size());
  }

  // ==================== Edge Cases and Boundary Conditions ====================

  @Test
  @DisplayName("Cache with size 1 should work correctly")
  void cacheWithSizeOneShouldWorkCorrectly() {
    final WeatherCache smallCache = new DefaultWeatherCache(Duration.ofMinutes(10), 1);

    smallCache.put("London", londonWeather);
    assertEquals(1, smallCache.size());
    assertTrue(smallCache.get("London").isPresent());

    // Adding second entry should evict first
    smallCache.put("Paris", parisWeather);
    assertEquals(1, smallCache.size());
    assertFalse(smallCache.get("London").isPresent());
    assertTrue(smallCache.get("Paris").isPresent());
  }

  @Test
  @DisplayName("Get should handle null city name")
  void getShouldHandleNullCityName() {
    assertThrows(IllegalArgumentException.class, () -> cache.get(null));
  }

  @Test
  @DisplayName("Put should handle null city name")
  void putShouldHandleNullCityName() {
    assertThrows(IllegalArgumentException.class, () -> cache.put(null, londonWeather));
  }

  @Test
  @DisplayName("Evict should handle null city name")
  void evictShouldHandleNullCityName() {
    assertThrows(IllegalArgumentException.class, () -> cache.evict(null));
  }

  @Test
  @DisplayName("Empty string city name should work")
  void emptyStringCityNameShouldWork() {
    final WeatherData emptyNameWeather = createWeatherData("", 283.15);
    cache.put("", emptyNameWeather);
    assertTrue(cache.get("").isPresent());
  }

  @Test
  @DisplayName("Very long TTL should work")
  void veryLongTtlShouldWork() {
    final WeatherCache longTtlCache = new DefaultWeatherCache(Duration.ofDays(365), 10);
    longTtlCache.put("London", londonWeather);
    assertTrue(longTtlCache.get("London").isPresent());
  }

  @Test
  @DisplayName("Very short TTL should work")
  void veryShortTtlShouldWork() throws InterruptedException {
    final WeatherCache shortTtlCache = new DefaultWeatherCache(Duration.ofMillis(1), 10);
    shortTtlCache.put("London", londonWeather);

    Thread.sleep(10);

    assertFalse(shortTtlCache.get("London").isPresent());
  }

  // ==================== Helper Methods ====================

  /**
   * Creates a sample WeatherData object for testing.
   *
   * @param cityName the city name
   * @param temperature the temperature in Kelvin
   * @return a WeatherData object
   */
  private WeatherData createWeatherData(final String cityName, final double temperature) {
    final Weather weather = new Weather("Clear", "clear sky");
    final Temperature temp = new Temperature(temperature, temperature - 2);
    final Wind wind = new Wind(3.5, 180);
    final long currentTime = System.currentTimeMillis() / 1000;
    final SystemInfo systemInfo = new SystemInfo(currentTime - 3600, currentTime + 3600);

    return new WeatherData(
        java.util.Collections.singletonList(weather),
        temp,
        10000,
        wind,
        currentTime,
        systemInfo,
        0,
        cityName);
  }
}
