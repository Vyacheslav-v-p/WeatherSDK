/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.config;

import java.time.Duration;
import java.util.Objects;

/**
 * Immutable configuration object that holds all settings for the Weather SDK.
 *
 * <p>This class provides a thread-safe, immutable configuration container that encapsulates all
 * settings required to initialize and operate a Weather SDK instance. All configuration parameters
 * are validated during construction to ensure they meet SDK requirements.
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and therefore thread-safe. All fields
 * are final and cannot be modified after construction. The class can be safely shared across
 * multiple threads without external synchronization.
 *
 * <h2>Configuration Parameters</h2>
 *
 * <ul>
 *   <li><strong>apiKey</strong>: OpenWeatherAPI authentication key (required, non-empty)
 *   <li><strong>mode</strong>: SDK operation mode ({@link SDKMode})
 *   <li><strong>pollingInterval</strong>: Background polling frequency (POLLING mode only)
 *   <li><strong>cacheSize</strong>: Maximum number of cities to cache
 *   <li><strong>cacheTTL</strong>: Time-to-live for cached weather data
 *   <li><strong>timeouts</strong>: HTTP timeout configuration
 *   <li><strong>maxRetries</strong>: Maximum number of retry attempts for API calls
 *   <li><strong>retryDelayMs</strong>: Delay between retry attempts in milliseconds
 * </ul>
 *
 * <h2>Default Values</h2>
 *
 * <ul>
 *   <li><strong>mode</strong>: {@link SDKMode#ON_DEMAND}
 *   <li><strong>pollingInterval</strong>: 5 minutes
 *   <li><strong>cacheSize</strong>: 10 cities
 *   <li><strong>cacheTTL</strong>: 10 minutes
 *   <li><strong>connectionTimeout</strong>: 5 seconds
 *   <li><strong>readTimeout</strong>: 10 seconds
 *   <li><strong>maxRetries</strong>: 3 retry attempts
 *   <li><strong>retryDelayMs</strong>: 500 milliseconds
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * // Create configuration using WeatherSDKBuilder
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key")
 *     .mode(SDKMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(10))
 *     .cacheSize(10)
 *     .cacheTTL(Duration.ofMinutes(15))
 *     .maxRetries(3)
 *     .retryDelayMs(50L)
 *     .build();
 *
 * // Access configuration values
 * String apiKey = config.getApiKey();
 * SDKMode mode = config.getMode();
 * Duration pollingInterval = config.getPollingInterval();
 * int maxRetries = config.getMaxRetries();
 * long retryDelayMs = config.getRetryDelayMs();
 * </pre>
 *
 * @see WeatherSDKBuilder
 * @see SDKMode
 * @since 1.0.0
 */
// PMD suppressions for configuration pattern violations
@SuppressWarnings({
  "PMD.DataClass",
  "PMD.CommentDefaultAccessModifier",
  "PMD.CyclomaticComplexity",
  "PMD.NPathComplexity",
  "PMD.InefficientEmptyStringCheck",
  "PMD.MethodArgumentCouldBeFinal",
  "PMD.OnlyOneReturn",
  "PMD.LocalVariableCouldBeFinal",
  "PMD.GodClass",
  "PMD.TooManyMethods"
})
public final class SDKConfiguration {

  /** The OpenWeatherAPI authentication key. Required and non-empty. */
  private final String apiKey;

  /** The SDK operation mode (on-demand or polling). Default: ON_DEMAND. */
  private final SDKMode mode;

  /** The polling interval for background updates. Used only in POLLING mode. */
  private final Duration pollingInterval;

  /** The maximum number of cities to cache. Default: 10. Must be positive. */
  private final int cacheSize;

  /** The time-to-live for cached weather data. Default: 10 minutes. Must be positive. */
  private final Duration cacheTTL;

  /** HTTP timeout configuration. Contains connection and read timeouts. */
  private final TimeoutConfig timeouts;

  /** The maximum number of retry attempts for API calls. Default: 3. Must be non-negative. */
  private final int maxRetries;

  /** The delay between retry attempts in milliseconds. Default: 500ms. Must be positive. */
  private final long retryDelayMs;

