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
 * Exception thrown when there are issues with the API key.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>The API key is invalid or malformed
 *   <li>The API key is missing or null
 *   <li>The API key has insufficient permissions
 *   <li>The API key has been revoked or deactivated
 *   <li>The API key has exceeded its usage limits
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #INVALID_API_KEY}
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li>Incorrect API key format or length
 *   <li>Expired or revoked API key
 *   <li>Insufficient API key permissions for requested endpoints
 *   <li>API key quota exceeded
 *   <li>Typo in API key during configuration
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Verify the API key is correctly copied from OpenWeatherAPI dashboard
 *   <li>Check if the API key is active and not expired
 *   <li>Ensure the API key has proper permissions
 *   <li>Consider rotating to a backup API key if available
 *   <li>Contact OpenWeatherAPI support if the issue persists
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Catching API Key Exceptions</h3>
 *
 * <pre>
 * try {
 *   SDKConfiguration config = new WeatherSDKBuilder()
 *       .apiKey("invalid-key")
 *       .build();
 *
 *   WeatherSDK sdk = new DefaultWeatherSDK(config);
 * } catch (ApiKeyException e) {
 *   logger.error("API key error ({}): {}", e.getErrorCode(), e.getMessage());
 *
 *   if (e.getErrorCode().equals(ApiKeyException.INVALID_API_KEY)) {
 *     // Prompt user to enter correct API key
 *     String newApiKey = promptForApiKey();
 *     // Retry with correct key
 *   }
 * }
 * </pre>
 *
 * <h3>API Key Validation</h3>
 *
 * <pre>
 * // Validate API key before SDK creation
 * public boolean isValidApiKey(String apiKey) {
 *   try {
 *     SDKConfiguration config = new WeatherSDKBuilder()
 *         .apiKey(apiKey)
 *         .build();
 *
 *     WeatherSDK sdk = new DefaultWeatherSDK(config);
 *
 *     // Test with a simple request
 *     sdk.getWeather("London");
 *     return true;
 *
 *   } catch (ApiKeyException e) {
 *     return false;
 *   } catch (Exception e) {
 *     // Other exceptions don't indicate API key issues
 *     return true;
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class ApiKeyException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for API key related issues. */
  public static final String INVALID_API_KEY = "INVALID_API_KEY";

  /**
   * Constructs a new ApiKeyException with the specified message.
   *
   * @param message the detail message explaining the API key issue. Must not be null.
   * @throws NullPointerException if message is null
   */
  public ApiKeyException(final String message) {
    super(INVALID_API_KEY, message);
  }

  /**
   * Constructs a new ApiKeyException with the specified message and cause.
   *
   * @param message the detail message explaining the API key issue. Must not be null.
   * @param cause the underlying cause of the API key issue. May be null.
   * @throws NullPointerException if message is null
   */
  public ApiKeyException(final String message, final Throwable cause) {
    super(INVALID_API_KEY, message, cause);
  }

  /**
   * Constructs a new ApiKeyException with the specified details.
   *
   * @param message the detail message explaining the API key issue. Must not be null.
   * @param apiKey the problematic API key (for logging purposes). May be null or partially masked.
   * @param cause the underlying cause of the API key issue. May be null.
   */
  public ApiKeyException(final String message, final String apiKey, final Throwable cause) {
    super(
        INVALID_API_KEY,
        message + (apiKey != null ? " [API Key: " + maskApiKey(apiKey) + "]" : ""),
        cause);
  }

  /**
   * Returns the exception type description.
   *
   * @return "API Key Error"
   */
  @Override
  public String getExceptionType() {
    return "API Key Error";
  }

  /**
   * Masks the API key for safe logging (shows only first and last 4 characters).
   *
   * @param apiKey the API key to mask
   * @return the masked API key string
   */
  private static String maskApiKey(final String apiKey) {
    final String result;
    if (apiKey == null || apiKey.length() <= 8) {
      result = "***MASKED***";
    } else {
      result = apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
    return result;
  }
}
