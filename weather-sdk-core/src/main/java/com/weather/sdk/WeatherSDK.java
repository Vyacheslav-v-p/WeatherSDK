/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk; // Intentionally add extra spaces

import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;
import java.io.Closeable;

/**
 * The main interface for the Weather SDK that provides weather data retrieval and management
 * functionality.
 *
 * <p>This interface defines the contract for interacting with OpenWeatherAPI through a fluent,
 * production-ready SDK that supports multiple operation modes, caching, metrics collection, and
 * graceful resource management.
 *
 * <p><strong>Thread Safety:</strong> All implementations of this interface must be thread-safe and
 * support concurrent access from multiple threads without external synchronization.
 *
 * <h2>Core Features</h2>
 *
 * <ul>
 *   <li>Weather data retrieval by city name
 *   <li>Intelligent caching with configurable TTL and LRU eviction
 *   <li>Two operation modes: on-demand and polling
 *   <li>Comprehensive metrics and diagnostics
 *   <li>Graceful shutdown and resource cleanup
 *   <li>Support for multiple API keys via registry
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Usage (On-Demand Mode)</h3>
 *
 * <pre>
 * // Create SDK instance using builder
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key")
 *     .mode(SDKMode.ON_DEMAND)
 *     .build();
 *
 * try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
 *
 *   // Fetch weather data
 *   WeatherData weather = sdk.getWeather("New York");
 *   System.out.println("Temperature: " + weather.getTemperature().getTemp() + "K");
 *
 *   // Check performance metrics
 *   SDKMetrics metrics = sdk.getMetrics();
 *   System.out.println("Cache hit ratio: " + metrics.getCacheHitRatio());
 * }
 * </pre>
 *
 * <h3>Polling Mode</h3>
 *
 * <pre>
 * // Create SDK with automatic polling every 5 minutes
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key")
 *     .mode(SDKMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(5))
 *     .build();
 *
 * try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
 *
 *   // Weather data is automatically updated in background
 *   WeatherData weather = sdk.getWeather("London");
 *   // Data is fresh from the latest polling cycle
 * }
 * </pre>
 *
 * <h3>Multiple API Keys</h3>
 *
 * <pre>
 * // Register multiple SDK instances for different API keys
 * WeatherSDK sdk1 = SDKRegistry.createSDKInstance("key1", config1);
 * WeatherSDK sdk2 = SDKRegistry.createSDKInstance("key2", config2);
 *
 * // Later, retrieve by API key
 * WeatherSDK sdk = SDKRegistry.getSDKInstance("key1");
 * </pre>
 *
 * <h2>Error Handling</h2>
 *
 * <p>The SDK uses a comprehensive exception hierarchy to provide clear, actionable error
 * information:
 *
 * <ul>
 *   <li>{@link com.weather.sdk.exception.ApiKeyException} - Invalid or missing API key
 *   <li>{@link com.weather.sdk.exception.CityNotFoundException} - City not found in OpenWeatherAPI
 *   <li>{@link com.weather.sdk.exception.RateLimitException} - API rate limit exceeded
 *   <li>{@link com.weather.sdk.exception.NetworkException} - Network connectivity issues
 *   <li>{@link com.weather.sdk.exception.CacheException} - Cache-related errors
 * </ul>
 *
 * <h2>Configuration</h2>
 *
 * <p>All configuration is handled through the {@link com.weather.sdk.config.WeatherSDKBuilder}
 * which provides a fluent API for setting up SDK behavior:
 *
 * <ul>
 *   <li>API key and authentication
 *   <li>Operation mode (on-demand or polling)
 *   <li>Polling interval for background updates
 *   <li>Cache size and TTL configuration
 *   <li>Network timeout settings
 * </ul>
 *
 * @see com.weather.sdk.config.WeatherSDKBuilder
 * @see com.weather.sdk.config.SDKConfiguration
 * @see com.weather.sdk.config.SDKMode
 * @see com.weather.sdk.model.WeatherData
 * @see com.weather.sdk.SDKRegistry
 * @since 1.0.0
 */
public interface WeatherSDK extends Closeable {

