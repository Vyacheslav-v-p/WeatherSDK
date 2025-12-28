/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.config;

import com.weather.sdk.exception.ConfigurationException;
import java.time.Duration;
import java.util.Objects;

/**
 * Fluent builder for creating {@link SDKConfiguration} instances with validation.
 *
 * <p>This class provides a user-friendly, type-safe builder pattern for configuring SDK instances.
 * It follows the builder pattern with method chaining and includes comprehensive validation to
 * ensure configuration correctness before object creation.
 *
 * <p><strong>Thread Safety:</strong> This class is not thread-safe and should not be used
 * concurrently by multiple threads. Each builder instance should be used by a single thread.
 *
 * <h2>Configuration Parameters</h2>
 *
 * <ul>
 *   <li><strong>apiKey</strong>: OpenWeatherAPI key (required, validated)
 *   <li><strong>mode</strong>: Operation mode ({@link SDKMode}, default: ON_DEMAND)
 *   <li><strong>pollingInterval</strong>: Polling frequency (default: 5 minutes)
 *   <li><strong>cacheSize</strong>: Cache size in cities (default: 10)
 *   <li><strong>cacheTTL</strong>: Cache time-to-live (default: 10 minutes)
 *   <li><strong>connectionTimeout</strong>: HTTP connection timeout (default: 5 seconds)
 *   <li><strong>readTimeout</strong>: HTTP read timeout (default: 10 seconds)
 * </ul>
 *
 * <h2>Validation Rules</h2>
 *
 * <ul>
 *   <li>API key must not be null or empty after trimming whitespace
 *   <li>Mode must not be null
 *   <li>Polling interval must be positive (if mode is POLLING)
 *   <li>Cache size must be positive (> 0)
 *   <li>Cache TTL must be positive
 *   <li>Connection timeout must be positive
 *   <li>Read timeout must be positive
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic On-Demand Configuration</h3>
 *
 * <pre>
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key-here")
 *     .mode(SDKMode.ON_DEMAND)
 *     .build();
 * </pre>
 *
 * <h3>Polling Mode with Custom Settings</h3>
 *
 * <pre>
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key-here")
 *     .mode(SDKMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(10))
 *     .cacheSize(10)
 *     .cacheTTL(Duration.ofMinutes(15))
 *     .connectionTimeout(Duration.ofSeconds(10))
 *     .readTimeout(Duration.ofSeconds(15))
 *     .maxRetries(3)
 *     .retryDelayMs(50L)
 *     .build();
 * </pre>
 *
 * <h3>Error Handling Example</h3>
 *
 * <pre>
 * try {
 *   SDKConfiguration config = new WeatherSDKBuilder()
 *       .apiKey("invalid-or-empty-key")
 *       .mode(SDKMode.POLLING)
 *       .pollingInterval(Duration.ofSeconds(30)) // Too short
 *       .build();
 * } catch (ConfigurationException e) {
 *   System.err.println("Configuration error: " + e.getMessage());
 *   System.err.println("Suggested fix: " + e.getSuggestedFix());
 * }
 * </pre>
 *
 * @see SDKConfiguration
 * @see SDKMode
 * @see ConfigurationException
 * @since 1.0.0
 */
// PMD suppressions for builder pattern complexity and style violations
@SuppressWarnings({
  "PMD.TooManyMethods",
  "PMD.LongVariable",
  "PMD.MethodArgumentCouldBeFinal",
  "PMD.UnnecessaryConstructor",
  "PMD.LawOfDemeter",
  "PMD.AvoidUncheckedExceptionsInSignatures",
  "PMD.CyclomaticComplexity",
  "PMD.AvoidDeeplyNestedIfStmts",
  "PMD.AvoidFieldNameMatchingMethodName",
  "PMD.OnlyOneReturn"
})
public final class WeatherSDKBuilder {

  /** API key - required, non-null, non-empty after trimming */
  private String apiKey;

  /** SDK operation mode - default: ON_DEMAND */
  private SDKMode mode = SDKMode.ON_DEMAND;

  /** Polling interval - default: 5 minutes */
  private Duration pollingInterval = Duration.ofMinutes(5);

