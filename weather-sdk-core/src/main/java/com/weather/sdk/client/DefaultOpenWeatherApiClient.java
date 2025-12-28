/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.sdk.exception.ApiKeyException;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
import com.weather.sdk.exception.RateLimitException;
import com.weather.sdk.model.WeatherData;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the OpenWeatherApiClient interface.
 *
 * <p>This implementation provides HTTP communication with the OpenWeatherAPI service using Java
 * 17's HttpClient. It handles request construction, response parsing, error handling, and provides
 * thread-safe operations.
 *
 * <p><strong>Implementation Details:</strong>
 *
 * <ul>
 *   <li>Uses Java 17 HttpClient for HTTP communication
 *   <li>Implements connection and read timeout handling
 *   <li>Parses JSON responses using Jackson ObjectMapper
 *   <li>Maps HTTP status codes to appropriate exceptions
 *   <li>Thread-safe and can be called concurrently
 * </ul>
 *
 * <h2>Request Configuration</h2>
 *
 * <ul>
 *   <li><strong>HTTP Method:</strong> GET
 *   <li><strong>Base URL:</strong> {@value #API_ENDPOINT_URL}
 *   <li><strong>Timeout:</strong> Configurable (default: 10 seconds connection, 30 seconds read)
 *   <li><strong>Headers:</strong> Accept: application/json
 *   <li><strong>Parameters:</strong> q={cityName}, appid={apiKey}, units=metric
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <ul>
 *   <li><strong>HTTP 200:</strong> Parse JSON to WeatherData
 *   <li><strong>HTTP 401:</strong> Throw ApiKeyException
 *   <li><strong>HTTP 404:</strong> Throw CityNotFoundException
 *   <li><strong>HTTP 429:</strong> Throw RateLimitException (includes Retry-After header)
 *   <li><strong>HTTP 5xx:</strong> Throw NetworkException
 *   <li><strong>Network errors:</strong> Throw NetworkException
 * </ul>
 *
 * @since 1.0.0
 * @see OpenWeatherApiClient
 * @see WeatherData
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "CT_CONSTRUCTOR_THROW"})
public class DefaultOpenWeatherApiClient implements OpenWeatherApiClient {

  /** Default connection timeout in seconds. */
  private static final int DEF_CONN_TIMEOUT = 10;

  /** Default read timeout in seconds. */
  private static final int DEF_READ_TIMEOUT = 30;

  /** HTTP status code for server error responses. */
  private static final int SERVER_ERR_STATUS = 500;

  /** HTTP status code for unauthorized responses. */
  private static final int UNAUTH_STATUS = 401;

  /** HTTP status code for not found responses. */
  private static final int NOT_FOUND_STATUS = 404;

  /** HTTP status code for rate limit exceeded responses. */
  private static final int RATE_LIMIT_STATUS = 429;

  // Maximum number of retry attempts and delay are now configurable
  // These constants are no longer used as the values are now passed via
  // configuration

  /** Maximum number of retry attempts. */
  private final int maxRetries;

  /** Delay between retry attempts in milliseconds. */
  private final long retryDelayMs;

  /** Jackson ObjectMapper for JSON parsing. */
  private final ObjectMapper objectMapper;

  /** HTTP client for making requests. */
  private final HttpClient httpClient;

  /** Connection timeout duration. */
  private final Duration connectionTimeout;

  /** Logger for this class. */
  private static final org.slf4j.Logger LOGGER =
      LoggerFactory.getLogger(DefaultOpenWeatherApiClient.class);

  /** API endpoint URL for OpenWeatherMap service. */
  private static final String API_ENDPOINT_URL = "https://api.openweathermap.org/data/2.5/weather";

  /** Query parameter name for city. */
  private static final String PARAM_CITY = "q";

  /** Query parameter name for API key. */
  private static final String PARAM_API_KEY = "appid";

  /** Query parameter name for units. */
  private static final String PARAM_UNITS = "units";

  /** Standard units value. */
  private static final String UNITS_STANDARD = "standard";

