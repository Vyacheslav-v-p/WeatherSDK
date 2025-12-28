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
 * Exception thrown when there are issues with SDK configuration.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>Invalid API key format or missing API key
 *   <li>Invalid operation mode (not ON_DEMAND or POLLING)Invalid polling interval (negative, zero,
 *       or unreasonably large)
 *   <li>Invalid cache size (negative or zero)
 *   <li>Invalid timeout values (negative or zero)
 *   <li>Null or missing required configuration parameters
 *   <li>Inconsistent configuration combinations
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #INVALID_CONFIG}
 *
 * <h2>Configuration Validation</h2>
 *
 * <p>The SDK validates all configuration parameters during builder construction:
 *
 * <ul>
 *   <li><strong>API Key:</strong> Must not be null or empty
 *   <li><strong>Mode:</strong> Must be ON_DEMAND or POLLING
 *   <li><strong>Polling Interval:</strong> Must be positive and reasonable (max 24 hours)
 *   <li><strong>Cache Size:</strong> Must be positive (max 1000 cities)
 *   <li><strong>Timeouts:</strong> Must be positive and reasonable (max 60 seconds)
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Verify all configuration parameters are within valid ranges
 *   <li>Use the builder's validation methods before building
 *   <li>Check for null values in required fields
 *   <li>Use configuration constants for common values
 *   <li>Implement configuration validation in tests
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Handling Configuration Exceptions</h3>
 *
 * <pre>
 * try {
 *   SDKConfiguration config = new WeatherSDKBuilder()
 *       .apiKey("") // Invalid: empty API key
 *       .mode(SDKMode.POLLING)
 *       .pollingInterval(Duration.ofMinutes(-5)) // Invalid: negative interval
 *       .build();
 *
 *   WeatherSDK sdk = new DefaultWeatherSDK(config);
 * } catch (ConfigurationException e) {
 *   logger.error("Configuration error ({}): {}", e.getErrorCode(), e.getMessage());
 *
 *   switch (e.getInvalidParameter()) {
 *     case "apiKey":
 *       logger.error("API key is required and cannot be empty");
 *       break;
 *     case "pollingInterval":
 *       logger.error("Polling interval must be positive");
 *       break;
 *     case "cacheSize":
 *       logger.error("Cache size must be positive");
 *       break;
 *   }
 * }
 * </pre>
 *
 * <h3>Proactive Configuration Validation</h3>
 *
 * <pre>
 * public WeatherSDK createValidatedSDK(String apiKey, SDKMode mode) throws ConfigurationException {
 *   if (apiKey == null || apiKey.trim().isEmpty()) {
 *     throw new ConfigurationException("API key is required", "apiKey");
 *   }
 *
 *   if (mode == null) {
 *     throw new ConfigurationException("Operation mode is required", "mode");
 *   }
 *
 *   try {
 *     SDKConfiguration config = new WeatherSDKBuilder()
 *         .apiKey(apiKey.trim())
 *         .mode(mode)
 *         .build();
 *
 *     return new DefaultWeatherSDK(config);
 *   } catch (Exception e) {
 *     throw new ConfigurationException("Invalid SDK configuration", e);
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.config.WeatherSDKBuilder
 * @see com.weather.sdk.config.SDKConfiguration
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class ConfigurationException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for configuration-related issues. */
  public static final String INVALID_CONFIG = "INVALID_CONFIGURATION";

  /** The name of the configuration parameter that is invalid. */
  private final String invalidParameter;

  /**
   * Constructs a new ConfigurationException with the specified message.
   *
   * @param message the detail message explaining the configuration error. Must not be null.
   * @throws NullPointerException if message is null
   */
  public ConfigurationException(final String message) {
    super(INVALID_CONFIG, message);
    this.invalidParameter = null;
  }

  /**
   * Constructs a new ConfigurationException with the specified message and invalid parameter.
   *
   * @param message the detail message explaining the configuration error. Must not be null.
   * @param invalidParameter the name of the configuration parameter that is invalid. May be null.
   * @throws NullPointerException if message is null
   */
  public ConfigurationException(final String message, final String invalidParameter) {
    super(INVALID_CONFIG, message);
    this.invalidParameter = invalidParameter;
  }

  /**
   * Constructs a new ConfigurationException with the specified message, invalid parameter, and
   * cause.
   *
   * @param message the detail message explaining the configuration error. Must not be null.
   * @param invalidParameter the name of the configuration parameter that is invalid. May be null.
   * @param cause the underlying cause of the configuration error. May be null.
   * @throws NullPointerException if message is null
   */
  public ConfigurationException(
      final String message, final String invalidParameter, final Throwable cause) {
    super(INVALID_CONFIG, message, cause);
    this.invalidParameter = invalidParameter;
  }

  /**
   * Returns the name of the configuration parameter that is invalid.
   *
   * @return the invalid parameter name, or null if not provided
   */
  public String getInvalidParameter() {
    return invalidParameter;
  }

  /**
   * Indicates whether the error is related to API key configuration.
   *
   * @return true if the error is related to API key
   */
  public boolean isApiKeyError() {
    return "apiKey".equalsIgnoreCase(invalidParameter);
  }

  /**
   * Indicates whether the error is related to operation mode configuration.
   *
   * @return true if the error is related to mode
   */
  public boolean isModeError() {
    return "mode".equalsIgnoreCase(invalidParameter);
  }

  /**
   * Indicates whether the error is related to polling interval configuration.
   *
   * @return true if the error is related to polling interval
   */
  public boolean isPollingIntervalError() {
    return "pollingInterval".equalsIgnoreCase(invalidParameter);
  }

  /**
   * Indicates whether the error is related to cache configuration.
   *
   * @return true if the error is related to cache
   */
  public boolean isCacheError() {
    return invalidParameter != null
        && invalidParameter.toLowerCase(java.util.Locale.ROOT).contains("cache");
  }

  /**
   * Indicates whether the error is related to timeout configuration.
   *
   * @return true if the error is related to timeout
   */
  public boolean isTimeoutError() {
    return invalidParameter != null
        && invalidParameter.toLowerCase(java.util.Locale.ROOT).contains("timeout");
  }

  /**
   * Returns a descriptive category for the type of configuration error.
   *
   * @return a descriptive error category
   */
  public String getErrorCategory() {
    final String result;
    if (isApiKeyError()) {
      result = "API Key Configuration Error";
    } else if (isModeError()) {
      result = "Mode Configuration Error";
    } else if (isPollingIntervalError()) {
      result = "Polling Interval Configuration Error";
    } else if (isCacheError()) {
      result = "Cache Configuration Error";
    } else if (isTimeoutError()) {
      result = "Timeout Configuration Error";
    } else if (invalidParameter != null) {
      result = "Parameter Configuration Error";
    } else {
      result = "Configuration Error";
    }
    return result;
  }

  /**
   * Returns a suggested fix for the configuration error.
   *
   * @return a suggestion for fixing the configuration issue
   */
  public String getSuggestedFix() {
    final String result;
    if (isApiKeyError()) {
      result = "Provide a valid, non-empty API key from OpenWeatherAPI";
    } else if (isModeError()) {
      result = "Use SDKMode.ON_DEMAND or SDKMode.POLLING";
    } else if (isPollingIntervalError()) {
      result = "Use a positive Duration (e.g., Duration.ofMinutes(5))";
    } else if (isCacheError()) {
      result = "Use a positive cache size (e.g., 10, 50, 100)";
    } else if (isTimeoutError()) {
      result = "Use positive timeout durations (e.g., Duration.ofSeconds(5))";
    } else {
      result = "Review configuration parameters and ensure all values are valid";
    }
    return result;
  }

  /**
   * Returns the exception type description.
   *
   * @return "Configuration Error"
   */
  @Override
  public String getExceptionType() {
    return "Configuration Error";
  }

  /**
   * Returns a detailed message including the invalid parameter if available.
   *
   * @return a detailed error message
   */
  @Override
  public String getMessage() {
    final String baseMessage = super.getMessage();
    final String result;

    if (invalidParameter != null) {
      result = String.format("%s [Parameter: %s]", baseMessage, invalidParameter);
    } else {
      result = baseMessage;
    }

    return result;
  }
}
