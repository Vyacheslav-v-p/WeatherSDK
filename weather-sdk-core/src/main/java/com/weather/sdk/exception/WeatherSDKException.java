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
 * The base exception class for all weather SDK related errors.
 *
 * <p>This is the root exception for the Weather SDK exception hierarchy. All other weather-specific
 * exceptions extend from this class, providing a common base for catch-all exception handling while
 * still allowing for specific exception handling through the more specialized subclasses.
 *
 * <h2>Exception Hierarchy</h2>
 *
 * <p>The Weather SDK uses a comprehensive exception hierarchy for clear error handling:
 *
 * <ul>
 *   <li><strong>WeatherSDKException</strong> - Base class for all SDK errors
 *   <li><strong>ApiKeyException</strong> - API key related issues (invalid, missing, etc.)
 *   <li><strong>CityNotFoundException</strong> - City not found in OpenWeatherAPI
 *   <li><strong>RateLimitException</strong> - API rate limit exceeded
 *   <li><strong>NetworkException</strong> - Network connectivity or HTTP errors
 *   <li><strong>CacheException</strong> - Cache operation failures
 *   <li><strong>ConfigurationException</strong> - Invalid configuration parameters
 * </ul>
 *
 * <h2>Error Codes</h2>
 *
 * <p>Each exception type provides a standardized error code that can be used for:
 *
 * <ul>
 *   <li>Programmatic error handling and routing
 *   <li>Logging and monitoring systems
 *   <li>User-facing error messages
 *   <li>Analytics and error tracking
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Catch Specific Exceptions</h3>
 *
 * <pre>
 * try {
 *   WeatherData weather = sdk.getWeather("London");
 * } catch (CityNotFoundException e) {
 *   logger.warn("City not found: {}", e.getMessage());
 *   // Handle city not found specifically
 * } catch (ApiKeyException e) {
 *   logger.error("Invalid API key: {}", e.getMessage());
 *   // Handle API key issues
 * } catch (RateLimitException e) {
 *   logger.warn("Rate limit exceeded: {}", e.getMessage());
 *   // Implement backoff strategy
 * }
 * </pre>
 *
 * <h3>Catch All SDK Exceptions</h3>
 *
 * <pre>
 * try {
 *   WeatherData weather = sdk.getWeather("Paris");
 * } catch (WeatherSDKException e) {
 *   logger.error("Weather SDK error ({}): {}", e.getErrorCode(), e.getMessage());
 *   // Handle any weather-related error
 * }
 * </pre>
 *
 * <h3>Error Code Usage</h3>
 *
 * <pre>
 * // Programmatic error handling based on error codes
 * try {
 *   WeatherData weather = sdk.getWeather("Berlin");
 * } catch (WeatherSDKException e) {
 *   switch (e.getErrorCode()) {
 *     case INVALID_API_KEY:
 *       // Trigger API key rotation
 *       rotateApiKey();
 *       break;
 *     case RATE_LIMIT_EXCEEDED:
 *       // Implement exponential backoff
 *       backoffAndRetry();
 *       break;
 *     case CITY_NOT_FOUND:
 *       // Suggest alternative city names
 *       suggestSimilarCities();
 *       break;
 *     default:
 *       // Handle unknown errors
 *       handleUnknownError(e);
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.exception.ApiKeyException
 * @see com.weather.sdk.exception.CityNotFoundException
 * @see com.weather.sdk.exception.RateLimitException
 * @see com.weather.sdk.exception.NetworkException
 * @see com.weather.sdk.exception.CacheException
 * @see com.weather.sdk.exception.ConfigurationException
 */
public abstract class WeatherSDKException extends Exception {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** The error code for this exception type. This constant should be overridden by subclasses. */
  public static final String WEATHER_SDK_ERROR = "WEATHER_SDK_ERROR";

  /** The error code associated with this exception. */
  private final String errorCode;

  /**
   * Constructs a new WeatherSDKException with the specified error code and message.
   *
   * @param errorCode the error code for this exception. Must not be null or empty.
   * @param message the detail message. Must not be null.
   * @throws NullPointerException if errorCode or message is null
   * @throws IllegalArgumentException if errorCode is empty
   */
  @SuppressWarnings("CT_CONSTRUCTOR_THROW")
  protected WeatherSDKException(final String errorCode, final String message) {
    super(message);
    this.errorCode = validateErrorCode(errorCode);
  }

  /**
   * Creates a new WeatherSDKException with the specified error code and message.
   *
   * @param errorCode the error code for this exception. Must not be null or empty.
   * @param message the detail message. Must not be null.
   * @return a new WeatherSDKException instance
   * @throws NullPointerException if errorCode or message is null
   * @throws IllegalArgumentException if errorCode is empty
   */
  public static WeatherSDKException create(final String errorCode, final String message) {
    return new WeatherSDKException(errorCode, message) {};
  }

  /**
   * Constructs a new WeatherSDKException with the specified error code, message, and cause.
   *
   * @param errorCode the error code for this exception. Must not be null or empty.
   * @param message the detail message. Must not be null.
   * @param cause the underlying cause. May be null.
   * @throws NullPointerException if errorCode or message is null
   * @throws IllegalArgumentException if errorCode is empty
   */
  @SuppressWarnings("CT_CONSTRUCTOR_THROW")
  protected WeatherSDKException(
      final String errorCode, final String message, final Throwable cause) {
    super(message, cause);
    this.errorCode = validateErrorCode(errorCode);
  }

  /**
   * Returns the error code associated with this exception.
   *
   * <p>Error codes are standardized strings that identify the type of error that occurred. They can
   * be used for programmatic error handling, logging, and monitoring.
   *
   * @return the error code. Never returns null or empty.
   */
  public String getErrorCode() {
    return errorCode;
  }

  /**
   * Returns a formatted string containing both the error code and message.
   *
   * <p>This is useful for logging and error reporting where both pieces of information are needed.
   *
   * @return a formatted error string in the format "ERROR_CODE: message"
   */
  public String getFormattedMessage() {
    return String.format("%s: %s", errorCode, getMessage());
  }

  /**
   * Returns a brief description of this exception type.
   *
   * <p>Subclasses should override this method to provide a more specific description of the
   * exception type.
   *
   * @return a brief description of this exception
   */
  public String getExceptionType() {
    return "Weather SDK Error";
  }

  /**
   * Validates that the error code is not null or empty.
   *
   * @param errorCode the error code to validate
   * @return the validated error code
   * @throws NullPointerException if errorCode is null
   * @throws IllegalArgumentException if errorCode is empty
   */
  private static String validateErrorCode(final String errorCode) {
    if (errorCode == null) {
      throw new IllegalArgumentException("Error code must not be null");
    }
    final String trimmed = errorCode.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Error code must not be empty");
    }
    return trimmed;
  }

  @Override
  public String toString() {
    return String.format("%s[%s]: %s", getClass().getSimpleName(), errorCode, getMessage());
  }
}