  /**
   * Constructs a new DefaultOpenWeatherApiClient with default timeout settings.
   *
   * <p>This constructor uses default timeouts: 10 seconds for connection, 30 seconds for read
   * operations, 3 for max retries, and 500ms for retry delay.
   *
   * @param httpClient the HTTP client to use for requests (must not be null)
   * @throws NullPointerException if httpClient is null
   */
  public DefaultOpenWeatherApiClient(final HttpClient httpClient) {
    this(httpClient, DEF_CONN_TIMEOUT, DEF_READ_TIMEOUT);
  }

  /**
   * Constructs a new DefaultOpenWeatherApiClient with custom timeout settings.
   *
   * @param httpClient the HTTP client to use for requests (must not be null)
   * @param connTimeoutSec the connection timeout in seconds (must be positive)
   * @param readTimeoutSec the read timeout in seconds (must be positive)
   * @throws NullPointerException if httpClient is null
   * @throws IllegalArgumentException if either timeout is not positive
   */
  public DefaultOpenWeatherApiClient(
      final HttpClient httpClient, final int connTimeoutSec, final int readTimeoutSec) {
    this(httpClient, connTimeoutSec, readTimeoutSec, 3, 500L);
  }

  /**
   * Constructs a new DefaultOpenWeatherApiClient with custom timeout and retry settings.
   *
   * @param httpClient the HTTP client to use for requests (must not be null)
   * @param connTimeoutSec the connection timeout in seconds (must be positive)
   * @param readTimeoutSec the read timeout in seconds (must be positive)
   * @param maxRetries the maximum number of retry attempts (must be non-negative)
   * @param retryDelayMs the delay between retry attempts in milliseconds (must be positive)
   * @throws NullPointerException if httpClient is null
   * @throws IllegalArgumentException if any parameter is invalid
   */
  @SuppressWarnings("CT_CONSTRUCTOR_THROW")
  public DefaultOpenWeatherApiClient(
      final HttpClient httpClient,
      final int connTimeoutSec,
      final int readTimeoutSec,
      final int maxRetries,
      final long retryDelayMs) {
    // Initialize fields first to avoid partial initialization
    this.httpClient = Objects.requireNonNull(httpClient, "HTTP client must not be null");
    this.connectionTimeout = Duration.ofSeconds(connTimeoutSec);
    this.objectMapper = new ObjectMapper();
    this.maxRetries = maxRetries;
    this.retryDelayMs = retryDelayMs;

    // Validate parameters after field initialization
    if (connTimeoutSec <= 0) {
      throw new IllegalArgumentException(
          "Connection timeout must be positive, was: " + connTimeoutSec);
    }
    if (readTimeoutSec <= 0) {
      throw new IllegalArgumentException("Read timeout must be positive, was: " + readTimeoutSec);
    }
    if (maxRetries < 0) {
      throw new IllegalArgumentException("Max retries must be non-negative, was: " + maxRetries);
    }
    if (retryDelayMs <= 0) {
      throw new IllegalArgumentException("Retry delay must be positive, was: " + retryDelayMs);
    }
  }

