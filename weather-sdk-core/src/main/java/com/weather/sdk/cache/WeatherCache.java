/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.cache;

import com.weather.sdk.model.WeatherData;
import java.util.Optional;

/**
 * Interface contract for weather data caching with LRU eviction and TTL (Time-To-Live) support.
 *
 * <p>This interface defines the contract for caching weather data with intelligent eviction
 * policies and automatic expiration to ensure data freshness while optimizing API usage.
 *
 * <h2>Core Features</h2>
 *
 * <ul>
 *   <li><strong>LRU Eviction</strong>: When cache reaches maximum size, automatically evicts the
 *       least recently used entry
 *   <li><strong>TTL Support</strong>: Each cache entry has a configurable time-to-live and
 *       automatically expires after the TTL duration
 *   <li><strong>Thread Safety</strong>: All operations are thread-safe and can be called
 *       concurrently by multiple threads
 *   <li><strong>Cache Key Normalization</strong>: City names are normalized to lowercase, trimmed
 *       format for consistent lookup
 * </ul>
 *
 * <h2>LRU Eviction Policy</h2>
 *
 * <p>The cache implements a Least Recently Used (LRU) eviction strategy:
 *
 * <ul>
 *   <li>When the cache reaches its maximum size, the least recently accessed entry is removed
 *   <li>Access order is updated by both {@code get()} and {@code put()} operations
 *   <li>Eviction happens automatically during {@code put()} operations when the cache is full
 *   <li>The eviction is atomic and thread-safe
 * </ul>
 *
 * <h2>TTL (Time-To-Live) Behavior</h2>
 *
 * <p>Each cached entry includes a timestamp indicating when it was cached:
 *
 * <ul>
 *   <li>When retrieving data via {@code get()}, the cache checks if the entry is stale
 *   <li>An entry is stale if: current_time - cached_time > TTL duration
 *   <li>Stale entries return {@link Optional#empty()} and are automatically removed
 *   <li>Default TTL: 10 minutes (configurable via {@link com.weather.sdk.config.SDKConfiguration})
 * </ul>
 *
 * <h2>Cache Key Normalization</h2>
 *
 * <p>Cache keys are automatically normalized to ensure consistent lookup:
 *
 * <ul>
 *   <li>City names are converted to lowercase
 *   <li>Leading and trailing whitespace is trimmed
 *   <li>Example: "New York" → "new york"
 *   <li>This ensures "New York" and "new york" map to the same cache entry
 * </ul>
 *
 * <h2>Thread Safety Guarantees</h2>
 *
 * <p>This interface and its implementations are designed to be thread-safe:
 *
 * <ul>
 *   <li>Multiple threads can call methods concurrently without external synchronization
 *   <li>All operations (get, put, evict, clear) are atomic
 *   <li>LRU ordering is maintained correctly in multi-threaded environments
 *   <li>TTL expiration is checked safely during concurrent access
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * // Example usage with different cache operations
 * WeatherCache cache = new DefaultWeatherCache(Duration.ofMinutes(10), 100);
 *
 * // Store weather data
 * WeatherData weatherData = createWeatherDataFor("London");
 * cache.put("London", weatherData);
 *
 * // Retrieve weather data
 * Optional{@code <WeatherData>} cached = cache.get("London");
 * if (cached.isPresent()) {
 *   System.out.println("Cached weather: " + cached.get());
 * }
 *
 * // Remove specific entry
 * cache.evict("London");
 *
 * // Check cache size
 * int currentSize = cache.size();
 *
 * // Clear all entries
 * cache.clear();
 * </pre>
 *
 * @see WeatherData
 * @since 1.0.0
 */
public interface WeatherCache {

  /**
   * Retrieves weather data for the specified city from the cache.
   *
   * <p>This method performs the following operations atomically:
   *
   * <ol>
   *   <li>Normalizes the city name (lowercase, trimmed)
   *   <li>Checks if the entry exists and is not expired (TTL check)
   *   <li>If expired or not found, returns {@link Optional#empty()}
   *   <li>If valid, updates the access order (LRU) and returns the data
   * </ol>
   *
   * <p>Thread Safety: This method is thread-safe and can be called concurrently.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   * @return an {@link Optional} containing the cached weather data if found and valid, or {@link
   *     Optional#empty()} if not found or expired
   */
  Optional<WeatherData> get(String cityName);

  /**
   * Stores weather data for the specified city in the cache.
   *
   * <p>This method performs the following operations atomically:
   *
   * <ol>
   *   <li>Normalizes the city name (lowercase, trimmed)
   *   <li>Records the current timestamp for TTL calculation
   *   <li>If cache is full, evicts the least recently used entry
   *   <li>Stores the new entry with the current timestamp
   *   <li>Updates the access order (LRU)
   * </ol>
   *
   * <p>Thread Safety: This method is thread-safe and can be called concurrently.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   * @param weatherData the weather data to cache (must not be null)
   */
  void put(String cityName, WeatherData weatherData);

  /**
   * Evicts (removes) the weather data entry for the specified city from the cache.
   *
   * <p>This method performs the following operations atomically:
   *
   * <ol>
   *   <li>Normalizes the city name (lowercase, trimmed)
   *   <li>Removes the entry if it exists
   *   <li>If the entry doesn't exist, this operation has no effect
   * </ol>
   *
   * <p>Thread Safety: This method is thread-safe and can be called concurrently.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   */
  void evict(String cityName);

  /**
   * Clears all entries from the cache.
   *
   * <p>This method removes all cached weather data and resets the cache to its initial state. After
   * this operation, {@link #size()} will return 0.
   *
   * <p>Thread Safety: This method is thread-safe.
   */
  void clear();

  /**
   * Returns the current number of entries in the cache.
   *
   * <p>This includes both valid (non-expired) and expired entries that haven't been cleaned up yet.
   * Expired entries are automatically removed during {@code get()} operations.
   *
   * <p>Thread Safety: This method is thread-safe.
   *
   * @return the current number of entries in the cache
   */
  int size();

  /**
   * Returns a set of all cached city names.
   *
   * <p>This method provides access to all city names currently in the cache, which is useful for
   * operations like polling all cached cities.
   *
   * <p>Thread Safety: This method is thread-safe and returns an unmodifiable set to prevent
   * external modification of the cache's internal state.
   *
   * @return a set of all cached city names (normalized to lowercase)
   */
  java.util.Set<String> getAllCachedCities();
}
