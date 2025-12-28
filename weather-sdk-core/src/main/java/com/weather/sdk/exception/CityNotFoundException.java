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
 * Exception thrown when a city cannot be found by the OpenWeatherAPI.
 *
 * <p>This exception is thrown when:
 *
 * <ul>
 *   <li>The specified city name doesn't exist in the OpenWeatherAPI database
 *   <li>The city name is misspelled or malformed
 *   <li>The city name is too ambiguous (multiple matches found)
 *   <li>The city name contains invalid characters
 *   <li>The city is too small or not covered by OpenWeatherAPI
 * </ul>
 *
 * <h2>Error Code</h2>
 *
 * <p>Error code: {@value #CITY_NOT_FOUND}
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li>Misspelled city name (e.g., "Pariss" instead of "Paris")
 *   <li>Ambiguous city names without country specification
 *   <li>Very small towns or villages not in the database
 *   <li>Special characters or numbers in city names
 *   <li>Historical or deprecated city names
 * </ul>
 *
 * <h2>Recovery Actions</h2>
 *
 * <ul>
 *   <li>Check spelling and try alternative spellings
 *   <li>Include country code with city name (e.g., "Paris,FR")
 *   <li>Try nearby major cities as alternatives
 *   <li>Use coordinates (latitude/longitude) instead of city names
 *   <li>Implement a city suggestion system using fuzzy matching
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Handling City Not Found</h3>
 *
 * <pre>
 * try {
 *     WeatherData weather = sdk.getWeather("Atlantis");
 * } catch (CityNotFoundException e) {
 *     logger.warn("City not found: {}", e.getMessage());
 *
 *     // Suggest alternatives
 *     List{@code <String>} suggestions = getCitySuggestions("Atlantis");
 *     if (!suggestions.isEmpty()) {
 *         logger.info("Did you mean: {}", String.join(", ", suggestions));
 *     }
 * }
 * </pre>
 *
 * <h3>City Name Normalization</h3>
 *
 * <pre>
 * public Optional{@code <WeatherData>} getWeatherForNormalizedCity(String cityName) {
 *     String normalized = normalizeCityName(cityName);
 *
 *   try {
 *     return Optional.of(sdk.getWeather(normalized));
 *   } catch (CityNotFoundException e) {
 *     // Try with country code
 *     String withCountry = normalizeCityName(cityName + ",US");
 *     try {
 *       return Optional.of(sdk.getWeather(withCountry));
 *     } catch (CityNotFoundException e2) {
 *       return Optional.empty();
 *     }
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK#getWeather(String)
 * @see com.weather.sdk.exception.WeatherSDKException
 */
public class CityNotFoundException extends WeatherSDKException {

  /** Serial version UID for serialization compatibility. */
  private static final long serialVersionUID = 1L;

  /** Error code for city not found issues. */
  public static final String CITY_NOT_FOUND = "CITY_NOT_FOUND";

  /** The city name that was searched for. */
  private final String searchedCity;

  /**
   * Constructs a new CityNotFoundException with the specified message.
   *
   * @param message the detail message explaining why the city was not found. Must not be null.
   * @throws NullPointerException if message is null
   */
  public CityNotFoundException(final String message) {
    super(CITY_NOT_FOUND, message);
    this.searchedCity = null;
  }

  /**
   * Constructs a new CityNotFoundException with the specified message and searched city.
   *
   * @param message the detail message explaining why the city was not found. Must not be null.
   * @param searchedCity the city name that was searched for. May be null.
   * @throws NullPointerException if message is null
   */
  public CityNotFoundException(final String message, final String searchedCity) {
    super(CITY_NOT_FOUND, message);
    this.searchedCity = searchedCity;
  }

  /**
   * Constructs a new CityNotFoundException with the specified message, searched city, and cause.
   *
   * @param message the detail message explaining why the city was not found. Must not be null.
   * @param searchedCity the city name that was searched for. May be null.
   * @param cause the underlying cause of the city not being found. May be null.
   * @throws NullPointerException if message is null
   */
  public CityNotFoundException(
      final String message, final String searchedCity, final Throwable cause) {
    super(CITY_NOT_FOUND, message, cause);
    this.searchedCity = searchedCity;
  }

  /**
   * Returns the city name that was searched for.
   *
   * @return the searched city name, or null if not provided
   */
  public String getSearchedCity() {
    return searchedCity;
  }

  /**
   * Returns a detailed message including the searched city name.
   *
   * @return a detailed error message
   */
  @Override
  public String getMessage() {
    final String result;
    if (searchedCity != null && !super.getMessage().contains(searchedCity)) {
      result = String.format("City '%s' not found: %s", searchedCity, super.getMessage());
    } else {
      result = super.getMessage();
    }
    return result;
  }

  /**
   * Returns the exception type description.
   *
   * @return "City Not Found Error"
   */
  @Override
  public String getExceptionType() {
    return "City Not Found Error";
  }
}