  /**
   * Constructs a new SDKConfiguration with the specified parameters.
   *
   * <p>This constructor is package-private and is only called by {@link WeatherSDKBuilder} to
   * ensure that all configuration values are properly validated before object construction.
   *
   * @param apiKey the OpenWeatherAPI authentication key (required, non-empty)
   * @param mode the SDK operation mode (required, non-null)
   * @param pollingInterval the polling interval for background updates (required, positive)
   * @param cacheSize the maximum number of cities to cache (required, positive)
   * @param cacheTTL the time-to-live for cached weather data (required, positive)
   * @param timeouts the timeout configuration (required, not null)
   * @param maxRetries the maximum number of retry attempts (required, non-negative)
   * @param retryDelayMs the delay between retry attempts in milliseconds (required, positive)
   * @throws IllegalArgumentException if any parameter is invalid (null, empty, or non-positive)
   */
  SDKConfiguration(
      final String apiKey,
      final SDKMode mode,
      final Duration pollingInterval,
      final int cacheSize,
      final Duration cacheTTL,
      final TimeoutConfig timeouts,
      final int maxRetries,
      final long retryDelayMs) {

    validateApiKey(apiKey);
    validateMode(mode);
    validatePollingInterval(pollingInterval);
    validateCacheSize(cacheSize);
    validateCacheTTL(cacheTTL);
    validateTimeouts(timeouts);
    validateMaxRetries(maxRetries);
    validateRetryDelay(retryDelayMs);

    this.apiKey = apiKey.trim();
    this.mode = mode;
    this.pollingInterval = pollingInterval;
    this.cacheSize = cacheSize;
    this.cacheTTL = cacheTTL;
    this.timeouts = timeouts;
    this.maxRetries = maxRetries;
    this.retryDelayMs = retryDelayMs;
  }

  /**
   * Validates the API key parameter.
   *
   * @param apiKeyValue the API key to validate
   * @throws IllegalArgumentException if the API key is invalid
   */
  private void validateApiKey(final String apiKeyValue) {
    if (apiKeyValue == null || apiKeyValue.trim().isEmpty()) {
      throw new IllegalArgumentException("API key must not be null or empty");
    }
  }

  /**
   * Validates the mode parameter.
   *
   * @param modeValue the mode to validate
   * @throws IllegalArgumentException if the mode is invalid
   */
  private void validateMode(final SDKMode modeValue) {
    if (modeValue == null) {
      throw new IllegalArgumentException("SDK mode must not be null");
    }
  }

  /**
   * Validates the polling interval parameter.
   *
   * @param interval the polling interval to validate
   * @throws IllegalArgumentException if the polling interval is invalid
   */
  private void validatePollingInterval(final Duration interval) {
    if (interval == null || interval.isNegative() || interval.isZero()) {
      throw new IllegalArgumentException("Polling interval must be positive");
    }
  }

  /**
   * Validates the cache size parameter.
   *
   * @param size the cache size to validate
   * @throws IllegalArgumentException if the cache size is invalid
   */
  private void validateCacheSize(final int size) {
    if (size <= 0) {
      throw new IllegalArgumentException("Cache size must be positive");
    }
  }

  /**
   * Validates the cache TTL parameter.
   *
   * @param ttl the cache TTL to validate
   * @throws IllegalArgumentException if the cache TTL is invalid
   */
  private void validateCacheTTL(final Duration ttl) {
    if (ttl == null || ttl.isNegative() || ttl.isZero()) {
      throw new IllegalArgumentException("Cache TTL must be positive");
    }
  }