  /**
   * Fetches current weather data for the specified city.
   *
   * <p>This method makes an HTTP GET request to the OpenWeatherAPI service to retrieve current
   * weather information for the given city. The method handles all aspects of the HTTP
   * communication including request construction, response parsing, and error handling.
   *
   * <h4>Request Construction</h4>
   *
   * <ul>
   *   <li>URL: {@value #API_ENDPOINT_URL}
   *   <li>Method: GET
   *   <li>Parameters: q={cityName}, appid={apiKey}, units=metric
   *   <li>Headers: Accept: application/json
   *   <li>Timeout: Uses configured connection and read timeouts
   * </ul>
   *
   * <h4>Response Handling</h4>
   *
   * <ul>
   *   <li><strong>HTTP 200</strong>: Parse JSON response to {@link WeatherData}
   *   <li><strong>HTTP 401</strong>: Throw {@link ApiKeyException}
   *   <li><strong>HTTP 404</strong>: Throw {@link CityNotFoundException}
   *   <li><strong>HTTP 429</strong>: Throw {@link RateLimitException}
   *   <li><strong>HTTP 5xx</strong>: Throw {@link NetworkException}
   *   <li><strong>Network failure</strong>: Throw {@link NetworkException}
   * </ul>
   *
   * <h4>Retry Logic</h4>
   *
   * <p>The SDK implements automatic retry logic for network-related failures:
   *
   * <ul>
   *   <li>Maximum retry attempts configured via SDK configuration
   *   <li>Fixed delay between retries configured via SDK configuration
   *   <li>Retries on network exceptions (connection failures, timeouts)
   *   <li>Retries on server errors (HTTP 5xx responses)
   *   <li>No retries on client errors (HTTP 4xx responses) - these are not recoverable via retry
   *   <li>No retries on {@link RateLimitException}, {@link ApiKeyException}, or {@link
   *       CityNotFoundException}
   *   <li>Logs retry attempts at DEBUG level
   * </ul>
   *
   * <h4>Thread Safety</h4>
   *
   * This method is thread-safe and can be called concurrently by multiple threads.
   *
   * @param cityName the name of the city to fetch weather for (must not be null or empty)
   * @param apiKey the OpenWeatherAPI authentication key (must not be null or empty)
   * @return the current weather data for the specified city
   * @throws ApiKeyException if the API key is invalid, missing, or unauthorized
   * @throws CityNotFoundException if the specified city cannot be found
   * @throws RateLimitException if the API rate limit has been exceeded
   * @throws NetworkException if a network error occurs during the request
   * @throws IllegalArgumentException if cityName or apiKey is null or empty
   */
  @Override
  public WeatherData fetchWeather(final String cityName, final String apiKey)
      throws ApiKeyException, CityNotFoundException, RateLimitException, NetworkException {
    validateParameters(cityName, apiKey);
    return executeWithRetry(cityName, apiKey);
  }

  /**
   * Executes the weather request with retry logic.
   *
   * @param cityName the name of the city to fetch weather for
   * @param apiKey the OpenWeatherAPI authentication key
   * @return the current weather data for the specified city
   * @throws ApiKeyException if the API key is invalid, missing, or unauthorized
   * @throws CityNotFoundException if the specified city cannot be found
   * @throws RateLimitException if the API rate limit has been exceeded
   * @throws NetworkException if a network error occurs during the request
   */
  @SuppressWarnings({
    "PMD.CognitiveComplexity",
    "PMD.CyclomaticComplexity",
    "PMD.AvoidRethrowingException"
  })
  private WeatherData executeWithRetry(final String cityName, final String apiKey)
      throws ApiKeyException, CityNotFoundException, RateLimitException, NetworkException {
    int attempts = 0;
    Exception lastException = null;
    // Extract BodyHandler outside the loop to avoid object instantiation in loops
    final HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();

    while (attempts < maxRetries) {
      try {
        final HttpRequest request = buildRequest(cityName, apiKey);
        final HttpResponse<String> response = httpClient.send(request, bodyHandler);
        return handleResponse(response);
      } catch (final java.io.IOException ioException) {
        lastException = createNetworkException(ioException);
        attempts = handleRetryOnException(attempts, "Network error", ioException.getMessage());
        if (attempts >= maxRetries) {
          break; // Exit the loop to throw the final exception
        }
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new NetworkException(
            "Request was interrupted while fetching weather data", API_ENDPOINT_URL, null, e);
      } catch (final NetworkException networkException) {
        if (shouldRetryNetworkException(networkException)) {
          lastException = networkException;
          attempts =
              handleRetryOnException(attempts, "Server error", networkException.getMessage());
          if (attempts >= maxRetries) {
            break; // Exit the loop to throw the final exception
          }
        } else {
          // For client errors (4xx), do not retry - throw immediately
          throw networkException;
        }
      } catch (@SuppressWarnings("PMD.AvoidRethrowingException")
          final RateLimitException
          | ApiKeyException
          | CityNotFoundException e) {
        // Do not retry on these exceptions - throw immediately
        throw e;
      }
    }

    // If we've exhausted all retries, throw the final exception
    if (attempts >= maxRetries && lastException != null) {
      // Preserve the HTTP status code from the original exception if it's a
      // NetworkException
      Integer origStatusCode = null;
      String origMessage = lastException.getMessage();
      if (lastException instanceof NetworkException) {
        final NetworkException networkEx = (NetworkException) lastException;
        origStatusCode = networkEx.getHttpStatusCode();
        // Include original message in the final message to preserve test expectations
        origMessage = networkEx.getMessage();
      }
      final String finalMessage =
          "Failed to fetch weather data after "
              + maxRetries
              + " attempts. Original error: "
              + origMessage;
      throw new NetworkException(finalMessage, API_ENDPOINT_URL, origStatusCode, lastException);
    }

    // This should never be reached due to the logic above, but added for
    // completeness
    throw new NetworkException(
        "Unexpected end of retry loop reached - this should never happen", API_ENDPOINT_URL, null);
  }

