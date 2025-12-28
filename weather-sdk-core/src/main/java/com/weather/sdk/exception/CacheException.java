/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.exception;

/**
 * Exception thrown when cache-related errors occur.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>Cache initialization fails
 *   <li>Cache storage operations fail (put, evict)
 *   <li>Cache retrieval operations fail (get)
 *   <li>Cache capacity limits are exceeded
 *   <li>Cache eviction strategy encounters errors
 *   <li>Cache TTL (Time To Live) management fails
 *   <li>Thread-safety issues in cache operations
 *   <li>Cache corruption or invalid data detected
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #CACHE_ERROR}
 *
 * <h2>Cache Configuration</h2>
 *
 * <ul>
 *   <li><strong>Default Capacity:</strong> 10 cities
 *   <li><strong>Default TTL:</strong> 10 minutes
 *   <li><strong>Eviction Strategy:</strong> LRU (Least Recently Used)
 *   <li><strong>Thread Safety:</strong> Concurrent access supported
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Clear and reinitialize the cache
 *   <li>Increase cache capacity if needed
 *   <li>Verify cache implementation thread-safety
 *   <li>Check for memory/resource constraints
 *   <li>Implement fallback to direct API calls
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Handling Cache Exceptions</h3>
 *
 * <pre>
 * try {
 *   WeatherData weather = sdk.getWeather("London");
 * } catch (CacheException e) {
 *   logger.warn("Cache error ({}): {}", e.getErrorCode(), e.getMessage());
 *
 *   if (e.getOperation().equals("PUT")) {
 *     logger.warn("Failed to store weather data in cache");
 *   } else if (e.getOperation().equals("GET")) {
 *     logger.warn("Failed to retrieve weather data from cache");
 *   }
 *
 *   // Clear cache and retry without caching
 *   sdk.clearCache();
 *   return sdk.getWeather(cityName);
 * }
 * </pre>
 *
 * <h3>Cache Health Monitoring</h3>
 *
 * <pre>
 * public boolean isCacheHealthy() {
 *   try {
 *     // Test cache operations
 *     sdk.getCache().put("test", createTestWeatherData());
 *     sdk.getCache().get("test");
 *     return true;
 *   } catch (CacheException e) {
 *     logger.error("Cache health check failed: {}", e.getMessage());
 *     return false;
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK#getWeather(String)
 * @see com.weather.sdk.cache.WeatherCache
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class CacheException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for cache-related issues. */
  public static final String CACHE_ERROR = "CACHE_ERROR";

  /** The cache operation that failed (e.g., "GET", "PUT", "EVICT"). */
  private final String operation;

  /** The cache key involved in the operation. */
  private final String cacheKey;

  /**
   * Constructs a new CacheException with the specified message.
   *
   * @param message the detail message explaining the cache error. Must not be null.
   * @throws NullPointerException if message is null
   */
  public CacheException(final String message) {
    super(CACHE_ERROR, message);
    this.operation = null;
    this.cacheKey = null;
  }

  /**
   * Constructs a new CacheException with the specified message and operation.
   *
   * @param message the detail message explaining the cache error. Must not be null.
   * @param operation the cache operation that failed (e.g., "GET", "PUT", "EVICT"). May be null.
   * @throws NullPointerException if message is null
   */
  public CacheException(final String message, final String operation) {
    super(CACHE_ERROR, message);
    this.operation = operation;
    this.cacheKey = null;
  }

  /**
   * Constructs a new CacheException with the specified message, operation, and cache key.
   *
   * @param message the detail message explaining the cache error. Must not be null.
   * @param operation the cache operation that failed (e.g., "GET", "PUT", "EVICT"). May be null.
   * @param cacheKey the cache key involved in the operation. May be null.
   * @throws NullPointerException if message is null
   */
  public CacheException(final String message, final String operation, final String cacheKey) {
    super(CACHE_ERROR, message);
    this.operation = operation;
    this.cacheKey = cacheKey;
  }

  /**
   * Constructs a new CacheException with the specified message, operation, cache key, and cause.
   *
   * @param message the detail message explaining the cache error. Must not be null.
   * @param operation the cache operation that failed (e.g., "GET", "PUT", "EVICT"). May be null.
   * @param cacheKey the cache key involved in the operation. May be null.
   * @param cause the underlying cause of the cache error. May be null.
   * @throws NullPointerException if message is null
   */
  public CacheException(
      final String message, final String operation, final String cacheKey, final Throwable cause) {
    super(CACHE_ERROR, message, cause);
    this.operation = operation;
    this.cacheKey = cacheKey;
  }

  /**
   * Returns the cache operation that failed.
   *
   * @return the operation name (e.g., "GET", "PUT", "EVICT"), or null if not provided
   */
  public String getOperation() {
    return operation;
  }

  /**
   * Returns the cache key involved in the failed operation.
   *
   * @return the cache key, or null if not provided
   */
  public String getCacheKey() {
    return cacheKey;
  }

  /**
   * Indicates whether this was a storage operation failure (put, store).
   *
   * @return true if the error was related to storing data in cache
   */
  public boolean isStorageError() {
    return operation != null
        && ("PUT".equalsIgnoreCase(operation)
            || "STORE".equalsIgnoreCase(operation)
            || "ADD".equalsIgnoreCase(operation));
  }

  /**
   * Indicates whether this was a retrieval operation failure (get, retrieve).
   *
   * @return true if the error was related to retrieving data from cache
   */
  public boolean isRetrievalError() {
    return operation != null
        && ("GET".equalsIgnoreCase(operation)
            || "RETRIEVE".equalsIgnoreCase(operation)
            || "LOAD".equalsIgnoreCase(operation));
  }

  /**
   * Indicates whether this was an eviction operation failure.
   *
   * @return true if the error was related to cache eviction
   */
  public boolean isEvictionError() {
    return operation != null
        && ("EVICT".equalsIgnoreCase(operation)
            || "REMOVE".equalsIgnoreCase(operation)
            || "DELETE".equalsIgnoreCase(operation));
  }

  /**
   * Returns a descriptive category for the type of cache error.
   *
   * @return a descriptive error category
   */
  public String getErrorCategory() {
    final String result;
    if (isStorageError()) {
      result = "Cache Storage Error";
    } else if (isRetrievalError()) {
      result = "Cache Retrieval Error";
    } else if (isEvictionError()) {
      result = "Cache Eviction Error";
    } else if (operation != null) {
      result = "Cache Operation Error";
    } else {
      result = "Cache Error";
    }
    return result;
  }

  /**
   * Returns the exception type description.
   *
   * @return "Cache Error"
   */
  @Override
  public String getExceptionType() {
    return "Cache Error";
  }

  /**
   * Returns a detailed message including operation and cache key if available.
   *
   * @return a detailed error message
   */
  @Override
  public String getMessage() {
    final String baseMessage = super.getMessage();
    final String result;

    if (operation != null && cacheKey != null) {
      result = String.format("%s [Operation: %s, Key: %s]", baseMessage, operation, cacheKey);
    } else if (operation != null) {
      result = String.format("%s [Operation: %s]", baseMessage, operation);
    } else if (cacheKey != null) {
      result = String.format("%s [Key: %s]", baseMessage, cacheKey);
    } else {
      result = baseMessage;
    }

    return result;
  }
}