  /**
   * Retrieves current weather data for the specified city.
   *
   * <p>This method implements intelligent caching logic:
   *
   * <ul>
   *   <li>Checks cache first for fresh data (within TTL)
   *   <li>Fetches from OpenWeatherAPI if cache miss or stale data
   *   <li>Automatically caches successful API responses
   *   <li>Implements retry logic for transient failures
   * </ul>
   *
   * <p><strong>Thread Safety:</strong> This method is safe to call concurrently from multiple
   * threads.
   *
   * @param cityName the name of the city to retrieve weather for. Must not be null or empty.
   *     Leading/trailing whitespace will be trimmed.
   * @return the current weather data for the specified city
   * @throws com.weather.sdk.exception.ApiKeyException if the API key is invalid or missing
   * @throws com.weather.sdk.exception.CityNotFoundException if the city could not be found
   * @throws com.weather.sdk.exception.RateLimitException if the API rate limit has been exceeded
   * @throws com.weather.sdk.exception.NetworkException if a network error occurs during API
   *     communication
   * @throws com.weather.sdk.exception.WeatherSDKException for any other weather-related errors
   * @throws IllegalArgumentException if the cityName parameter is null or empty after trimming
   * @throws InterruptedException if the operation is interrupted during retry attempts
   *     <h4>Input Validation</h4>
   *     <ul>
   *       <li>City name must not be null
   *       <li>City name must not be empty after trimming whitespace
   *       <li>City name length should be reasonable (1-100 characters recommended)
   *     </ul>
   *     <h5>Retry Behavior</h5>
   *     <ul>
   *       <li>Automatically retries on network failures (up to 3 attempts)
   *       <li>Uses exponential backoff with 500ms base delay
   *       <li>Does not retry on client errors (4xx responses)
   *       <li>Logs retry attempts at DEBUG level
   *     </ul>
   *     <h5>Caching Behavior</h5>
   *     <ul>
   *       <li>Default cache size: 10 cities
   *       <li>Default TTL: 10 minutes
   *       <li>LRU eviction when cache is full
   *       <li>Thread-safe cache operations
   *     </ul>
   *
   * @since 1.0.0
   */
  WeatherData getWeather(String cityName) throws WeatherSDKException, InterruptedException;

  /**
   * Retrieves current SDK performance and usage metrics.
   *
   * <p>This method provides insights into SDK performance including:
   *
   * <ul>
   *   <li>Total API requests made
   *   <li>Cache hit/miss statistics
   *   <li>API success/failure rates
   *   <li>Average response times
   *   <li>Last API call timestamp
   * </ul>
   *
   * <p><strong>Thread Safety:</strong> This method is safe to call concurrently from multiple
   * threads. The returned metrics object is a snapshot of current values and will not be updated in
   * real-time.
   *
   * @return current SDK metrics and performance statistics. Never returns null.
   * @since 1.0.0
   * @see com.weather.sdk.SDKMetrics
   */
  SDKMetrics getMetrics();

  /**
   * Gracefully shuts down the SDK instance and releases all associated resources.
   *
   * <p>This method performs proper cleanup:
   *
   * <ul>
   *   <li>Stops any background polling threads
   *   <li>Closes HTTP client connections
   *   <li>Clears internal resources
   *   <li>Interrupts any pending operations
   * </ul>
   *
   * <p><strong>Thread Safety:</strong> This method can be called safely from any thread. After
   * shutdown, subsequent method calls will throw {@link IllegalStateException}.
   *
   * <p><strong>Idempotent:</strong> This method can be called multiple times safely.
   *
   * <p><strong>Blocking Behavior:</strong> This method may block briefly to ensure clean shutdown
   * of background threads.
   *
   * <h4>Lifecycle Management</h4>
   *
   * <p>For automatic resource management, consider using try-with-resources:
   *
   * <pre>
   * SDKConfiguration config = new WeatherSDKBuilder().apiKey("key").build();
   *
   * try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
   *   // SDK automatically shutdown when exiting try block
   *   WeatherData weather = sdk.getWeather("Boston");
   * }
   * </pre>
   *
   * @since 1.0.0
   */
  void shutdown();

  /**
   * Closes this SDK instance and releases all resources.
   *
   * <p>This method delegates to {@link #shutdown()} to ensure consistent resource cleanup. It
   * enables the use of try-with-resources statement for automatic resource management.
   *
   * <p><strong>Thread Safety:</strong> This method is safe to call concurrently from multiple
   * threads.
   *
   * <p><strong>Idempotent:</strong> This method can be called multiple times safely.
   *
   * @since 1.0.0
   */
  @Override
  void close();
}