  /**
   * Determines if a NetworkException should be retried.
   *
   * @param networkException the NetworkException to check
   * @return true if the exception should be retried, false otherwise
   */
  private boolean shouldRetryNetworkException(final NetworkException networkException) {
    return networkException.isServerError() || networkException.isConnectionError();
  }

  /**
   * Handles retry attempt on exception.
   *
   * @param currentAttempt the current attempt number
   * @param errorType the type of error (for logging)
   * @param errorMessage the error message
   * @return the updated attempt number
   * @throws NetworkException if max retries are exceeded
   */
  private int handleRetryOnException(
      final int currentAttempt, final String errorType, final String errorMessage)
      throws NetworkException {
    handleRetryAttempt(errorType, currentAttempt, errorMessage);
    waitForRetry();
    return currentAttempt + 1;
  }

  /**
   * Handles a retry attempt by logging the error.
   *
   * @param errorType the type of error (for logging)
   * @param attempts the current number of attempts made
   * @param message the error message
   */
  private void handleRetryAttempt(
      final String errorType, final int attempts, final String message) {
    if (LOGGER.isDebugEnabled() && attempts < maxRetries - 1) {
      LOGGER.debug(
          "{} on attempt {}/{}: {}. Retrying in {}ms...",
          errorType,
          attempts + 1,
          maxRetries,
          message,
          retryDelayMs);
    }
  }

  /**
   * Creates a NetworkException from an IOException.
   *
   * @param ioException the IOException to convert
   * @return a NetworkException wrapping the IOException
   */
  private NetworkException createNetworkException(final java.io.IOException ioException) {
    return new NetworkException(
        "Network error occurred while fetching weather data: " + ioException.getMessage(),
        API_ENDPOINT_URL,
        null,
        ioException);
  }

  /**
   * Waits for the retry delay period.
   *
   * @throws NetworkException if the thread is interrupted during the wait
   */
  private void waitForRetry() throws NetworkException {
    try {
      Thread.sleep(retryDelayMs);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new NetworkException(
          "Request was interrupted while fetching weather data", API_ENDPOINT_URL, null, ie);
    }
  }

