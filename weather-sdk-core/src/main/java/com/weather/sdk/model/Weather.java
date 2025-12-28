/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents the weather conditions (main category and detailed description).
 *
 * <p>This class encapsulates the weather information from the OpenWeatherAPI response, providing
 * the primary weather category (e.g., "Clear", "Rain", "Clouds") and a detailed description (e.g.,
 * "clear sky", "light rain").
 *
 * <h2>Weather Categories</h2>
 *
 * <p>OpenWeatherAPI uses standard weather condition codes:
 *
 * <ul>
 *   <li>Clear - Clear sky
 *   <li>Clouds - Various cloud conditions
 *   <li>Rain - Various rain conditions
 *   <li>Drizzle - Light rain
 *   <li>Thunderstorm - Storm conditions
 *   <li>Snow - Snow conditions
 *   <li>Mist - Visibility issues due to particles
 *   <li>Fog - Thick fog conditions
 *   <li>Haze - Hazy conditions
 *   <li>Dust - Dust in the air
 *   <li>Sand - Sand conditions
 *   <li>Ash - Volcanic ash
 *   <li>Squall - Strong wind gusts
 *   <li>Tornado - Tornado conditions
 * </ul>
 *
 * <h2>JSON Mapping</h2>
 *
 * <p>Mapped from OpenWeatherAPI response:
 *
 * <pre>
 * {
 *   "weather": {
 *     "main": "Clear",
 *     "description": "clear sky"
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Weather {

  /** The main weather category (e.g., "Clear", "Rain", "Clouds"). */
  @JsonProperty("main")
  private final String main;

  /** The detailed weather description (e.g., "clear sky", "light rain"). */
  @JsonProperty("description")
  private final String description;

  /**
   * Constructs a new Weather instance.
   *
   * @param main the main weather category. Must not be null or empty.
   * @param description the detailed weather description. Must not be null or empty.
   * @throws NullPointerException if either parameter is null
   * @throws IllegalArgumentException if either parameter is empty
   */
  @JsonCreator
  public Weather(
      @JsonProperty("main") final String main,
      @JsonProperty("description") final String description) {
    this.main = Objects.requireNonNull(main, "Main weather category must not be null");
    this.description = Objects.requireNonNull(description, "Weather description must not be null");

    if (main.isBlank()) {
      throw new IllegalArgumentException("Main weather category must not be empty");
    }
    if (description.isBlank()) {
      throw new IllegalArgumentException("Weather description must not be empty");
    }
  }

  /**
   * Returns the main weather category.
   *
   * @return the main weather category (e.g., "Clear", "Rain", "Clouds"). Never returns null.
   */
  public String getMain() {
    return main;
  }

  /**
   * Returns the detailed weather description.
   *
   * @return the detailed weather description (e.g., "clear sky", "light rain"). Never returns null.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Indicates whether this weather represents clear conditions.
   *
   * @return true if the main category is "Clear", false otherwise
   */
  public boolean isClear() {
    return "Clear".equalsIgnoreCase(main);
  }

  /**
   * Indicates whether this weather represents precipitation (rain, snow, drizzle, etc.).
   *
   * @return true if the weather involves precipitation, false otherwise
   */
  public boolean isPrecipitation() {
    return "Rain".equalsIgnoreCase(main)
        || "Drizzle".equalsIgnoreCase(main)
        || "Snow".equalsIgnoreCase(main);
  }

  /**
   * Indicates whether this weather represents severe conditions.
   *
   * @return true if the weather is considered severe (thunderstorm, tornado, etc.), false otherwise
   */
  public boolean isSevere() {
    return "Thunderstorm".equalsIgnoreCase(main)
        || "Tornado".equalsIgnoreCase(main)
        || "Squall".equalsIgnoreCase(main);
  }

  /**
   * Returns a human-readable summary of the weather conditions.
   *
   * @return a formatted weather summary
   */
  public String getSummary() {
    return main + ": " + description;
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (this == obj) {
      result = true;
    } else if (obj == null || getClass() != obj.getClass()) {
      result = false;
    } else {
      final Weather weather = (Weather) obj;
      result =
          Objects.equals(main, weather.main) && Objects.equals(description, weather.description);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(main, description);
  }

  @Override
  public String toString() {
    return "Weather{main='" + main + "', description='" + description + "'}";
  }
}