  /** Cache size - default: 10 cities, must be positive */
  private int cacheSize = 10;

  /** Cache TTL - default: 10 minutes, must be positive */
  private Duration cacheTTL = Duration.ofMinutes(10);

  /** Connection timeout - default: 5 seconds, must be positive */
  private Duration connectionTimeout = Duration.ofSeconds(5);

  /** Read timeout - default: 10 seconds, must be positive */
  private Duration readTimeout = Duration.ofSeconds(10);

  /** Maximum number of retry attempts - default: 3, must be non-negative */
  private int maxRetries = 3;

  /** Delay between retry attempts in milliseconds - default: 50ms, must be positive */
  private long retryDelayMs = 500L;

  /** Minimum recommended polling interval in minutes. */
  private static final int MIN_POLLING_INTERVAL_MINUTES = 1;

  /**
   * Constructs a new WeatherSDKBuilder with default configuration values.
   *
   * <p>Default values:
   *
   * <ul>
   *   <li>Mode: {@link SDKMode#ON_DEMAND}
   *   <li>Polling interval: 5 minutes
   *   <li>Cache size: 10 cities
   *   <li>Cache TTL: 10 minutes
   *   <li>Connection timeout: 5 seconds
   *   <li>Read timeout: 10 seconds
   *   <li>Max retries: 3
   *   <li>Retry delay: 500ms
   * </ul>
   */
  public WeatherSDKBuilder() {
    // Initialize with defaults
  }

  /**
   * Sets the OpenWeatherAPI authentication key.
   *
   * <p>The API key is required for all SDK operations. It will be trimmed of leading and trailing
   * whitespace during validation.
   *
   * @param apiKeyParam the OpenWeatherAPI authentication key (must not be null or empty)
   * @return this builder for method chaining
   * @throws ConfigurationException if apiKeyParam is null or empty after trimming
   */
  public WeatherSDKBuilder apiKey(String apiKeyParam) throws ConfigurationException {
    if (apiKeyParam == null || apiKeyParam.isBlank()) {
      throw new ConfigurationException("API key must not be null or empty");
    }
    this.apiKey = apiKeyParam.trim();
    return this;
  }

  /**
   * Sets the SDK operation mode.
   *
   * <p>The mode determines how the SDK fetches weather data:
   *
   * <ul>
   *   <li><strong>ON_DEMAND</strong>: Weather data is fetched only when requested by the
   *       application
   *   <li><strong>POLLING</strong>: Weather data is automatically fetched at regular intervals in
   *       the background
   * </ul>
   *
   * @param modeParam the SDK operation mode (must not be null)
   * @return this builder for method chaining
   * @throws ConfigurationException if modeParam is null
   */
  public WeatherSDKBuilder mode(SDKMode modeParam) throws ConfigurationException {
    if (modeParam == null) {
      throw new ConfigurationException("SDK mode must not be null");
    }
    this.mode = modeParam;
    return this;
  }

  /**
   * Sets the polling interval for background weather data updates.
   *
   * <p>This setting is only used when the mode is {@link SDKMode#POLLING}. The SDK will
   * automatically fetch weather data for cached cities at the specified interval.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>Minimum: 1 minute (for high-frequency updates)
   *   <li>Recommended: 5-10 minutes (balance between freshness and API usage)
   *   <li>Maximum: 60 minutes (for low-frequency updates)
   * </ul>
   *
   * @param interval the polling interval (must not be null or non-positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if interval is null, negative, or zero
   */
  public WeatherSDKBuilder pollingInterval(Duration interval) throws ConfigurationException {
    if (interval == null) {
      throw new ConfigurationException("Polling interval must not be null");
    }
    if (interval.isNegative() || interval.isZero()) {
      throw new ConfigurationException("Polling interval must be positive");
    }
    this.pollingInterval = interval;
    return this;
  }

