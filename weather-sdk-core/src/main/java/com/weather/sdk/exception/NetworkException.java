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
 * Exception thrown when network-related errors occur during API communication.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>Network connectivity is lost or unavailable
 *   <li>DNS resolution fails for the API endpoint
 *   <li>HTTP connection timeout occurs
 *   <li>SSL/TLS handshake failures
 *   <li>HTTP 5xx server errors from OpenWeatherAPI
 *   <li>Unexpected HTTP response codes
 *   <li>Malformed API responses
 *   <li>Connection refused by the server
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #NETWORK_ERROR}
 *
 * <h2>Retry Logic</h2>
 *
 * <p>The SDK implements automatic retry logic for network exceptions:
 *
 * <ul>
 *   <li>Maximum 3 retry attempts
 *   <li>Fixed delay of 500ms between retries
 *   <li>Exponential backoff strategy
 *   <li>Does not retry on client errors (4xx responses)
 *   <li>Logs retry attempts at DEBUG level
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Check internet connectivity and firewall settings
 *   <li>Verify OpenWeatherAPI service status
 *   <li>Increase timeout values if needed
 *   <li>Implement offline/cached data fallback
 *   <li>Use circuit breaker pattern for repeated failures
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Handling Network Exceptions</h3>
 *
 * <pre>
 * try {
 *   WeatherData weather = sdk.getWeather("London");
 * } catch (NetworkException e) {
 *   logger.warn("Network error ({}): {}", e.getErrorCode(), e.getMessage());
 *
 *   if (e.getCause() instanceof ConnectException) {
 *     logger.error("Unable to connect to weather service");
 *     showOfflineModeMessage();
 *   } else if (e.getHttpStatusCode().isPresent()) {
 *     int statusCode = e.getHttpStatusCode().getAsInt();
 *     if (statusCode >= 500) {
 *       logger.error("Weather service error: HTTP {}", statusCode);
 *       // Service may be temporarily down
 *     }
 *   }
 * }
 * </pre>
 *
 * <h3>Circuit Breaker Pattern</h3>
 *
 * <pre>
 * public class ResilientWeatherService {
 *   private final CircuitBreaker breaker = new CircuitBreaker();
 *
 *   public WeatherData getWeather(String city) throws WeatherSDKException {
 *     return breaker.execute(() -> {
 *       try {
 *         return sdk.getWeather(city);
 *       } catch (NetworkException e) {
 *         breaker.recordFailure();
 *         throw e;
 *       }
 *     });
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK#getWeather(String)
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class NetworkException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for network-related issues. */
  public static final String NETWORK_ERROR = "NETWORK_ERROR";

  /** The API endpoint URL that failed. */
  private final String endpointUrl;

  /** The HTTP status code returned by the server. */
  private final Integer httpStatusCode;

  /**
   * Constructs a new NetworkException with the specified message.
   *
   * @param message the detail message explaining the network error. Must not be null.
   * @throws NullPointerException if message is null
   */
  public NetworkException(final String message) {
    super(NETWORK_ERROR, message);
    this.endpointUrl = null;
    this.httpStatusCode = null;
  }

  /**
   * Constructs a new NetworkException with the specified message and endpoint.
   *
   * @param message the detail message explaining the network error. Must not be null.
   * @param endpointUrl the API endpoint URL that failed. May be null.
   * @throws NullPointerException if message is null
   */
  public NetworkException(final String message, final String endpointUrl) {
    super(NETWORK_ERROR, message);
    this.endpointUrl = endpointUrl;
    this.httpStatusCode = null;
  }

  /**
   * Constructs a new NetworkException with the specified message, endpoint, and HTTP status code.
   *
   * @param message the detail message explaining the network error. Must not be null.
   * @param endpointUrl the API endpoint URL that failed. May be null.
   * @param httpStatusCode the HTTP status code returned by the server. May be null.
   * @throws NullPointerException if message is null
   */
  public NetworkException(
      final String message, final String endpointUrl, final Integer httpStatusCode) {
    super(NETWORK_ERROR, message);
    this.endpointUrl = endpointUrl;
    this.httpStatusCode = httpStatusCode;
  }

  /**
   * Constructs a new NetworkException with the specified message, endpoint, HTTP status code, and
   * cause.
   *
   * @param message the detail message explaining the network error. Must not be null.
   * @param endpointUrl the API endpoint URL that failed. May be null.
   * @param httpStatusCode the HTTP status code returned by the server. May be null.
   * @param cause the underlying cause of the network error. May be null.
   * @throws NullPointerException if message is null
   */
  public NetworkException(
      final String message,
      final String endpointUrl,
      final Integer httpStatusCode,
      final Throwable cause) {
    super(NETWORK_ERROR, message, cause);
    this.endpointUrl = endpointUrl;
    this.httpStatusCode = httpStatusCode;
  }

  /**
   * Returns the API endpoint URL that failed.
   *
   * @return the endpoint URL, or null if not provided
   */
  public String getEndpointUrl() {
    return endpointUrl;
  }

  /**
   * Returns the HTTP status code returned by the server.
   *
   * @return the HTTP status code, or null if not available
   */
  public Integer getHttpStatusCode() {
    return httpStatusCode;
  }

  /**
   * Indicates whether this was a server error (5xx status code).
   *
   * @return true if the HTTP status code indicates a server error
   */
  public boolean isServerError() {
    return httpStatusCode != null && httpStatusCode >= 500;
  }

  /**
   * Indicates whether this was a client error (4xx status code).
   *
   * @return true if the HTTP status code indicates a client error
   */
  public boolean isClientError() {
    return httpStatusCode != null && httpStatusCode >= 400 && httpStatusCode < 500;
  }

  /**
   * Indicates whether this was a connection-related error (no HTTP response received).
   *
   * @return true if the error was connection-related rather than HTTP-based
   */
  public boolean isConnectionError() {
    return httpStatusCode == null;
  }

  /**
   * Gets a descriptive category for the type of network error.
   *
   * @return a descriptive error category
   */
  public String getErrorCategory() {
    final String result;
    if (isConnectionError()) {
      result = "Connection Error";
    } else if (isServerError()) {
      result = "Server Error (5xx)";
    } else if (isClientError()) {
      result = "Client Error (4xx)";
    } else {
      result = "HTTP Error";
    }
    return result;
  }

  /**
   * Returns the exception type description.
   *
   * @return "Network Error"
   */
  @Override
  public String getExceptionType() {
    return "Network Error";
  }

  /**
   * Returns a formatted message including endpoint and status code if available.
   *
   * @return a detailed error message
   */
  @Override
  public String getMessage() {
    final String baseMessage = super.getMessage();
    final String result;

    if (endpointUrl != null && httpStatusCode != null) {
      result =
          String.format("%s [Endpoint: %s, Status: %d]", baseMessage, endpointUrl, httpStatusCode);
    } else if (endpointUrl != null) {
      result = String.format("%s [Endpoint: %s]", baseMessage, endpointUrl);
    } else if (httpStatusCode != null) {
      result = String.format("%s [Status: %d]", baseMessage, httpStatusCode);
    } else {
      result = baseMessage;
    }

    return result;
  }
}
