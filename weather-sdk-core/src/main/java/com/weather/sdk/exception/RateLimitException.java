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
 * Exception thrown when the OpenWeatherAPI rate limit is exceeded.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>The API key has exceeded its rate limit (60 calls per minute for free tier)
 *   <li>The request frequency is too high for the current API key
 *   <li>The application is making requests too quickly
 *   <li>The API key has reached its daily/monthly quota limit
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #ERROR_CODE}
 *
 * <h2>Rate Limits</h2>
 *
 * <ul>
 *   <li><strong>Free Tier:</strong> 60 calls per minute, 1,000,000 calls per month
 *   <li><strong>Paid Tiers:</strong> Higher limits depending on subscription level
 *   <li><strong>Enterprise:</strong> Custom rate limits
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Implement exponential backoff strategy
 *   <li>Cache weather data to reduce API calls
 *   <li>Consider upgrading to a higher tier API plan
 *   <li>Implement request queuing to spread calls over time
 *   <li>Use batch requests where possible
 * </ul>
 *
 * <h2>Retry Strategy</h2>
 *
 * <p>The SDK implements automatic retry logic for rate limit exceptions:
 *
 * <ul>
 *   <li>Exponential backoff with jitter (500ms base, 2x multiplier)
 *   <li>Maximum 3 retry attempts
 *   <li>Logs retry attempts at WARN level
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Handling Rate Limit Exceptions</h3>
 *
 * <pre>
 * try {
 *   WeatherData weather = sdk.getWeather("London");
 * } catch (RateLimitException e) {
 *   logger.warn("Rate limit exceeded: {}", e.getMessage());
 *
 *   // Implement exponential backoff
 *   try {
 *     Thread.sleep(calculateBackoffDelay(e.getRetryAfterSeconds()));
 *     WeatherData weather = sdk.getWeather("London");
 *   } catch (InterruptedException ie) {
 *     Thread.currentThread().interrupt();
 *     throw new RuntimeException("Interrupted during backoff", ie);
 *   }
 * }
 * </pre>
 *
 * <h3>Proactive Rate Limit Management</h3>
 *
 * <pre>
 * public class RateLimitManager {
 *   private final RateLimiter limiter = RateLimiter.create(50.0); // 50 calls per minute
 *
 *   public WeatherData getWeatherWithRateLimit(String city) throws WeatherSDKException {
 *     limiter.acquire(); // Blocks until permit available
 *     return sdk.getWeather(city);
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK#getWeather(String)
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class RateLimitException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for rate limit exceeded issues. */
  public static final String ERROR_CODE = "RATE_LIMIT_EXCEEDED";

  /** One minute in seconds. */
  private static final int MINUTE_60_SEC = 60;

  /** One hour in seconds. */
  private static final int HOUR_3600_SEC = 3600;

  /** The number of seconds to wait before making another request. */
  private final Integer retryAfterSeconds;

  /**
   * Constructs a new RateLimitException with the specified message.
   *
   * @param message the detail message explaining the rate limit issue. Must not be null.
   * @throws NullPointerException if message is null
   */
  public RateLimitException(final String message) {
    super(ERROR_CODE, message);
    this.retryAfterSeconds = null;
  }

  /**
   * Constructs a new RateLimitException with the specified message and retry-after time.
   *
   * @param message the detail message explaining the rate limit issue. Must not be null.
   * @param retryAfterSeconds the number of seconds to wait before making another request. May be
   *     null if not provided by the API.
   * @throws NullPointerException if message is null
   */
  public RateLimitException(final String message, final Integer retryAfterSeconds) {
    super(ERROR_CODE, message);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  /**
   * Constructs a new RateLimitException with the specified message, retry-after time, and cause.
   *
   * @param message the detail message explaining the rate limit issue. Must not be null.
   * @param retryAfterSeconds the number of seconds to wait before making another request. May be
   *     null if not provided by the API.
   * @param cause the underlying cause of the rate limit issue. May be null.
   * @throws NullPointerException if message is null
   */
  public RateLimitException(
      final String message, final Integer retryAfterSeconds, final Throwable cause) {
    super(ERROR_CODE, message, cause);
    this.retryAfterSeconds = retryAfterSeconds;
  }

  /**
   * Returns the number of seconds to wait before making another request.
   *
   * @return the retry-after time in seconds, or null if not provided
   */
  public Integer getRetryAfterSeconds() {
    return retryAfterSeconds;
  }

  /**
   * Returns the recommended wait time in milliseconds.
   *
   * @return the retry-after time in milliseconds, or a default 60000ms (1 minute) if null
   */
  public long getRetryAfterMillis() {
    return retryAfterSeconds != null ? retryAfterSeconds * 1000L : 60_000L;
  }

  /**
   * Calculates a recommended backoff delay using exponential backoff with jitter.
   *
   * @param attempt the current retry attempt number (1-based)
   * @return the backoff delay in milliseconds
   */
  public long calculateBackoffDelay(final int attempt) {
    final long result;
    if (retryAfterSeconds != null) {
      // Use the API-provided retry-after time if available
      result = getRetryAfterMillis();
    } else {
      // Exponential backoff: base 500ms, max 32 seconds
      final long baseDelay = 500L;
      final long maxDelay = 32_000L;
      final long delay = Math.min(baseDelay * (1L << (attempt - 1)), maxDelay);

      // Add jitter (±25% random variation)
      final double jitter = 0.75 + (Math.random() * 0.5);
      result = (long) (delay * jitter);
    }
    return result;
  }

  /**
   * Returns a human-readable description of when the rate limit will reset.
   *
   * @return a description of the reset time, or null if unknown
   */
  public String getResetTimeDescription() {
    final String result;
    if (retryAfterSeconds == null) {
      result = null;
    } else if (retryAfterSeconds < MINUTE_60_SEC) {
      result = retryAfterSeconds + " seconds";
    } else if (retryAfterSeconds < HOUR_3600_SEC) {
      result = (retryAfterSeconds / 60) + " minutes";
    } else {
      result = (retryAfterSeconds / 3600) + " hours";
    }
    return result;
  }

  /**
   * Returns the exception type description.
   *
   * @return "Rate Limit Exceeded Error"
   */
  @Override
  public String getExceptionType() {
    return "Rate Limit Exceeded Error";
  }
}