  /**
   * Sets the maximum number of cities to cache.
   *
   * <p>The SDK maintains an in-memory cache of recently requested weather data. When the cache
   * reaches this limit, it uses an LRU (Least Recently Used) eviction strategy to remove old
   * entries.
   *
   * <p><strong>Considerations:</strong>
   *
   * <ul>
   *   <li>Higher values provide better cache hit rates but consume more memory
   *   <li>Lower values save memory but may result in more API calls
   *   <li>Typical applications: 10-50 cities
   * </ul>
   *
   * @param size the maximum number of cities to cache (must be positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if size is not positive
   */
  public WeatherSDKBuilder cacheSize(int size) throws ConfigurationException {
    if (size <= 0) {
      throw new ConfigurationException("Cache size must be positive");
    }
    this.cacheSize = size;
    return this;
  }

  /**
   * Sets the time-to-live for cached weather data.
   *
   * <p>Cached weather data becomes stale after this duration and will trigger a fresh API call.
   * This helps ensure weather data remains reasonably current while reducing API usage.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>Short TTL (5-10 minutes): For time-sensitive applications
   *   <li>Medium TTL (10-15 minutes): Balance between freshness and API usage
   *   <li>Long TTL (15-30 minutes): For applications where weather doesn't change rapidly
   * </ul>
   *
   * @param ttl the cache time-to-live (must not be null or non-positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if ttl is null, negative, or zero
   */
  public WeatherSDKBuilder cacheTTL(Duration ttl) throws ConfigurationException {
    if (ttl == null) {
      throw new ConfigurationException("Cache TTL must not be null");
    }
    if (ttl.isNegative() || ttl.isZero()) {
      throw new ConfigurationException("Cache TTL must be positive");
    }
    this.cacheTTL = ttl;
    return this;
  }

  /**
   * Sets the HTTP connection timeout.
   *
   * <p>This timeout controls how long the SDK will wait while establishing a connection to the
   * OpenWeatherAPI servers. If the connection cannot be established within this time, a {@link
   * com.weather.sdk.exception.NetworkException} will be thrown.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>Fast networks: 3-5 seconds
   *   <li>Average networks: 5-10 seconds
   *   <li>Slow/unreliable networks: 10-15 seconds
   * </ul>
   *
   * @param timeout the connection timeout (must not be null or non-positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if timeout is null, negative, or zero
   */
  public WeatherSDKBuilder connectionTimeout(Duration timeout) throws ConfigurationException {
    if (timeout == null) {
      throw new ConfigurationException("Connection timeout must not be null");
    }
    if (timeout.isNegative() || timeout.isZero()) {
      throw new ConfigurationException("Connection timeout must be positive");
    }
    this.connectionTimeout = timeout;
    return this;
  }

  /**
   * Sets the HTTP read timeout.
   *
   * <p>This timeout controls how long the SDK will wait for a response after sending a request to
   * the OpenWeatherAPI. If the server doesn't respond within this time, a {@link
   * com.weather.sdk.exception.NetworkException} will be thrown.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>Fast networks: 5-10 seconds
   *   <li>Average networks: 10-15 seconds
   *   <li>Slow networks: 15-30 seconds
   * </ul>
   *
   * @param timeout the read timeout (must not be null or non-positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if timeout is null, negative, or zero
   */
  public WeatherSDKBuilder readTimeout(Duration timeout) throws ConfigurationException {
    if (timeout == null) {
      throw new ConfigurationException("Read timeout must not be null");
    }
    if (timeout.isNegative() || timeout.isZero()) {
      throw new ConfigurationException("Read timeout must be positive");
    }
    this.readTimeout = timeout;
    return this;
  }

  /**
   * Sets the maximum number of retry attempts for API calls.
   *
   * <p>The SDK will retry failed API calls up to this number of times before giving up. This is
   * useful for handling temporary network issues or server errors.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>0: No retries (fail fast)
   *   <li>1-3: Conservative retry policy (default: 3)
   *   <li>4-5: Aggressive retry policy
   * </ul>
   *
   * @param maxRetriesParam the maximum number of retry attempts (must be non-negative)
   * @return this builder for method chaining
   * @throws ConfigurationException if maxRetriesParam is negative
   */
  public WeatherSDKBuilder maxRetries(int maxRetriesParam) throws ConfigurationException {
    if (maxRetriesParam < 0) {
      throw new ConfigurationException("Max retries must be non-negative");
    }
    this.maxRetries = maxRetriesParam;
    return this;
  }

