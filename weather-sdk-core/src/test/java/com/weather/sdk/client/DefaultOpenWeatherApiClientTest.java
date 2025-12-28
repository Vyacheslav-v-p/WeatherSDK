/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.weather.sdk.exception.ApiKeyException;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
import com.weather.sdk.exception.RateLimitException;
import com.weather.sdk.model.WeatherData;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Comprehensive unit tests for DefaultOpenWeatherApiClient.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>HTTP request construction and validation
 *   <li>JSON response parsing and error handling
 *   <li>HTTP status code handling (200, 401, 404, 429, 5xx)
 *   <li>Parameter validation
 *   <li>Exception mapping
 *   <li>Timeout configuration
 * </ul>
 */
@DisplayName("DefaultOpenWeatherApiClient Tests")
class DefaultOpenWeatherApiClientTest {

  /** Mock HTTP client for testing. */
  private HttpClient mockHttpClient;

  /** The client instance under test. */
  private DefaultOpenWeatherApiClient client;

  /** Sample valid weather JSON response. */
  private static final String VALID_WEATHER_JSON =
      """
      {
        "weather": [
          {
            "main": "Clear",
            "description": "clear sky"
          }
        ],
        "main": {
          "temp": 294.15,
          "feels_like": 293.15
        },
        "visibility": 10000,
        "wind": {
          "speed": 3.5,
          "deg": 180
        },
        "dt": 1675751262,
        "sys": {
          "sunrise": 1675751262,
          "sunset": 1675787560
        },
        "timezone": 3600,
        "name": "London"
      }
      """;

  /** Sample API key for testing. */
  private static final String TEST_API_KEY = "test-api-key-12345";

  /** Sample city name for testing. */
  private static final String TEST_CITY = "London";

  @BeforeEach
  void setUp() {
    mockHttpClient = mock(HttpClient.class);
    client = new DefaultOpenWeatherApiClient(mockHttpClient);
  }

  // ==================== Constructor Tests ====================

  @Test
  @DisplayName("Constructor should reject null HTTP client")
  void constructorShouldRejectNullHttpClient() {
    assertThrows(NullPointerException.class, () -> new DefaultOpenWeatherApiClient(null));
  }

