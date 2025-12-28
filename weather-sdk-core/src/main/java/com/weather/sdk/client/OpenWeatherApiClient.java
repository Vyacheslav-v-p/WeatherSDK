/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.client;

import com.weather.sdk.exception.ApiKeyException;
import com.weather.sdk.exception.CityNotFoundException;
import com.weather.sdk.exception.NetworkException;
import com.weather.sdk.exception.RateLimitException;
import com.weather.sdk.model.WeatherData;

/**
 * Contract for OpenWeatherAPI HTTP integration providing weather data retrieval capabilities.
 *
 * <p>This interface defines the contract for communicating with the OpenWeatherAPI service to fetch
 * current weather data for specified cities. It handles all HTTP communication details, response
 * parsing, error handling, and provides thread-safe operations.
 *
 * <h2>API Integration Details</h2>
 *
 * <ul>
 *   <li><strong>Base URL</strong>: https://api.openweathermap.org/data/2.5/weather
 *   <li><strong>Method</strong>: HTTP GET
 *   <li><strong>Format</strong>: JSON response
 *   <li><strong>Authentication</strong>: API key via query parameter
 *   <li><strong>Units</strong>: Metric (Celsius, meters/second)
 * </ul>
 *
 * <h2>Request Format</h2>
 *
 * <p>Requests are constructed with the following parameters:
 *
 * <ul>
 *   <li><code>q</code>: City name (required)
 *   <li><code>appid</code>: API key (required)
 *   <li><code>units</code>: Units system (always "metric")
 * </ul>
 *
 * <p>Example URL:
 *
 * <pre>
 * https://api.openweathermap.org/data/2.5/weather?q=London&amp;appid=your-api-key&amp;units=metric
 * </pre>
 *
 * <h2>Response Mapping</h2>
 *
 * <p>The API returns weather data in JSON format which is mapped to {@link WeatherData}:
 *
 * <ul>
 *   <li><code>weather[0].main</code> → {@link WeatherData#getWeather() getWeather().getMain()}
 *   <li><code>weather[0].description</code> → {@link WeatherData#getWeather()
 *       getWeather().getDescription()}
 *   <li><code>main.temp</code> → {@link WeatherData#getTemperature() getTemperature().getTemp()}
 *   <li><code>main.feels_like</code> → {@link WeatherData#getTemperature()
 *       getTemperature().getFeelsLike()}
 *   <li><code>wind.speed</code> → {@link WeatherData#getWind() getWind().getSpeed()}
 *   <li><code>wind.deg</code> → {@link WeatherData#getWind() getWind().getDirection()}
 *   <li><code>sys.sunrise</code> → {@link WeatherData#getSystemInfo() getSystemInfo().getSunrise()}
 *   <li><code>sys.sunset</code> → {@link WeatherData#getSystemInfo() getSystemInfo().getSunset()}
 * </ul>
 *
 * <h2>Error Handling</h2>
 *
 * <p>The interface provides comprehensive error handling for various failure scenarios:
 *
 * <ul>
 *   <li><strong>200 OK</strong>: Success - Parse JSON to WeatherData
 *   <li><strong>401 Unauthorized</strong>: Throw {@link ApiKeyException}
 *   <li><strong>404 Not Found</strong>: Throw {@link CityNotFoundException}
 *   <li><strong>429 Too Many Requests</strong>: Throw {@link RateLimitException} (includes
 *       Retry-After header)
 *   <li><strong>5xx Server Error</strong>: Throw {@link NetworkException}
 *   <li><strong>Network Failure</strong>: Throw {@link NetworkException}
 * </ul>
 *
 * <h2>Error Response Format</h2>
 *
 * <p>OpenWeatherAPI returns error responses in JSON format:
 *
 * <pre>
 * {
 *   "cod": "404",
 *   "message": "city not found"
 * }
 * </pre>
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This interface and its implementations are designed to be thread-safe:
 *
 * <ul>
 *   <li>Can be called concurrently from multiple threads
 *   <li>No external synchronization required
 *   <li>Each call is independent and thread-local
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * // Create client instance
 * OpenWeatherApiClient client = new DefaultOpenWeatherApiClient();
 *
 * // Fetch weather data for a city
 * try {
 *   WeatherData weather = client.fetchWeather("London", "your-api-key");
 *   System.out.println("Temperature: " + weather.getTemperature().getTempCelsius() + "°C");
 *   System.out.println("Conditions: " + weather.getWeather().getDescription());
 * } catch (ApiKeyException e) {
 *   System.err.println("Invalid API key: " + e.getMessage());
 * } catch (CityNotFoundException e) {
 *   System.err.println("City not found: " + e.getMessage());
 * } catch (RateLimitException e) {
 *   System.err.println("Rate limit exceeded. Retry after: " + e.getRetryAfterSeconds() + " seconds");
 * } catch (NetworkException e) {
 *   System.err.println("Network error: " + e.getMessage());
 * }
 * </pre>
 *
 * @see WeatherData
 * @see NetworkException
 * @see RateLimitException
 * @see ApiKeyException
 * @see CityNotFoundException
 * @since 1.0.0
 */
public interface OpenWeatherApiClient {

  /** Base URL for the OpenWeatherAPI current weather endpoint. */
  String API_ENDPOINT_URL = "https://api.openweathermap.org/data/2.5/weather";

  /** Query parameter name for city name. */
  String PARAM_CITY = "q";

  /** Query parameter name for API key. */
  String PARAM_API_KEY = "appid";

  /** Query parameter name for units system. */
  String PARAM_UNITS = "units";

  /** Value for metric units system. */
  String UNITS_METRIC = "metric";

  /** Value for standard units system (returns temperatures in Kelvin). */
  String UNITS_STANDARD = "standard";

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
   */
  WeatherData fetchWeather(String cityName, String apiKey)
      throws ApiKeyException, CityNotFoundException, RateLimitException, NetworkException;
}