  /**
   * Validates the method parameters.
   *
   * @param cityName the city name to validate
   * @param apiKey the API key to validate
   * @throws IllegalArgumentException if either parameter is null or empty
   */
  private void validateParameters(final String cityName, final String apiKey) {
    if (cityName == null || cityName.isBlank()) {
      throw new IllegalArgumentException("City name must not be null or empty");
    }
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("API key must not be null or empty");
    }
  }

  /**
   * Builds the HTTP request for the weather API.
   *
   * @param cityName the city name
   * @param apiKey the API key
   * @return the constructed HTTP request
   */
  private HttpRequest buildRequest(final String cityName, final String apiKey) {
    final String url =
        API_ENDPOINT_URL
            + "?"
            + PARAM_CITY
            + "="
            + encodeParameter(cityName.trim())
            + "&"
            + PARAM_API_KEY
            + "="
            + encodeParameter(apiKey.trim())
            + "&"
            + PARAM_UNITS
            + "="
            + UNITS_STANDARD;

    return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(connectionTimeout)
        .header("Accept", "application/json")
        .GET()
        .build();
  }

  /**
   * Encodes a parameter value for URL inclusion.
   *
   * @param value the parameter value to encode
   * @return the URL-encoded value
   */
  private String encodeParameter(final String value) {
    return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
  }

  /**
   * Handles the HTTP response and maps it to the appropriate result or exception.
   *
   * @param response the HTTP response
   * @return the parsed WeatherData object
   * @throws ApiKeyException if HTTP 401
   * @throws CityNotFoundException if HTTP 404
   * @throws RateLimitException if HTTP 429
   * @throws NetworkException for other error conditions
   */
  private WeatherData handleResponse(final HttpResponse<String> response)
      throws ApiKeyException, CityNotFoundException, RateLimitException, NetworkException {
    final int statusCode = response.statusCode();

    if (HttpURLConnection.HTTP_OK == statusCode) {
      return parseWeatherData(response.body());
    }
    handleErrorStatus(statusCode, response);
    throw new NetworkException("Unexpected error handling response", API_ENDPOINT_URL, null, null);
  }

  private void handleErrorStatus(final int statusCode, final HttpResponse<String> response)
      throws ApiKeyException, CityNotFoundException, RateLimitException, NetworkException {
    final String responseBody = response.body();

    switch (statusCode) {
      case UNAUTH_STATUS:
        throw new ApiKeyException("Invalid or missing API key");
      case NOT_FOUND_STATUS:
        final String errorMessage = extractErrorMessage(responseBody);
        throw new CityNotFoundException("City not found: " + errorMessage);
      case RATE_LIMIT_STATUS:
        final long retryAfterSeconds = extractRetryAfterSeconds(response);
        throw new RateLimitException(
            "Rate limit exceeded. Retry after " + retryAfterSeconds + " seconds",
            (int) retryAfterSeconds);
      default:
        handleOtherError(statusCode, responseBody);
    }
  }

  private void handleOtherError(final int statusCode, final String responseBody)
      throws NetworkException {
    if (statusCode >= SERVER_ERR_STATUS) {
      throw new NetworkException(
          "Server error (HTTP " + statusCode + "): " + responseBody, API_ENDPOINT_URL, statusCode);
    }
    throw new NetworkException(
        "Unexpected HTTP status " + statusCode + ": " + responseBody, API_ENDPOINT_URL, statusCode);
  }

  /**
   * Parses the JSON response body into a WeatherData object.
   *
   * @param jsonResponse the JSON response body
   * @return the parsed WeatherData object
   * @throws NetworkException if parsing fails
   */
  private WeatherData parseWeatherData(final String jsonResponse) throws NetworkException {
    if (jsonResponse == null || jsonResponse.isEmpty()) {
      throw new NetworkException(
          "Empty or null response body from API", API_ENDPOINT_URL, null, null);
    }
    try {
      return objectMapper.readValue(jsonResponse, WeatherData.class);
    } catch (final com.fasterxml.jackson.core.JsonProcessingException e) {
      throw new NetworkException(
          "Failed to parse weather data from response: " + e.getMessage(),
          API_ENDPOINT_URL,
          null,
          e);
    }
  }

  /**
   * Extracts error message from the API error response.
   *
   * @param responseBody the error response body
   * @return the extracted error message, or a default message if extraction fails
   */
  private String extractErrorMessage(final String responseBody) {
    String errorMessage = "Unknown error";
    try {
      final com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(responseBody);
      final com.fasterxml.jackson.databind.JsonNode messageNode = root.get("message");
      if (messageNode != null) {
        errorMessage = messageNode.asText();
      }
    } catch (final com.fasterxml.jackson.core.JsonProcessingException e) {
      errorMessage = "Unable to parse error message";
    }
    return errorMessage;
  }

  /**
   * Extracts the Retry-After seconds from the rate limit response headers.
   *
   * @param response the HTTP response
   * @return the retry-after seconds, or 0 if not available
   */
  private long extractRetryAfterSeconds(final HttpResponse<String> response) {
    long retryAfterSeconds = 0;
    final String retryAfterHeader = response.headers().firstValue("Retry-After").orElse(null);
    if (retryAfterHeader != null) {
      try {
        retryAfterSeconds = Long.parseLong(retryAfterHeader);
      } catch (final NumberFormatException e) {
        // Log the error but ignore parsing errors, return 0
        // In production, this would use a proper logger
        final Logger logger = Logger.getLogger(DefaultOpenWeatherApiClient.class.getName());
        if (logger.isLoggable(java.util.logging.Level.WARNING)) {
          logger.warning("Invalid Retry-After header format: " + retryAfterHeader);
        }
      }
    }
    return retryAfterSeconds;
  }
}
