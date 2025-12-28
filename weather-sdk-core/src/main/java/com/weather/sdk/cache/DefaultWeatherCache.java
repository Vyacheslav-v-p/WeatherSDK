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
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of the WeatherCache interface with LRU eviction and TTL support.
 *
 * <p>This class provides a thread-safe cache for weather data with the following features:
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
 * <p><strong>Implementation Details:</strong> This implementation uses a LinkedHashMap with
 * access-order enabled for LRU eviction. The {@code removeEldestEntry()} method automatically
 * removes the least recently used entry when the cache exceeds maximum size. All public methods are
 * synchronized to ensure thread safety.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * // Create cache with max 100 cities and 10-minute TTL
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
 * // Check cache size
 * int currentSize = cache.size();
 * </pre>
 *
 * @since 1.0.0
 * @see WeatherCache
 * @see WeatherData
 */
@SuppressWarnings("CT_CONSTRUCTOR_THROW")
public class DefaultWeatherCache implements WeatherCache {

  /** Maximum number of cities to cache. */
  private final int maxSize;

  /** Time-to-live for cached entries. */
  private final Duration ttl;

  /** Internal storage for cached entries with LRU ordering. */
  private final Map<String, CacheEntry> cache;

  /**
   * Constructs a new DefaultWeatherCache with the specified parameters.
   *
   * <p>This constructor creates a LinkedHashMap with access-order enabled for LRU eviction. When
   * the cache reaches maximum size, the least recently accessed entry is automatically removed.
   *
   * @param ttl the time-to-live for cached entries (must not be null or non-positive)
   * @param maxSize the maximum number of cities to cache (must be positive)
   * @throws IllegalArgumentException if ttl is null, negative, or zero, or if maxSize is not
   *     positive
   */
  @SuppressWarnings("CT_CONSTRUCTOR_THROW")
  public DefaultWeatherCache(final Duration ttl, final int maxSize) {
    // Initialize fields first to avoid partial initialization
    this.ttl = ttl;
    this.maxSize = maxSize;

    // Validate parameters after field initialization
    if (ttl == null || ttl.isNegative() || ttl.isZero()) {
      throw new IllegalArgumentException("TTL must be positive and not null");
    }
    if (maxSize <= 0) {
      throw new IllegalArgumentException("Max size must be positive");
    }

    // Create LinkedHashMap with access-order for LRU eviction
    // Initial capacity: maxSize + 1, load factor: 0.75f, accessOrder: true
    this.cache =
        new LinkedHashMap<>(maxSize + 1, 0.75f, true) {
          @Override
          protected boolean removeEldestEntry(final Map.Entry<String, CacheEntry> eldest) {
            // Automatically remove eldest entry when size exceeds maxSize
            return DefaultWeatherCache.this.size() > DefaultWeatherCache.this.maxSize;
          }
        };
  }

  /**
   * Normalizes a city name for consistent cache key lookup.
   *
   * <p>Normalizes by converting to lowercase and trimming whitespace.
   *
   * @param cityName the city name to normalize (must not be null)
   * @return the normalized city name
   */
  private String normalizeCityName(final String cityName) {
    if (cityName == null) {
      throw new IllegalArgumentException("City name must not be null");
    }
    return cityName.trim().toLowerCase(Locale.ROOT);
  }

  /**
   * Retrieves weather data for the specified city from the cache.
   *
   * <p>This method performs TTL expiration checks and automatically removes expired entries. The
   * access order is updated for LRU eviction when a valid entry is accessed.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   * @return an {@link Optional} containing the cached weather data if found and valid, or {@link
   *     Optional#empty()} if not found or expired
   */
  @Override
  public synchronized Optional<WeatherData> get(final String cityName) {
    final String normalizedKey = normalizeCityName(cityName);
    final CacheEntry entry = cache.get(normalizedKey);

    Optional<WeatherData> result = Optional.empty();

    if (entry != null) {
      // Check if entry has expired based on TTL
      final long currentTime = System.currentTimeMillis();
      final long entryAge = currentTime - entry.getTimestamp();
      final long ttlMillis = ttl.toMillis();

      if (entryAge > ttlMillis) {
        // Entry has expired, remove it
        cache.remove(normalizedKey);
      } else {
        // Entry is valid, return the data (access order updated automatically by
        // LinkedHashMap)
        result = Optional.of(entry.getData());
      }
    }

    return result;
  }

  /**
   * Stores weather data for the specified city in the cache.
   *
   * <p>This method records the current timestamp for TTL calculation. The LinkedHashMap
   * automatically handles LRU eviction when the cache exceeds maxSize.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   * @param weatherData the weather data to cache (must not be null)
   * @throws IllegalArgumentException if weatherData is null
   */
  @Override
  public synchronized void put(final String cityName, final WeatherData weatherData) {
    if (weatherData == null) {
      throw new IllegalArgumentException("Weather data must not be null");
    }

    final String normalizedKey = normalizeCityName(cityName);
    final CacheEntry entry = new CacheEntry(weatherData);

    // Store entry with current timestamp
    // LRU eviction handled automatically by LinkedHashMap's removeEldestEntry()
    cache.put(normalizedKey, entry);
  }

  /**
   * Evicts (removes) the weather data entry for the specified city from the cache.
   *
   * <p>This method removes the entry if it exists. If the entry doesn't exist, this operation has
   * no effect.
   *
   * @param cityName the city name (will be normalized to lowercase, trimmed)
   */
  @Override
  public synchronized void evict(final String cityName) {
    final String normalizedKey = normalizeCityName(cityName);
    cache.remove(normalizedKey);
  }

  /**
   * Clears all entries from the cache.
   *
   * <p>This method removes all cached weather data and resets the cache to its initial state. After
   * this operation, {@link #size()} will return 0.
   */
  @Override
  public synchronized void clear() {
    cache.clear();
  }

  /**
   * Returns the current number of entries in the cache.
   *
   * <p>This includes both valid (non-expired) and expired entries that haven't been cleaned up yet.
   * Expired entries are automatically removed during {@code get()} operations.
   *
   * @return the current number of entries in the cache
   */
  @Override
  public synchronized int size() {
    return cache.size();
  }

  /**
   * Returns a set of all cached city names.
   *
   * <p>This method provides access to all city names currently in the cache, which is useful for
   * operations like polling all cached cities.
   *
   * @return a set of all cached city names (normalized to lowercase)
   */
  @Override
  public synchronized java.util.Set<String> getAllCachedCities() {
    return java.util.Collections.unmodifiableSet(new java.util.HashSet<>(cache.keySet()));
  }

  /**
   * Internal wrapper class for cache entries that includes timestamp information.
   *
   * <p>This class is used to store weather data along with metadata for TTL expiration.
   *
   * @since 1.0.0
   */
  private static class CacheEntry {
    /** The cached weather data. */
    private final WeatherData data;

    /** The timestamp when this entry was cached. */
    private final long timestamp;

    /**
     * Constructs a new CacheEntry with the specified data and current timestamp.
     *
     * @param data the weather data to cache (must not be null)
     */
    /* default */ CacheEntry(final WeatherData data) {
      this.data = data;
      this.timestamp = System.currentTimeMillis();
    }

    /**
     * Returns the cached weather data.
     *
     * @return the weather data (never null)
     */
    /* default */ WeatherData getData() {
      return data;
    }

    /**
     * Returns the timestamp when this entry was cached.
     *
     * @return the timestamp in milliseconds since epoch
     */
    /* default */ long getTimestamp() {
      return timestamp;
    }
  }
}