  /**
   * Validates the timeouts parameter.
   *
   * @param config the timeouts to validate
   * @throws IllegalArgumentException if the timeouts are invalid
   */
  private void validateTimeouts(final TimeoutConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("Timeouts must not be null");
    }
    // Validation is done in TimeoutConfig constructor
  }

  /**
   * Validates the max retries parameter.
   *
   * @param retries the max retries to validate
   * @throws IllegalArgumentException if max retries is invalid
   */
  private void validateMaxRetries(final int retries) {
    if (retries < 0) {
      throw new IllegalArgumentException("Max retries must be non-negative");
    }
  }

  /**
   * Validates the retry delay parameter.
   *
   * @param delay the retry delay to validate
   * @throws IllegalArgumentException if the retry delay is invalid
   */
  private void validateRetryDelay(final long delay) {
    if (delay <= 0) {
      throw new IllegalArgumentException("Retry delay must be positive");
    }
  }

  /**
   * Immutable record for HTTP timeout configuration.
   *
   * <p>This record groups connection and read timeouts to reduce constructor parameter count and
   * improve code organization.
   *
   * @param connectionTimeout the HTTP connection timeout
   * @param readTimeout the HTTP read timeout
   */
  public record TimeoutConfig(Duration connectionTimeout, Duration readTimeout) {

    /**
     * Validates the timeout configuration.
     *
     * @throws IllegalArgumentException if either timeout is invalid
     */
    public TimeoutConfig {
      if (connectionTimeout == null
          || connectionTimeout.isNegative()
          || connectionTimeout.isZero()) {
        throw new IllegalArgumentException("Connection timeout must be positive");
      }
      if (readTimeout == null || readTimeout.isNegative() || readTimeout.isZero()) {
        throw new IllegalArgumentException("Read timeout must be positive");
      }
    }
  }

  /**
   * Returns the OpenWeatherAPI authentication key.
   *
   * @return the API key (never null or empty)
   */
  public String getApiKey() {
    return apiKey;
  }

  /**
   * Returns the SDK operation mode.
   *
   * @return the operation mode (never null)
   */
  public SDKMode getMode() {
    return mode;
  }

  /**
   * Returns the polling interval for background updates.
   *
   * <p>This value is used only when the mode is {@link SDKMode#POLLING}.
   *
   * @return the polling interval (never null)
   */
  public Duration getPollingInterval() {
    return pollingInterval;
  }

  /**
   * Returns the maximum number of cities to cache.
   *
   * @return the cache size (positive integer)
   */
  public int getCacheSize() {
    return cacheSize;
  }

  /**
   * Returns the time-to-live for cached weather data.
   *
   * @return the cache TTL (never null)
   */
  public Duration getCacheTTL() {
    return cacheTTL;
  }

  /**
   * Returns the HTTP connection timeout.
   *
   * @return the connection timeout (never null)
   */
  public Duration getConnectionTimeout() {
    return timeouts.connectionTimeout();
  }

  /**
   * Returns the HTTP read timeout.
   *
   * @return the read timeout (never null)
   */
  public Duration getReadTimeout() {
    return timeouts.readTimeout();
  }

  /**
   * Returns the connection timeout in seconds.
   *
   * @return the connection timeout in seconds (positive integer)
   */
  public int getConnectionTimeoutSeconds() {
    return (int) timeouts.connectionTimeout().getSeconds();
  }

  /**
   * Returns the read timeout in seconds.
   *
   * @return the read timeout in seconds (positive integer)
   */
  public int getReadTimeoutSeconds() {
    return (int) timeouts.readTimeout().getSeconds();
  }

  /**
   * Returns the maximum number of retry attempts for API calls.
   *
   * @return the maximum number of retry attempts (non-negative integer)
   */
  public int getMaxRetries() {
    return maxRetries;
  }

  /**
   * Returns the delay between retry attempts in milliseconds.
   *
   * @return the delay between retry attempts in milliseconds (positive long)
   */
  public long getRetryDelayMs() {
    return retryDelayMs;
  }

  /**
   * Returns a string representation of this SDK configuration.
   *
   * <p>The string representation includes all configuration values except the API key for security
   * reasons (API key is masked).
   *
   * @return a string representation of this configuration
   */
  @Override
  public String toString() {
    return "SDKConfiguration{"
        + "apiKey=***"
        + ", mode="
        + mode
        + ", pollingInterval="
        + pollingInterval
        + ", cacheSize="
        + cacheSize
        + ", cacheTTL="
        + cacheTTL
        + ", connectionTimeout="
        + timeouts.connectionTimeout()
        + ", readTimeout="
        + timeouts.readTimeout()
        + ", maxRetries="
        + maxRetries
        + ", retryDelayMs="
        + retryDelayMs
        + '}';
  }

  /**
   * Compares this SDKConfiguration to the specified object for equality.
   *
   * <p>Two SDKConfiguration objects are considered equal if they have identical configuration
   * values. The API key is included in the equality comparison.
   *
   * @param obj the object to compare with
   * @return true if the objects are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SDKConfiguration that = (SDKConfiguration) obj;
    return cacheSize == that.cacheSize
        && maxRetries == that.maxRetries
        && retryDelayMs == that.retryDelayMs
        && Objects.equals(apiKey, that.apiKey)
        && mode == that.mode
        && Objects.equals(pollingInterval, that.pollingInterval)
        && Objects.equals(cacheTTL, that.cacheTTL)
        && Objects.equals(timeouts, that.timeouts);
  }

  /**
   * Returns a hash code value for this SDKConfiguration.
   *
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        apiKey, mode, pollingInterval, cacheSize, cacheTTL, timeouts, maxRetries, retryDelayMs);
  }
}