  /**
   * Sets the delay between retry attempts in milliseconds.
   *
   * <p>This delay is applied between retry attempts when API calls fail. A longer delay allows the
   * server time to recover but increases the total request time.
   *
   * <p><strong>Recommended Values:</strong>
   *
   * <ul>
   *   <li>100-500ms: For quick retries on transient errors
   *   <li>500-1000ms: Balanced approach (default: 500ms)
   *   <li>1000-5000ms: Conservative approach for sensitive environments
   * </ul>
   *
   * @param retryDelayMsParam the delay between retry attempts in milliseconds (must be positive)
   * @return this builder for method chaining
   * @throws ConfigurationException if retryDelayMsParam is not positive
   */
  public WeatherSDKBuilder retryDelayMs(long retryDelayMsParam) throws ConfigurationException {
    if (retryDelayMsParam <= 0) {
      throw new ConfigurationException("Retry delay must be positive");
    }
    this.retryDelayMs = retryDelayMsParam;
    return this;
  }

  /**
   * Validates the current configuration and constructs a new {@link SDKConfiguration} object.
   *
   * <p>This method performs comprehensive validation of all configuration parameters and throws a
   * {@link ConfigurationException} if any validation fails. The exception includes detailed
   * information about what went wrong and suggested fixes.
   *
   * <h4>Validation Performed</h4>
   *
   * <ul>
   *   <li>API key: Must not be null or empty after trimming
   *   <li>Mode: Must not be null
   *   <li>Polling interval: Must be positive (if mode is POLLING)
   *   <li>Cache size: Must be positive
   *   <li>Cache TTL: Must be positive
   *   <li>Connection timeout: Must be positive
   *   <li>Read timeout: Must be positive
   * </ul>
   *
   * @return a new, validated SDKConfiguration object
   * @throws ConfigurationException if any configuration parameter is invalid
   */
  public SDKConfiguration build() throws ConfigurationException {
    validateApiKey();
    validateMode();
    validatePollingInterval();
    validateCacheSize();
    validateCacheTTL();
    validateConnectionTimeout();
    validateReadTimeout();
    validateTimeoutsRelationship();
    validateMaxRetries();
    validateRetryDelay();

    return new SDKConfiguration(
        apiKey,
        mode,
        pollingInterval,
        cacheSize,
        cacheTTL,
        new SDKConfiguration.TimeoutConfig(connectionTimeout, readTimeout),
        maxRetries,
        retryDelayMs);
  }

  /**
   * Validates the API key.
   *
   * @throws ConfigurationException if the API key is invalid
   */
  private void validateApiKey() throws ConfigurationException {
    if (apiKey == null || isBlank(apiKey)) {
      throw new ConfigurationException("API key is required and must not be empty", "apiKey");
    }
  }

