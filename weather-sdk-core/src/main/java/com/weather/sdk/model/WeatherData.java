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
import java.util.List;
import java.util.Objects;

/**
 * Immutable data transfer object representing the complete weather information for a location.
 *
 * <p>This class encapsulates all weather-related data returned from the OpenWeatherAPI and provides
 * a type-safe, immutable representation of the weather response.
 *
 * <p><strong>Immutability:</strong> All fields are final and instances are immutable. Thread-safe
 * for concurrent access from multiple threads.
 *
 * <h2>Weather Data Structure</h2>
 *
 * <p>The weather data follows the OpenWeatherAPI response format and includes:
 *
 * <ul>
 *   <li>Current weather conditions (main description, detailed description)
 *   <li>Temperature data (current temperature, feels-like temperature)
 *   <li>Visibility information (in meters)
 *   <li>Wind data (speed and direction)
 *   <li>System information (sunrise, sunset times)
 *   <li>Location metadata (timezone offset, city name)
 *   <li>Data timestamp
 * </ul>
 *
 * <h2>JSON Mapping</h2>
 *
 * <p>This class is designed to be automatically mapped from OpenWeatherAPI JSON responses using
 * Jackson ObjectMapper. All fields use appropriate Jackson annotations for proper deserialization.
 *
 * <h2>Jackson Annotations</h2>
 *
 * <ul>
 *   <li>Maps to OpenWeatherAPI JSON response fields
 *   <li>Ignores unknown properties to handle API changes gracefully
 *   <li>Handles nested objects with proper field mapping
 * </ul>
 *
 * <h2>Usage Example</h2>
 *
 * <pre>
 * WeatherData weather = sdk.getWeather("London");
 *
 * // Access temperature
 * double temp = weather.getTemperature().getTemp(); // in Kelvin
 * double feelsLike = weather.getTemperature().getFeelsLike();
 *
 * // Access weather conditions
 * String main = weather.getWeather().getMain(); // "Clear", "Rain", etc.
 * String description = weather.getWeather().getDescription(); // "clear sky"
 *
 * // Access wind data
 * double windSpeed = weather.getWind().getSpeed(); // in m/s
 *
 * // Access system data (sunrise/sunset)
 * long sunrise = weather.getSystemInfo().getSunrise(); // Unix timestamp
 *
 * // Location information
 * String cityName = weather.getName(); // "London"
 * int timezone = weather.getTimezone(); // Offset from UTC in seconds
 * </pre>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK#getWeather(String)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class WeatherData {

  /** The weather conditions list (main, description). */
  @JsonProperty("weather")
  private final List<Weather> weatherList;

  /** The temperature data (current, feels-like). */
  @JsonProperty("main")
  private final Temperature temperature;

  /** The visibility distance in meters. */
  @JsonProperty("visibility")
  private final int visibility;

  /** The wind data (speed, direction). */
  @JsonProperty("wind")
  private final Wind wind;

  /** The timestamp of the weather data in Unix epoch seconds. */
  @JsonProperty("dt")
  private final long datetime;

  /** The system information (sunrise, sunset). */
  @JsonProperty("sys")
  private final SystemInfo systemInfo;

  /** The timezone offset from UTC in seconds. */
  @JsonProperty("timezone")
  private final int timezone;

  /** The name of the location. */
  @JsonProperty("name")
  private final String name;

  /**
   * Constructs a new WeatherData instance with all required fields.
   *
   * <p><strong>Note:</strong> This constructor is primarily intended for Jackson JSON
   * deserialization. For normal usage, instances are created through the OpenWeatherAPI client and
   * automatically mapped from JSON responses.
   *
   * @param weatherList the weather conditions list (main, description). Must not be null or empty.
   * @param temperature the temperature data (current, feels-like). Must not be null.
   * @param visibility the visibility distance in meters. Must be non-negative.
   * @param wind the wind data (speed, direction). Must not be null.
   * @param datetime the timestamp of the weather data in Unix epoch seconds. Must be non-negative.
   * @param systemInfo the system information (sunrise, sunset). Must not be null.
   * @param timezone the timezone offset from UTC in seconds. Can be positive or negative.
   * @param name the name of the location. Must not be null or empty.
   * @throws NullPointerException if any required parameter is null
   * @throws IllegalArgumentException if visibility or datetime are negative
   */
  @JsonCreator
  public WeatherData(
      @JsonProperty("weather") final List<Weather> weatherList,
      @JsonProperty("main") final Temperature temperature,
      @JsonProperty("visibility") final int visibility,
      @JsonProperty("wind") final Wind wind,
      @JsonProperty("dt") final long datetime,
      @JsonProperty("sys") final SystemInfo systemInfo,
      @JsonProperty("timezone") final int timezone,
      @JsonProperty("name") final String name) {
    this.weatherList = Objects.requireNonNull(weatherList, "Weather information must not be null");
    this.temperature =
        Objects.requireNonNull(temperature, "Temperature information must not be null");
    this.wind = Objects.requireNonNull(wind, "Wind information must not be null");
    this.systemInfo = Objects.requireNonNull(systemInfo, "System information must not be null");
    this.name = Objects.requireNonNull(name, "Location name must not be null");

    if (visibility < 0) {
      throw new IllegalArgumentException("Visibility must be non-negative, was: " + visibility);
    }
    if (datetime < 0) {
      throw new IllegalArgumentException("Datetime must be non-negative, was: " + datetime);
    }

    this.visibility = visibility;
    this.datetime = datetime;
    this.timezone = timezone;
  }

  /**
   * Returns the current weather conditions.
   *
   * @return the weather conditions. Never returns null.
   */
  public Weather getWeather() {
    return weatherList.isEmpty() ? null : weatherList.get(0);
  }

  /**
   * Returns the temperature data including current temperature and feels-like temperature.
   *
   * @return the temperature information. Never returns null.
   */
  public Temperature getTemperature() {
    return temperature;
  }

  /**
   * Returns the visibility distance.
   *
   * @return the visibility in meters. Always non-negative.
   */
  public int getVisibility() {
    return visibility;
  }

  /**
   * Returns the wind data including speed and direction.
   *
   * @return the wind information. Never returns null.
   */
  public Wind getWind() {
    return new Wind(wind.getSpeed(), wind.getDirection());
  }

  /**
   * Returns the timestamp when this weather data was recorded.
   *
   * @return the timestamp in Unix epoch seconds. Always non-negative.
   */
  public long getDatetime() {
    return datetime;
  }

  /**
   * Returns the system information including sunrise and sunset times.
   *
   * @return the system information. Never returns null.
   */
  public SystemInfo getSystemInfo() {
    return new SystemInfo(systemInfo.getSunrise(), systemInfo.getSunset());
  }

  /**
   * Returns the timezone offset from UTC.
   *
   * @return the timezone offset in seconds. Can be positive or negative.
   */
  public int getTimezone() {
    return timezone;
  }

  /**
   * Returns the name of the location.
   *
   * @return the location name. Never returns null or empty.
   */
  public String getName() {
    return name;
  }

  /**
   * Converts the Unix timestamp to Java timestamp (milliseconds).
   *
   * @return the timestamp in Java epoch milliseconds.
   */
  public long getTimestampMillis() {
    return datetime * 1000L;
  }

  /**
   * Indicates whether the data is recent (within the last hour).
   *
   * <p>This is a convenience method for checking if the weather data is considered current based on
   * the data timestamp.
   *
   * @return true if the data is from the last hour, false otherwise
   */
  public boolean isRecent() {
    final long oneHourAgo = System.currentTimeMillis() / 1000L - 3600L;
    return datetime >= oneHourAgo;
  }

  /**
   * Converts the temperature from Kelvin to Celsius.
   *
   * @return the temperature in Celsius
   */
  public double getTemperatureCelsius() {
    return temperature.getTemp() - 273.15;
  }

  /**
   * Converts the temperature from Kelvin to Fahrenheit.
   *
   * @return the temperature in Fahrenheit
   */
  public double getTemperatureFahrenheit() {
    return (temperature.getTemp() - 273.15) * 9 / 5 + 32;
  }

  /**
   * Converts the visibility from meters to kilometers.
   *
   * @return the visibility in kilometers
   */
  public double getVisibilityKm() {
    return visibility / 1000.0;
  }

  /**
   * Converts the visibility from meters to miles.
   *
   * @return the visibility in miles
   */
  public double getVisibilityMiles() {
    return visibility / 1609.344;
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (this == obj) {
      result = true;
    } else if (obj == null || getClass() != obj.getClass()) {
      result = false;
    } else {
      final WeatherData that = (WeatherData) obj;
      result =
          visibility == that.visibility
              && datetime == that.datetime
              && timezone == that.timezone
              && Objects.equals(weatherList, that.weatherList)
              && Objects.equals(temperature, that.temperature)
              && Objects.equals(wind, that.wind)
              && Objects.equals(systemInfo, that.systemInfo)
              && Objects.equals(name, that.name);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        weatherList, temperature, visibility, wind, datetime, systemInfo, timezone, name);
  }

  @Override
  public String toString() {
    return String.format(
        "WeatherData{city='%s', weather='%s', temp=%.1fK, visibility=%dm, timezone=%d}",
        name,
        getWeather() != null ? getWeather().getMain() : "N/A",
        temperature.getTemp(),
        visibility,
        timezone);
  }
}
