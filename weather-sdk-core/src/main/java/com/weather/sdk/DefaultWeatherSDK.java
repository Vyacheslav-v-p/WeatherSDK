/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import com.weather.sdk.cache.DefaultWeatherCache;
import com.weather.sdk.cache.WeatherCache;
import com.weather.sdk.client.DefaultOpenWeatherApiClient;
import com.weather.sdk.client.OpenWeatherApiClient;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of WeatherSDK interface for Phase 1.
 *
 * <p>This implementation provides full production-ready functionality including:
 *
 * <ul>
 *   <li>Intelligent caching with LRU eviction and TTL support
 *   <li>HTTP client integration with OpenWeatherAPI
 *   <li>Comprehensive metrics collection and tracking
 *   <li>Thread-safe operations for concurrent access
 *   <li>Proper resource management and lifecycle handling
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This implementation is thread-safe and supports concurrent
 * access from multiple threads without external synchronization. All operations use thread-safe
 * collections and atomic variables for metrics tracking.
 *
 * @since 1.0.0
 * @see WeatherSDK
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter", "CT_CONSTRUCTOR_THROW"})
public class DefaultWeatherSDK implements WeatherSDK {

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWeatherSDK.class);

  /** The SDK configuration used to create this instance. */
  private final SDKConfiguration configuration;

  /** Weather cache for storing and retrieving cached weather data. */
  private final WeatherCache weatherCache;

  /** HTTP client for making requests to OpenWeatherAPI. */
  private final HttpClient httpClient;

  /** OpenWeatherAPI client for fetching weather data. */
  private final OpenWeatherApiClient apiClient;

  /** Scheduled executor service for polling mode. */
  private ScheduledExecutorService pollingExecutor;

  /** Future task for the polling scheduler. */
  private ScheduledFuture<?> pollingTask;

  /** Flag indicating whether this instance has been shut down. */
  private volatile boolean closed;

  /** Total number of API requests made. */
  private final AtomicLong totalRequests = new AtomicLong(0);

  /** Number of successful API requests. */
  private final AtomicLong successCount = new AtomicLong(0);

  /** Number of failed API requests. */
  private final AtomicLong failedCount = new AtomicLong(0);

  /** Number of cache hits. */
  private final AtomicLong cacheHits = new AtomicLong(0);

  /** Number of cache misses. */
  private final AtomicLong cacheMisses = new AtomicLong(0);

  /** Total response time for calculating averages. */
  private final AtomicLong totalResponseTime = new AtomicLong(0);

  /** Timestamp of the last API call. */
  private final AtomicReference<Instant> lastApiCallTime = new AtomicReference<>();

  /**
   * Constructs a new DefaultWeatherSDK with the specified configuration.
   *
   * @param configuration the SDK configuration (must not be null)
   * @throws IllegalArgumentException if configuration is null
   */
  @SuppressWarnings("CT_CONSTRUCTOR_THROW")
  public DefaultWeatherSDK(final SDKConfiguration configuration) {
    // Validate configuration first to avoid NPE
    if (configuration == null) {
      throw new IllegalArgumentException("Configuration must not be null");
    }

    // Initialize fields first to avoid partial initialization
    this.configuration = configuration;

    // Initialize weather cache
    this.weatherCache =
        new DefaultWeatherCache(configuration.getCacheTTL(), configuration.getCacheSize());

    // Initialize HTTP client with configured timeouts
    final Duration connectionTimeout = configuration.getConnectionTimeout();
    final int connTimeoutSec = configuration.getConnectionTimeoutSeconds();
    this.httpClient = HttpClient.newBuilder().connectTimeout(connectionTimeout).build();

    // Initialize API client with retry configuration
    final int readTimeoutSec = configuration.getReadTimeoutSeconds();
    this.apiClient =
        new DefaultOpenWeatherApiClient(
            httpClient,
            connTimeoutSec,
            readTimeoutSec,
            configuration.getMaxRetries(),
            configuration.getRetryDelayMs());

    // Initialize polling if in POLLING mode
    // PMD suppression: Law of Demeter - accessing configuration properties is
    // acceptable
    if (configuration.getMode() == SDKMode.POLLING) {
      initializePolling();
    }
  }

  /**
   * Returns the SDK configuration used to create this instance.
   *
   * @return the SDK configuration
   */
  public SDKConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Retrieves current weather data for the specified city with intelligent caching.
   *
   * <p>This method implements the following logic:
   *
   * <ol>
   *   <li>Validates the SDK is not shut down
   *   <li>Normalizes the city name (trimmed, lowercase)
   *   <li>Checks cache first for fresh data (within TTL)
   *   <li>If cache hit: records metrics and returns cached data
   *   <li>If cache miss: fetches from OpenWeatherAPI
   *   <li>Caches successful API responses
   *   <li>Records all metrics for performance tracking
   * </ol>
   *
   * @param cityName the name of the city to retrieve weather for (must not be null or empty)
   * @return the current weather data for the specified city
   * @throws WeatherSDKException for weather-related errors (API key, network, etc.)
   * @throws InterruptedException if the operation is interrupted during retry attempts
   * @throws IllegalArgumentException if cityName is null or empty after trimming
   * @throws IllegalStateException if this SDK instance has been shut down
   */
  @Override
  public WeatherData getWeather(final String cityName)
      throws WeatherSDKException, InterruptedException {
    validateNotShutdown();
    validateCityName(cityName);

    final String normCity = cityName.trim().toLowerCase(java.util.Locale.ROOT);
    final long startTime = System.currentTimeMillis();
    boolean success = false;
    WeatherData result;

    try {
      // Check cache first
      final Optional<WeatherData> cachedWeather = weatherCache.get(normCity);

      if (cachedWeather.isPresent()) {
        // Cache hit
        cacheHits.incrementAndGet();
        result = cachedWeather.get();
        success = true;
      } else {
        // Cache miss - fetch from API
        cacheMisses.incrementAndGet();
        result = fetchFromApi(normCity, startTime);
        success = true;
      }
    } finally {
      recordMetrics(success, System.currentTimeMillis() - startTime);
    }

    return result;
  }

  /**
   * Fetches weather data from the API and caches the result.
   *
   * @param cityName the normalized city name
   * @param startTime the start time for metrics calculation
   * @return the fetched weather data
   * @throws WeatherSDKException if the API call fails
   * @throws InterruptedException if the operation is interrupted
   */
  private WeatherData fetchFromApi(final String cityName, final long startTime)
      throws WeatherSDKException, InterruptedException {
    // Fetch weather data from API
    final WeatherData weatherData = apiClient.fetchWeather(cityName, configuration.getApiKey());

    // Cache the result for future requests
    weatherCache.put(cityName, weatherData);

    recordMetrics(true, System.currentTimeMillis() - startTime);
    return weatherData;
  }

  /**
   * Records metrics for the operation.
   *
   * @param success whether the operation was successful
   * @param responseTime the response time in milliseconds
   */
  private void recordMetrics(final boolean success, final long responseTime) {
    totalRequests.incrementAndGet();
    totalResponseTime.addAndGet(responseTime);
    lastApiCallTime.set(Instant.now());

    if (success) {
      successCount.incrementAndGet();
    } else {
      failedCount.incrementAndGet();
    }
  }

  /**
   * Returns current SDK performance and usage metrics.
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
   * threads. The returned metrics object is a snapshot of current values.
   *
   * @return current SDK metrics and performance statistics
   */
  @Override
  public SDKMetrics getMetrics() {
    validateNotShutdown();
    final long totalReqs = totalRequests.get();
    final double avgResponseTime =
        totalReqs > 0 ? (double) totalResponseTime.get() / totalReqs : 0.0;

    return new SDKMetrics(
        totalReqs,
        successCount.get(),
        failedCount.get(),
        cacheHits.get(),
        cacheMisses.get(),
        avgResponseTime,
        lastApiCallTime.get());
  }

  /**
   * Closes this SDK instance and releases all resources.
   *
   * <p>This method delegates to {@link #shutdown()} to ensure consistent resource cleanup.
   *
   * <p><strong>Thread Safety:</strong> This method is safe to call concurrently from multiple
   * threads.
   *
   * <p><strong>Idempotent:</strong> This method can be called multiple times safely.
   */
  @Override
  public void close() {
    shutdown();
  }

  /**
   * Checks if this SDK instance has been shut down.
   *
   * @return true if shutdown, false otherwise
   */
  public boolean isShutdown() {
    return closed;
  }

  /**
   * Validates that this SDK instance is not shut down.
   *
   * @throws IllegalStateException if this instance is shut down
   */
  protected void validateNotShutdown() {
    if (closed) {
      throw new IllegalStateException("SDK instance has been shut down");
    }
  }

  /**
   * Validates the city name parameter.
   *
   * @param cityName the city name to validate
   * @throws IllegalArgumentException if cityName is null or empty after trimming
   */
  private void validateCityName(final String cityName) {
    if (cityName == null || cityName.isBlank()) {
      throw new IllegalArgumentException("City name must not be null or empty");
    }
  }

  /**
   * Initializes polling mode if the configuration specifies SDKMode.POLLING. This creates a
   * scheduled task that updates weather data for all cached cities at the configured polling
   * interval.
   */
  private void initializePolling() {
    this.pollingExecutor =
        Executors.newSingleThreadScheduledExecutor(
            runnable -> {
              final Thread pollingThread = new Thread(runnable, "WeatherSDK-Polling-Thread");
              pollingThread.setDaemon(true); // Allow JVM to exit even if polling is running
              return pollingThread;
            });

    // Schedule the polling task to run at the configured interval
    // PMD suppression: Law of Demeter - accessing configuration properties is
    // acceptable
    this.pollingTask =
        this.pollingExecutor.scheduleAtFixedRate(
            this::pollCachedCities,
            0, // Initial delay
            configuration.getPollingInterval().toMillis(),
            TimeUnit.MILLISECONDS);
  }

  /**
   * Polls all currently cached cities to update their weather data. This method is executed by the
   * scheduled executor at regular intervals.
   */
  @SuppressWarnings("PMD.AvoidCatchingGenericException")
  private void pollCachedCities() {
    if (closed) {
      // If the SDK is closed, cancel the polling task
      if (pollingTask != null && !pollingTask.isCancelled()) {
        pollingTask.cancel(false);
      }
      return;
    }

    try {
      // Get all cached city names and update their weather data
      for (final String cityName : weatherCache.getAllCachedCities()) {
        try {
          // Fetch fresh weather data for the city
          final WeatherData freshWeather =
              apiClient.fetchWeather(cityName, configuration.getApiKey());

          // Update the cache with fresh data
          weatherCache.put(cityName, freshWeather);

          // Record successful update metrics
          recordMetrics(true, 0); // Response time is not applicable for background polling
        } catch (final Exception e) {
          // Log the error but continue with other cities
          // PMD suppression: catching generic exception is intentional here to prevent
          // one failed city from stopping all polling operations
          if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(
                "Failed to update weather for city: {}, error: {}", cityName, e.getMessage());
          }

          // Record failed update metrics
          recordMetrics(false, 0); // Response time is not applicable for background polling
        }
      }
    } catch (final Exception e) {
      // Catch any unexpected errors during polling
      // PMD suppression: catching generic exception is intentional here to prevent
      // polling thread from terminating due to any unexpected error
      if (LOGGER.isErrorEnabled()) {
        LOGGER.error("Unexpected error during polling: {}", e.getMessage());
      }
    }
  }

  /**
   * Gracefully shuts down the SDK instance and releases all associated resources.
   *
   * <p>This method performs proper cleanup:
   *
   * <ul>
   *   <li>Stops any background polling threads
   *   <li>Closes HTTP client connections
   *   <li>Clears internal resources
   *   <li>Marks the instance as shut down
   * </ul>
   *
   * <p><strong>Thread Safety:</strong> This method can be called safely from any thread. After
   * shutdown, subsequent method calls will throw IllegalStateException.
   *
   * <p><strong>Idempotent:</strong> This method can be called multiple times safely.
   */
  @Override
  public void shutdown() {
    if (!closed) {
      closed = true;

      shutdownPolling();

      // Clear the cache to release memory
      weatherCache.clear();

      // HTTP client doesn't need explicit shutdown in Java 17
      // It will be garbage collected when no longer referenced
    }
  }

  /** Shuts down the polling executor and associated tasks. */
  private void shutdownPolling() {
    // Cancel polling task if it exists
    if (pollingTask != null && !pollingTask.isCancelled()) {
      pollingTask.cancel(false);
    }

    // Shutdown polling executor if it exists
    if (pollingExecutor != null && !pollingExecutor.isShutdown()) {
      pollingExecutor.shutdown();
      try {
        // Wait up to 5 seconds for existing tasks to terminate
        if (!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
          pollingExecutor.shutdownNow(); // Cancel currently executing tasks
          // Wait a bit more for tasks to respond to being cancelled
          if (!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            LOGGER.warn("Polling executor did not terminate");
          }
        }
      } catch (InterruptedException e) {
        // (Re-)Cancel if current thread also interrupted
        pollingExecutor.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }
    }
  }
}