  /**
   * Checks if a string is blank (null, empty, or contains only whitespace).
   *
   * @param str the string to check
   * @return true if the string is blank, false otherwise
   */
  private boolean isBlank(String str) {
    if (str == null) {
      return true;
    }
    for (int i = 0; i < str.length(); i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Validates the SDK mode.
   *
   * @throws ConfigurationException if the mode is invalid
   */
  private void validateMode() throws ConfigurationException {
    if (mode == null) {
      throw new ConfigurationException("SDK mode is required", "mode");
    }
  }

  /**
   * Validates the polling interval based on the selected mode.
   *
   * @throws ConfigurationException if the polling interval is invalid
   */
  private void validatePollingInterval() throws ConfigurationException {
    if (mode == SDKMode.POLLING) {
      if (pollingInterval.isNegative() || pollingInterval.isZero()) {
        throw new ConfigurationException(
            "Polling interval must be positive for POLLING mode", "pollingInterval");
      }

      if (pollingInterval.toMinutes() < MIN_POLLING_INTERVAL_MINUTES) {
        throw new ConfigurationException(
            "Polling interval too short for POLLING mode", "pollingInterval");
      }
    }
  }

  /**
   * Validates the cache size.
   *
   * @throws ConfigurationException if the cache size is invalid
   */
  private void validateCacheSize() throws ConfigurationException {
    if (cacheSize <= 0) {
      throw new ConfigurationException("Cache size must be positive", "cacheSize");
    }
  }

  /**
   * Validates the cache TTL.
   *
   * @throws ConfigurationException if the cache TTL is invalid
   */
  private void validateCacheTTL() throws ConfigurationException {
    if (cacheTTL.isNegative() || cacheTTL.isZero()) {
      throw new ConfigurationException("Cache TTL must be positive", "cacheTTL");
    }
  }

  /**
   * Validates the connection timeout.
   *
   * @throws ConfigurationException if the connection timeout is invalid
   */
  private void validateConnectionTimeout() throws ConfigurationException {
    if (connectionTimeout.isNegative() || connectionTimeout.isZero()) {
      throw new ConfigurationException("Connection timeout must be positive", "connectionTimeout");
    }
  }

  /**
   * Validates the read timeout.
   *
   * @throws ConfigurationException if the read timeout is invalid
   */
  private void validateReadTimeout() throws ConfigurationException {
    if (readTimeout.isNegative() || readTimeout.isZero()) {
      throw new ConfigurationException("Read timeout must be positive", "readTimeout");
    }
  }

  /**
   * Validates the relationship between connection and read timeouts.
   *
   * @throws ConfigurationException if the timeout relationship is invalid
   */
  private void validateTimeoutsRelationship() throws ConfigurationException {
    if (readTimeout.compareTo(connectionTimeout) < 0) {
      throw new ConfigurationException(
          "Read timeout must be greater than or equal to connection timeout", "readTimeout");
    }
  }

  /**
   * Validates the maximum number of retries.
   *
   * @throws ConfigurationException if max retries is invalid
   */
  private void validateMaxRetries() throws ConfigurationException {
    if (maxRetries < 0) {
      throw new ConfigurationException("Max retries must be non-negative");
    }
  }

  /**
   * Validates the retry delay.
   *
   * @throws ConfigurationException if the retry delay is invalid
   */
  private void validateRetryDelay() throws ConfigurationException {
    if (retryDelayMs <= 0) {
      throw new ConfigurationException("Retry delay must be positive");
    }
  }

  /**
   * Returns a string representation of this builder's current state.
   *
   * <p>The string includes all configured values (including API key for debugging purposes).
   *
   * @return a string representation of this builder
   */
  @Override
  public String toString() {
    return "WeatherSDKBuilder{"
        + "apiKey='"
        + apiKey
        + '\''
        + ", mode="
        + mode
        + ", pollingInterval="
        + pollingInterval
        + ", cacheSize="
        + cacheSize
        + ", cacheTTL="
        + cacheTTL
        + ", connectionTimeout="
        + connectionTimeout
        + ", readTimeout="
        + readTimeout
        + ", maxRetries="
        + maxRetries
        + ", retryDelayMs="
        + retryDelayMs
        + '}';
  }

  /**
   * Compares this WeatherSDKBuilder to the specified object for equality.
   *
   * <p>Two WeatherSDKBuilder objects are considered equal if they have identical configuration
   * values.
   *
   * @param obj the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  @SuppressWarnings({"PMD.LocalVariableCouldBeFinal", "PMD.OnlyOneReturn"})
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    WeatherSDKBuilder that = (WeatherSDKBuilder) obj;
    return cacheSize == that.cacheSize
        && maxRetries == that.maxRetries
        && retryDelayMs == that.retryDelayMs
        && Objects.equals(apiKey, that.apiKey)
        && mode == that.mode
        && Objects.equals(pollingInterval, that.pollingInterval)
        && Objects.equals(cacheTTL, that.cacheTTL)
        && Objects.equals(connectionTimeout, that.connectionTimeout)
        && Objects.equals(readTimeout, that.readTimeout);
  }

  /**
   * Returns a hash code value for this WeatherSDKBuilder.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        apiKey,
        mode,
        pollingInterval,
        cacheSize,
        cacheTTL,
        connectionTimeout,
        readTimeout,
        maxRetries,
        retryDelayMs);
  }
}
