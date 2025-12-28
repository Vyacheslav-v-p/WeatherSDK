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
 * Represents temperature data including current temperature and perceived temperature.
 *
 * <p>This class encapsulates temperature information from the OpenWeatherAPI response, providing
 * both the current temperature and the "feels like" temperature that accounts for factors like
 * humidity and wind chill.
 *
 * <h2>Temperature Units</h2>
 *
 * <p>OpenWeatherAPI returns temperatures in Kelvin by default. This class provides convenience
 * methods for conversion to Celsius and Fahrenheit.
 *
 * <h2>Temperature Characteristics</h2>
 *
 * <ul>
 *   <li><strong>Current Temperature:</strong> The actual air temperature
 *   <li><strong>Feels Like:</strong> The perceived temperature accounting for wind, humidity, etc.
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * Temperature temp = weather.getTemperature();
 *
 * // Get temperatures in Kelvin (API default)
 * double current = temp.getTemp(); // 294.15K (21°C)
 * double feelsLike = temp.getFeelsLike(); // 293.15K (20°C)
 *
 * // Convert to other units
 * double celsius = temp.getTempCelsius(); // 21.0°C
 * double fahrenheit = temp.getTempFahrenheit(); // 69.8°F
 *
 * // Check if temperature is comfortable
 * if (temp.isComfortable()) {
 *   System.out.println("Pleasant weather conditions");
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Temperature {

  /** Comfortable temperature range minimum in Celsius. */
  private static final double TEMP_COMFORT_MIN = 18.0;

  /** Comfortable temperature range maximum in Celsius. */
  private static final double TEMP_COMFORT_MAX = 24.0;

  /** Freezing temperature threshold in Celsius. */
  private static final double FREEZING_TEMP = 0.0;

  /** Very hot temperature threshold in Celsius. */
  private static final double VERY_HOT_TEMP = 35.0;

  /** Temperature categories thresholds in Celsius. */
  private static final double TEMP_COLD = 10.0;

  /** Temperature categories thresholds in Celsius. */
  private static final double TEMP_COOL = 18.0;

  /** Temperature categories thresholds in Celsius. */
  private static final double TEMP_MILD = 25.0;

  /** Temperature categories thresholds in Celsius. */
  private static final double TEMP_WARM = 30.0;

  /** Temperature categories thresholds in Celsius. */
  private static final double TEMP_HOT = 35.0;

  /** The current temperature in Kelvin. */
  @JsonProperty("temp")
  private final double temp;

  /** The perceived ("feels like") temperature in Kelvin. */
  @JsonProperty("feels_like")
  private final double feelsLike;

  /**
   * Constructs a new Temperature instance.
   *
   * @param temp the current temperature in Kelvin. Must be non-negative (absolute zero or above).
   * @param feelsLike the perceived temperature in Kelvin. Must be non-negative.
   * @throws IllegalArgumentException if either temperature is below absolute zero (0K)
   */
  @JsonCreator
  public Temperature(
      @JsonProperty("temp") final double temp, @JsonProperty("feels_like") final double feelsLike) {
    if (temp < 0) {
      throw new IllegalArgumentException(
          "Current temperature must be non-negative (Kelvin), was: " + temp);
    }
    if (feelsLike < 0) {
      throw new IllegalArgumentException(
          "Feels-like temperature must be non-negative (Kelvin), was: " + feelsLike);
    }

    this.temp = temp;
    this.feelsLike = feelsLike;
  }

  /**
   * Returns the current temperature in Kelvin.
   *
   * @return the current temperature in Kelvin. Always non-negative.
   */
  public double getTemp() {
    return temp;
  }

  /**
   * Returns the perceived ("feels like") temperature in Kelvin.
   *
   * @return the feels-like temperature in Kelvin. Always non-negative.
   */
  public double getFeelsLike() {
    return feelsLike;
  }

  /**
   * Converts the current temperature to Celsius.
   *
   * @return the temperature in Celsius
   */
  public double getTempCelsius() {
    return temp - 273.15;
  }

  /**
   * Converts the feels-like temperature to Celsius.
   *
   * @return the feels-like temperature in Celsius
   */
  public double getFeelsLikeCelsius() {
    return feelsLike - 273.15;
  }

  /**
   * Converts the current temperature to Fahrenheit.
   *
   * @return the temperature in Fahrenheit
   */
  public double getTempFahrenheit() {
    return (temp - 273.15) * 9 / 5 + 32;
  }

  /**
   * Converts the feels-like temperature to Fahrenheit.
   *
   * @return the feels-like temperature in Fahrenheit
   */
  public double getFeelsLikeFahrenheit() {
    return (feelsLike - 273.15) * 9 / 5 + 32;
  }

  /**
   * Returns the temperature difference between actual and perceived temperature.
   *
   * @return the temperature difference in Kelvin (positive if feels warmer, negative if feels
   *     cooler)
   */
  public double getTempDifference() {
    return feelsLike - temp;
  }

  /**
   * Indicates whether the temperature is considered comfortable for humans.
   *
   * <p>Comfortable temperature range is defined as 18-24°C (65-75°F).
   *
   * @return true if the temperature is in comfortable range, false otherwise
   */
  public boolean isComfortable() {
    final double celsius = getTempCelsius();
    return celsius >= TEMP_COMFORT_MIN && celsius <= TEMP_COMFORT_MAX;
  }

  /**
   * Indicates whether the temperature is freezing or below.
   *
   * @return true if the temperature is 0°C or below, false otherwise
   */
  public boolean isFreezing() {
    return getTempCelsius() <= FREEZING_TEMP;
  }

  /**
   * Indicates whether the temperature is very hot.
   *
   * @return true if the temperature is 35°C or above, false otherwise
   */
  public boolean isVeryHot() {
    return getTempCelsius() >= VERY_HOT_TEMP;
  }

  /**
   * Returns a human-readable temperature description.
   *
   * @return a descriptive temperature category
   */
  public String getDescription() {
    final double celsius = getTempCelsius();
    final String result;

    if (celsius < FREEZING_TEMP) {
      result = "Freezing";
    } else if (celsius < TEMP_COLD) {
      result = "Cold";
    } else if (celsius < TEMP_COOL) {
      result = "Cool";
    } else if (celsius < TEMP_MILD) {
      result = "Mild";
    } else if (celsius < TEMP_WARM) {
      result = "Warm";
    } else if (celsius < TEMP_HOT) {
      result = "Hot";
    } else {
      result = "Very Hot";
    }

    return result;
  }

  /**
   * Returns a formatted temperature string with unit.
   *
   * @param unit the temperature unit (K, C, or F)
   * @return formatted temperature string
   */
  public String format(final char unit) {
    double value;
    String unitStr;

    switch (Character.toUpperCase(unit)) {
      case 'C':
        value = getTempCelsius();
        unitStr = "°C";
        break;
      case 'F':
        value = getTempFahrenheit();
        unitStr = "°F";
        break;
      default: // 'K'
        value = temp;
        unitStr = "K";
        break;
    }

    return String.format("%.1f%s", value, unitStr);
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (this == obj) {
      result = true;
    } else if (obj == null || getClass() != obj.getClass()) {
      result = false;
    } else {
      final Temperature that = (Temperature) obj;
      result =
          Double.compare(temp, that.temp) == 0 && Double.compare(feelsLike, that.feelsLike) == 0;
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(temp, feelsLike);
  }

  @Override
  public String toString() {
    return String.format(
        "Temperature{temp=%.1fK (%.1f°C), feelsLike=%.1fK (%.1f°C)}",
        temp, getTempCelsius(), feelsLike, getFeelsLikeCelsius());
  }
}