  @Test
  @DisplayName("Constructor should reject non-positive connection timeout")
  void constructorShouldRejectNonPositiveConnectionTimeout() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DefaultOpenWeatherApiClient(mockHttpClient, 0, 30));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DefaultOpenWeatherApiClient(mockHttpClient, -5, 30));
  }

  @Test
  @DisplayName("Constructor should reject non-positive read timeout")
  void constructorShouldRejectNonPositiveReadTimeout() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DefaultOpenWeatherApiClient(mockHttpClient, 10, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DefaultOpenWeatherApiClient(mockHttpClient, 10, -5));
  }

  // ==================== Parameter Validation Tests ====================

  @Test
  @DisplayName("fetchWeather should reject null city name")
  void fetchWeatherShouldRejectNullCityName() {
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather(null, TEST_API_KEY));
  }

  @Test
  @DisplayName("fetchWeather should reject empty city name")
  void fetchWeatherShouldRejectEmptyCityName() {
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather("", TEST_API_KEY));
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather("   ", TEST_API_KEY));
  }

  @Test
  @DisplayName("fetchWeather should reject null API key")
  void fetchWeatherShouldRejectNullApiKey() {
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather(TEST_CITY, null));
  }

  @Test
  @DisplayName("fetchWeather should reject empty API key")
  void fetchWeatherShouldRejectEmptyApiKey() {
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather(TEST_CITY, ""));
    assertThrows(IllegalArgumentException.class, () -> client.fetchWeather(TEST_CITY, "   "));
  }

  // ==================== HTTP Request Construction Tests ====================

  @Test
  @DisplayName("fetchWeather should construct correct HTTP request")
  void fetchWeatherShouldConstructCorrectHttpRequest() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

    final HttpRequest capturedRequest = requestCaptor.getValue();
    assertEquals("GET", capturedRequest.method());

    final URI uri = capturedRequest.uri();
    assertTrue(uri.toString().contains("q=London"));
    assertTrue(uri.toString().contains("appid=test-api-key-12345"));
    assertTrue(uri.toString().contains("units=standard"));

    assertTrue(capturedRequest.headers().firstValue("Accept").isPresent());
    assertEquals("application/json", capturedRequest.headers().firstValue("Accept").get());
  }

  @Test
  @DisplayName("fetchWeather should trim city name and API key")
  void fetchWeatherShouldTrimParameters() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    client.fetchWeather("  London  ", "  test-api-key-12345  ");

    // Assert
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

    final HttpRequest capturedRequest = requestCaptor.getValue();
    final URI uri = capturedRequest.uri();
    assertTrue(uri.toString().contains("q=London"));
    assertTrue(uri.toString().contains("appid=test-api-key-12345"));
  }

  // ==================== JSON Response Parsing Tests ====================

  @Test
  @DisplayName("fetchWeather should parse valid JSON response")
  void fetchWeatherShouldParseValidJsonResponse() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    final WeatherData result = client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert
    assertEquals("London", result.getName());
    assertEquals("Clear", result.getWeather().getMain());
    assertEquals("clear sky", result.getWeather().getDescription());
    assertEquals(294.15, result.getTemperature().getTemp(), 0.01);
    assertEquals(293.15, result.getTemperature().getFeelsLike(), 0.01);
    assertEquals(3.5, result.getWind().getSpeed(), 0.01);
    assertEquals(180, result.getWind().getDirection());
    assertEquals(10000, result.getVisibility());
  }

  @Test
  @DisplayName("fetchWeather should throw NetworkException for invalid JSON")
  void fetchWeatherShouldThrowNetworkExceptionForInvalidJson() throws Exception {
    // Arrange
    final String invalidJson = "{ invalid json }";
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(invalidJson);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
  }

  // ==================== HTTP Status Code Handling Tests ====================

  @Test
  @DisplayName("fetchWeather should throw ApiKeyException for HTTP 401")
  void fetchWeatherShouldThrowApiKeyExceptionForHttp401() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(401);
    when(mockResponse.body()).thenReturn("{\"cod\":401, \"message\": \"Invalid API key\"}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final ApiKeyException exception =
        assertThrows(ApiKeyException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Invalid or missing API key"));
  }

  @Test
  @DisplayName("fetchWeather should throw CityNotFoundException for HTTP 404")
  void fetchWeatherShouldThrowCityNotFoundExceptionForHttp404() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.body()).thenReturn("{\"cod\":404, \"message\": \"city not found\"}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final CityNotFoundException exception =
        assertThrows(
            CityNotFoundException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("City not found:"));
    assertTrue(exception.getMessage().contains("city not found"));
  }

  @Test
  @DisplayName("fetchWeather should extract error message from 404 response")
  void fetchWeatherShouldExtractErrorMessageFrom404Response() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.body()).thenReturn("{\"cod\":404, \"message\": \"Invalid city name\"}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final CityNotFoundException exception =
        assertThrows(
            CityNotFoundException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Invalid city name"));
  }

  @Test
  @DisplayName("fetchWeather should throw RateLimitException for HTTP 429 with Retry-After")
  void fetchWeatherShouldThrowRateLimitExceptionForHttp429WithRetryAfter() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(429);
    when(mockResponse.body()).thenReturn("{\"cod\":429, \"message\": \"Rate limit exceeded\"}");
    final HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.headers()).thenReturn(mockHeaders);
    when(mockHeaders.firstValue("Retry-After")).thenReturn(java.util.Optional.of("60"));
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final RateLimitException exception =
        assertThrows(RateLimitException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    assertTrue(exception.getMessage().contains("60"));
    assertEquals(Integer.valueOf(60), exception.getRetryAfterSeconds());
  }

  @Test
  @DisplayName("fetchWeather should handle RateLimitException without Retry-After header")
  void fetchWeatherShouldHandleRateLimitExceptionWithoutRetryAfterHeader() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(429);
    when(mockResponse.body()).thenReturn("{\"cod\":429, \"message\": \"Rate limit exceeded\"}");
    final HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.headers()).thenReturn(mockHeaders);
    when(mockHeaders.firstValue("Retry-After")).thenReturn(java.util.Optional.empty());
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final RateLimitException exception =
        assertThrows(RateLimitException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    assertEquals(Integer.valueOf(0), exception.getRetryAfterSeconds());
  }

  @Test
  @DisplayName("fetchWeather should throw NetworkException for HTTP 5xx")
  void fetchWeatherShouldThrowNetworkExceptionForHttp5xx() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(500);
    when(mockResponse.body()).thenReturn("Internal Server Error");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Server error (HTTP 500)"));
    assertTrue(exception.isServerError());
  }

  @Test
  @DisplayName("fetchWeather should throw NetworkException for unexpected status codes")
  void fetchWeatherShouldThrowNetworkExceptionForUnexpectedStatusCodes() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(418); // I'm a teapot
    when(mockResponse.body()).thenReturn("I'm a teapot");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(exception.getMessage().contains("Unexpected HTTP status 418"));
  }

  // ==================== Network Exception Handling Tests ====================

  @Test
  @DisplayName("fetchWeather should throw NetworkException for IOException")
  void fetchWeatherShouldThrowNetworkExceptionForIOException() throws Exception {
    // Arrange
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new java.io.IOException("Connection failed"));

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(
        exception.getMessage().contains("Network error occurred while fetching weather data"));
    assertTrue(exception.getMessage().contains("Connection failed"));
  }

  @Test
  @DisplayName("fetchWeather should handle InterruptedException and restore interrupt status")
  void fetchWeatherShouldHandleInterruptedExceptionAndRestoreInterruptStatus() throws Exception {
    // Arrange
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new InterruptedException("Request interrupted"));

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    assertTrue(
        exception.getMessage().contains("Request was interrupted while fetching weather data"));
    assertTrue(Thread.currentThread().isInterrupted(), "Interrupt status should be restored");
  }

  // ==================== Timeout Configuration Tests ====================
  @Test
  @DisplayName("Client should use custom timeout values")
  void clientShouldUseCustomTimeoutValues() {
    // Arrange & Act
    final DefaultOpenWeatherApiClient customClient =
        new DefaultOpenWeatherApiClient(mockHttpClient, 15, 45);

    // Assert - just verify it constructs without throwing exception
    assertTrue(customClient != null);
  }

  // ==================== URL Encoding Tests ====================

  @Test
  @DisplayName("fetchWeather should handle special characters in city name")
  void fetchWeatherShouldHandleSpecialCharactersInCityName() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    client.fetchWeather("New York", TEST_API_KEY);

    // Assert
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

    final HttpRequest capturedRequest = requestCaptor.getValue();
    final URI uri = capturedRequest.uri();
    assertTrue(uri.toString().contains("q=New+York") || uri.toString().contains("q=New%20York"));
  }

  // ==================== Edge Cases and Boundary Conditions ====================

  @Test
  @DisplayName("fetchWeather should handle null response body")
  void fetchWeatherShouldHandleNullResponseBody() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(null);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
  }

  @Test
  @DisplayName("fetchWeather should handle empty response body")
  void fetchWeatherShouldHandleEmptyResponseBody() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
  }

  // ==================== Retry Mechanism Tests ====================

  @Test
  @DisplayName("fetchWeather should retry on IOException (network failure)")
  void fetchWeatherShouldRetryOnIOException() throws Exception {
    // Arrange
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new java.io.IOException("Connection failed"))
        .thenThrow(new java.io.IOException("Connection failed"))
        .thenReturn(createMockSuccessResponse());

    // Act
    final WeatherData result = client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert
    verify(mockHttpClient, times(3))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertEquals("London", result.getName());
  }

  @Test
  @DisplayName("fetchWeather should retry on server error (5xx) NetworkException")
  void fetchWeatherShouldRetryOnServerError() throws Exception {
    // Arrange
    final HttpResponse<String> mockServerErrorResponse = mock(HttpResponse.class);
    when(mockServerErrorResponse.statusCode()).thenReturn(500);
    when(mockServerErrorResponse.body()).thenReturn("Internal Server Error");

    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockServerErrorResponse) // First attempt - server error
        .thenReturn(mockServerErrorResponse) // Second attempt - server error
        .thenReturn(createMockSuccessResponse()); // Third attempt - success

    // Act
    final WeatherData result = client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert
    verify(mockHttpClient, times(3))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertEquals("London", result.getName());
  }

  @Test
  @DisplayName("fetchWeather should not retry on client error (4xx) NetworkException")
  void fetchWeatherShouldNotRetryOnClientError() throws Exception {
    // Arrange
    final HttpResponse<String> mockClientErrorResponse = mock(HttpResponse.class);
    when(mockClientErrorResponse.statusCode()).thenReturn(400);
    when(mockClientErrorResponse.body()).thenReturn("Bad Request");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockClientErrorResponse);

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(1))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("400"));
    assertTrue(exception.isClientError());
  }

  @Test
  @DisplayName("fetchWeather should not retry on ApiKeyException")
  void fetchWeatherShouldNotRetryOnApiKeyException() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(401);
    when(mockResponse.body()).thenReturn("{\"cod\":401, \"message\": \"Invalid API key\"}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final ApiKeyException exception =
        assertThrows(ApiKeyException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(1))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("Invalid or missing API key"));
  }

  @Test
  @DisplayName("fetchWeather should not retry on CityNotFoundException")
  void fetchWeatherShouldNotRetryOnCityNotFoundException() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.body()).thenReturn("{\"cod\":404, \"message\": \"city not found\"}");
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final CityNotFoundException exception =
        assertThrows(
            CityNotFoundException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(1))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("City not found:"));
  }

  @Test
  @DisplayName("fetchWeather should not retry on RateLimitException")
  void fetchWeatherShouldNotRetryOnRateLimitException() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(429);
    when(mockResponse.body()).thenReturn("{\"cod\":429, \"message\": \"Rate limit exceeded\"}");
    final HttpHeaders mockHeaders = mock(HttpHeaders.class);
    when(mockResponse.headers()).thenReturn(mockHeaders);
    when(mockHeaders.firstValue("Retry-After")).thenReturn(java.util.Optional.of("60"));
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act & Assert
    final RateLimitException exception =
        assertThrows(RateLimitException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(1))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("Rate limit exceeded"));
  }

  @Test
  @DisplayName("fetchWeather should fail after maximum retry attempts on IOException")
  void fetchWeatherShouldFailAfterMaxRetriesOnIOException() throws Exception {
    // Arrange
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenThrow(new java.io.IOException("Connection failed"))
        .thenThrow(new java.io.IOException("Connection failed"))
        .thenThrow(new java.io.IOException("Connection failed"));

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(3))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("Failed to fetch weather data after 3 attempts"));
  }

  @Test
  @DisplayName("fetchWeather should fail after maximum retry attempts on server error")
  void fetchWeatherShouldFailAfterMaxRetriesOnServerError() throws Exception {
    // Arrange
    final HttpResponse<String> mockServerErrorResponse = mock(HttpResponse.class);
    when(mockServerErrorResponse.statusCode()).thenReturn(500);
    when(mockServerErrorResponse.body()).thenReturn("Internal Server Error");

    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockServerErrorResponse)
        .thenReturn(mockServerErrorResponse)
        .thenReturn(mockServerErrorResponse);

    // Act & Assert
    final NetworkException exception =
        assertThrows(NetworkException.class, () -> client.fetchWeather(TEST_CITY, TEST_API_KEY));
    verify(mockHttpClient, times(3))
        .send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    assertTrue(exception.getMessage().contains("Failed to fetch weather data after 3 attempts"));
  }

  /**
   * Helper method to create a mock successful response.
   *
   * @return a mock HttpResponse with successful status and valid weather data
   */
  private HttpResponse<String> createMockSuccessResponse() throws Exception {
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    return mockResponse;
  }

  // ==================== Temperature Unit Conversion Prevention Tests
  // ====================

  /**
   * CRITICAL TEST: Prevents temperature unit conversion bug where Celsius values were treated as
   * Kelvin.
   *
   * <p>This test ensures that:
   *
   * <ul>
   *   <li>API client requests Kelvin (units=standard) from OpenWeatherAPI
   *   <li>Temperature values are in Kelvin range (273.15+ for realistic temperatures)
   *   <li>Temperature conversions work correctly
   * </ul>
   *
   * <p>This prevents the bug where API returned Celsius values (e.g., 8.0°C) but Temperature class
   * expected Kelvin, resulting in impossible values like -265.15°C.
   */
  @Test
  @DisplayName(
      "CRITICAL: API client must request Kelvin (units=standard) to prevent temperature conversion bug")
  void fetchWeatherShouldRequestKelvinUnitsToPreventConversionBug() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert - Verify that the API request uses units=standard (Kelvin)
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

    final HttpRequest capturedRequest = requestCaptor.getValue();
    final URI uri = capturedRequest.uri();
    final String requestUrl = uri.toString();

    // CRITICAL ASSERTION: Must use units=standard to get Kelvin values
    assertTrue(
        requestUrl.contains("units=standard"),
        "API client must request units=standard (Kelvin) to prevent temperature conversion bug. "
            + "Using units=metric (Celsius) would cause Temperature class to treat Celsius values as Kelvin, "
            + "resulting in impossible temperatures like -265.15°C");

    // Verify other required parameters are present
    assertTrue(requestUrl.contains("q=London"), "City parameter missing");
    assertTrue(requestUrl.contains("appid=test-api-key-12345"), "API key parameter missing");
  }

  /**
   * Test to verify that temperature values in API response are in Kelvin range.
   *
   * <p>This ensures that even if the units parameter is changed in the future, the temperature
   * values will be caught early if they're not in the expected Kelvin range.
   */
  @Test
  @DisplayName("Temperature values should be in Kelvin range to prevent conversion errors")
  void temperatureValuesShouldBeInKelvinRange() throws Exception {
    // Arrange
    final String kelvinWeatherJson =
        """
        {
          "weather": [{"main": "Clear", "description": "clear sky"}],
          "main": {
            "temp": 294.15,
            "feels_like": 293.15
          },
          "visibility": 10000,
          "wind": {"speed": 3.5, "deg": 180},
          "dt": 1675751262,
          "sys": {"sunrise": 1675751262, "sunset": 1675787560},
          "timezone": 3600,
          "name": "London"
        }
        """;

    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(kelvinWeatherJson);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    final WeatherData result = client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert - Temperature should be in Kelvin range (273.15+ for realistic
    // temperatures)
    final double temp = result.getTemperature().getTemp();
    final double feelsLike = result.getTemperature().getFeelsLike();

    assertTrue(
        temp >= 273.15,
        "Temperature should be in Kelvin range (273.15+). Got: "
            + temp
            + "K. "
            + "Lower values would indicate Celsius values being treated as Kelvin, "
            + "causing impossible negative temperatures after conversion.");

    assertTrue(
        feelsLike >= 273.15,
        "Feels-like temperature should be in Kelvin range (273.15+). Got: " + feelsLike + "K");

    // Verify conversion to Celsius produces realistic values
    final double tempCelsius = result.getTemperature().getTempCelsius();
    assertTrue(
        tempCelsius >= -50 && tempCelsius <= 50,
        "Converted Celsius temperature should be in realistic range (-50°C to 50°C). Got: "
            + tempCelsius
            + "°C. "
            + "Values outside this range indicate unit conversion errors.");
  }

  /**
   * Test to verify that realistic Kelvin temperatures are properly handled.
   *
   * <p>This test ensures that when realistic Kelvin temperatures are provided, they are converted
   * to realistic Celsius values.
   */
  @Test
  @DisplayName("Should handle realistic Kelvin temperatures correctly")
  void shouldHandleRealisticKelvinTemperatures() throws Exception {
    // Arrange - Use a realistic Kelvin temperature (around room temperature)
    final String realisticKelvinJson =
        """
        {
          "weather": [{"main": "Clear", "description": "clear sky"}],
          "main": {
            "temp": 294.15,
            "feels_like": 293.15
          },
          "visibility": 10000,
          "wind": {"speed": 3.5, "deg": 180},
          "dt": 1675751262,
          "sys": {"sunrise": 1675751262, "sunset": 1675787560},
          "timezone": 3600,
          "name": "London"
        }
        """;

    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(realisticKelvinJson);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    final WeatherData result = client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert - Verify realistic Kelvin temperatures convert to realistic Celsius
    final double temp = result.getTemperature().getTemp();
    final double tempCelsius = result.getTemperature().getTempCelsius();

    // Temperature should be in realistic Kelvin range
    assertTrue(
        temp >= 273.15 && temp <= 373.15,
        "Temperature should be in realistic Kelvin range (273.15K to 373.15K). Got: " + temp + "K");

    // Converted Celsius should also be realistic
    assertTrue(
        tempCelsius >= 0 && tempCelsius <= 100,
        "Converted Celsius temperature should be in realistic range (0°C to 100°C). Got: "
            + tempCelsius
            + "°C");

    // Specifically, 294.15K should convert to about 21°C
    assertEquals(21.0, tempCelsius, 1.0, "294.15K should convert to approximately 21°C");
  }

  /**
   * Test to verify that the UNITS_STANDARD constant is being used correctly.
   *
   * <p>This ensures that if someone accidentally changes the units parameter, the tests will catch
   * it.
   */
  @Test
  @DisplayName("Should use UNITS_STANDARD constant for API requests")
  void shouldUseUnitsStandardConstant() throws Exception {
    // Arrange
    final HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn(VALID_WEATHER_JSON);
    when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
        .thenReturn(mockResponse);

    // Act
    client.fetchWeather(TEST_CITY, TEST_API_KEY);

    // Assert - Verify the exact constant is being used
    final ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);
    verify(mockHttpClient).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));

    final HttpRequest capturedRequest = requestCaptor.getValue();
    final URI uri = capturedRequest.uri();
    final String requestUrl = uri.toString();

    // Verify units=standard is present (not units=metric or any other value)
    assertTrue(
        requestUrl.contains("units=standard"),
        "API request must use units=standard (Kelvin), not units=metric (Celsius) or other values. "
            + "Current request: "
            + requestUrl);

    assertFalse(
        requestUrl.contains("units=metric"),
        "CRITICAL: units=metric (Celsius) detected in API request! This will cause temperature "
            + "conversion bugs where Celsius values are treated as Kelvin. Use units=standard instead.");
  }
}
