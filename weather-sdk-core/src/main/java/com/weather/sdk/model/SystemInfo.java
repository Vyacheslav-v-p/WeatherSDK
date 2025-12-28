/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents system information including sunrise and sunset times.
 *
 * <p>This class encapsulates the system-related weather information from the OpenWeatherAPI
 * response, providing astronomical data such as sunrise and sunset times for the location.
 *
 * <h2>System Information</h2>
 *
 * <ul>
 *   <li><strong>Sunrise:</strong> Time of sunrise for the location
 *   <li><strong>Sunset:</strong> Time of sunset for the location
 * </ul>
 *
 * <h2>Time Format</h2>
 *
 * <p>All times are provided as Unix timestamps (seconds since January 1, 1970 UTC). This class
 * provides convenient methods for converting to Java Date objects and formatted time strings.
 *
 * <h2>JSON Mapping</h2>
 *
 * <p>Mapped from OpenWeatherAPI response:
 *
 * <pre>
 * {
 *   "sys": {
 *     "sunrise": 1675751262,
 *     "sunset": 1675787560
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings(
    "PMD.TooManyMethods") // Methods are cohesive and provide comprehensive time/date utilities
public final class SystemInfo {

  /** The sunrise time as Unix timestamp (seconds). */
  @JsonProperty("sunrise")
  private long sunrise;

  /** The sunset time as Unix timestamp (seconds). */
  @JsonProperty("sunset")
  private long sunset;

  /** Default constructor for Jackson deserialization. */
  public SystemInfo() {
    // Default constructor for Jackson
  }

  /**
   * Constructs a new SystemInfo instance.
   *
   * @param sunrise the sunrise time as Unix timestamp (seconds). Must be non-negative.
   * @param sunset the sunset time as Unix timestamp (seconds). Must be non-negative.
   * @throws IllegalArgumentException if either timestamp is negative or sunset is before sunrise
   */
  public SystemInfo(final long sunrise, final long sunset) {
    if (sunrise < 0) {
      throw new IllegalArgumentException("Sunrise timestamp must be non-negative, was: " + sunrise);
    }
    if (sunset < 0) {
      throw new IllegalArgumentException("Sunset timestamp must be non-negative, was: " + sunset);
    }
    if (sunset < sunrise) {
      throw new IllegalArgumentException("Sunset time must be after sunrise time");
    }

    this.sunrise = sunrise;
    this.sunset = sunset;
  }

  /**
   * Returns the sunrise time as Unix timestamp.
   *
   * @return the sunrise timestamp in seconds since epoch. Always non-negative.
   */
  public long getSunrise() {
    return sunrise;
  }

  /**
   * Returns the sunset time as Unix timestamp.
   *
   * @return the sunset timestamp in seconds since epoch. Always non-negative.
   */
  public long getSunset() {
    return sunset;
  }

  /**
   * Sets the sunrise time as Unix timestamp.
   *
   * @param sunrise the sunrise time as Unix timestamp (seconds). Must be non-negative.
   * @throws IllegalArgumentException if sunrise timestamp is negative
   */
  public void setSunrise(final long sunrise) {
    if (sunrise < 0) {
      throw new IllegalArgumentException("Sunrise timestamp must be non-negative, was: " + sunrise);
    }
    this.sunrise = sunrise;
  }

  /**
   * Sets the sunset time as Unix timestamp.
   *
   * @param sunset the sunset time as Unix timestamp (seconds). Must be non-negative.
   * @throws IllegalArgumentException if sunset timestamp is negative
   */
  public void setSunset(final long sunset) {
    if (sunset < 0) {
      throw new IllegalArgumentException("Sunset timestamp must be non-negative, was: " + sunset);
    }
    this.sunset = sunset;
  }

  /**
   * Returns the sunrise time as a Java Date object.
   *
   * @return the sunrise date object. Never returns null.
   */
  public java.util.Date getSunriseDate() {
    return new java.util.Date(sunrise * 1000L);
  }

  /**
   * Returns the sunset time as a Java Date object.
   *
   * @return the sunset date object. Never returns null.
   */
  public java.util.Date getSunsetDate() {
    return new java.util.Date(sunset * 1000L);
  }

  /**
   * Returns the duration of daylight (sunset - sunrise).
   *
   * @return the duration of daylight. Always non-negative.
   */
  public java.time.Duration getDayLength() {
    return java.time.Duration.ofSeconds(sunset - sunrise);
  }

  /**
   * Returns the duration of daylight in hours.
   *
   * @return the daylight duration in hours as a decimal number.
   */
  public double getDayLengthHours() {
    return (sunset - sunrise) / 3600.0;
  }

  /**
   * Returns the duration of daylight in hours and minutes.
   *
   * @return an array where [0] is hours and [1] is minutes
   */
  public int[] getDayLengthHoursAndMinutes() {
    final long dayLengthSeconds = sunset - sunrise;
    final int hours = (int) (dayLengthSeconds / 3600);
    final int minutes = (int) (dayLengthSeconds % 3600 / 60);
    return new int[] {hours, minutes};
  }

  /**
   * Checks if the current time falls within daytime hours.
   *
   * <p>Note: This method compares against the current system time and uses the sunrise/sunset times
   * from the weather data. For precise calculations, consider the timezone offset from the parent
   * WeatherData.
   *
   * @return true if the current time is between sunrise and sunset, false otherwise
   */
  public boolean isDaytime() {
    final long now = System.currentTimeMillis() / 1000L;
    return now >= sunrise && now <= sunset;
  }

  /**
   * Checks if the current time is before sunrise.
   *
   * @return true if current time is before sunrise, false otherwise
   */
  public boolean isBeforeSunrise() {
    final long now = System.currentTimeMillis() / 1000L;
    return now < sunrise;
  }

  /**
   * Checks if the current time is after sunset.
   *
   * @return true if current time is after sunset, false otherwise
   */
  public boolean isAfterSunset() {
    final long now = System.currentTimeMillis() / 1000L;
    return now > sunset;
  }

  /**
   * Calculates the time until next sunrise.
   *
   * @return the duration until next sunrise, or empty if currently daytime
   */
  public java.util.Optional<java.time.Duration> getTimeUntilSunrise() {
    final java.util.Optional<java.time.Duration> result;
    if (isDaytime()) {
      result = java.util.Optional.empty();
    } else {
      final long now = System.currentTimeMillis() / 1000L;
      final long timeUntil = sunrise - now;
      result = java.util.Optional.of(java.time.Duration.ofSeconds(Math.max(0, timeUntil)));
    }
    return result;
  }

  /**
   * Calculates the time until next sunset.
   *
   * @return the duration until next sunset, or empty if currently nighttime
   */
  public java.util.Optional<java.time.Duration> getTimeUntilSunset() {
    final java.util.Optional<java.time.Duration> result;
    if (isAfterSunset()) {
      result = java.util.Optional.empty();
    } else {
      final long now = System.currentTimeMillis() / 1000L;
      final long timeUntil = sunset - now;
      result = java.util.Optional.of(java.time.Duration.ofSeconds(Math.max(0, timeUntil)));
    }
    return result;
  }

  /**
   * Formats the sunrise time using the specified pattern.
   *
   * @param pattern the date format pattern (e.g., "HH:mm", "yyyy-MM-dd HH:mm")
   * @return the formatted sunrise time string
   * @throws IllegalArgumentException if the pattern is invalid
   */
  public String formatSunrise(final String pattern) {
    return formatTime(sunrise, pattern);
  }

  /**
   * Formats the sunset time using the specified pattern.
   *
   * @param pattern the date format pattern (e.g., "HH:mm", "yyyy-MM-dd HH:mm")
   * @return the formatted sunset time string
   * @throws IllegalArgumentException if the pattern is invalid
   */
  public String formatSunset(final String pattern) {
    return formatTime(sunset, pattern);
  }

  /**
   * Returns the sunrise time as a localized time string.
   *
   * @return the sunrise time in "h:mm a" format (e.g., "7:27 AM")
   */
  public String getSunriseTimeString() {
    return formatSunrise("h:mm a");
  }

  /**
   * Returns the sunset time as a localized time string.
   *
   * @return the sunset time in "h:mm a" format (e.g., "6:52 PM")
   */
  public String getSunsetTimeString() {
    return formatSunset("h:mm a");
  }

  /**
   * Returns the sunrise time in 24-hour format.
   *
   * @return the sunrise time in "HH:mm" format (e.g., "07:27")
   */
  public String getSunriseTime24h() {
    return formatSunrise("HH:mm");
  }

  /**
   * Returns the sunset time in 24-hour format.
   *
   * @return the sunset time in "HH:mm" format (e.g., "18:52")
   */
  public String getSunsetTime24h() {
    return formatSunset("HH:mm");
  }

  /**
   * Returns a human-readable description of the day length.
   *
   * @return a description like "11 hours 24 minutes" or "14 hours 32 minutes"
   */
  public String getDayLengthDescription() {
    final int[] hoursAndMinutes = getDayLengthHoursAndMinutes();
    final int hours = hoursAndMinutes[0];
    final int minutes = hoursAndMinutes[1];
    final String result;

    if (minutes == 0) {
      result = hours == 1 ? "1 hour" : hours + " hours";
    } else if (hours == 0) {
      result = minutes == 1 ? "1 minute" : minutes + " minutes";
    } else {
      result = String.format("%d hours %d minutes", hours, minutes);
    }

    return result;
  }

  /**
   * Indicates whether it's currently close to sunrise or sunset (within 1 hour).
   *
   * @return true if within 1 hour of sunrise or sunset
   */
  public boolean isNearSunriseOrSunset() {
    final long now = System.currentTimeMillis() / 1000L;
    final long oneHour = 3600L;

    return Math.abs(now - sunrise) <= oneHour || Math.abs(now - sunset) <= oneHour;
  }

  /**
   * Returns sunrise and sunset information as a formatted string.
   *
   * @return a formatted string containing sunrise and sunset times
   */
  public String formatSunriseSunset() {
    return String.format(
        "Sunrise: %s, Sunset: %s (%s daylight)",
        getSunriseTime24h(), getSunsetTime24h(), getDayLengthDescription());
  }

  /**
   * Helper method to format a Unix timestamp using the specified pattern.
   *
   * @param timestamp the Unix timestamp in seconds
   * @param pattern the date format pattern
   * @return the formatted time string
   * @throws IllegalArgumentException if the pattern is invalid
   */
  private String formatTime(
      @SuppressWarnings("unused") final long timestamp, final String pattern) {
    final String result;
    try {
      final java.time.format.DateTimeFormatter formatter =
          java.time.format.DateTimeFormatter.ofPattern(pattern);
      result =
          getSunriseDate()
              .toInstant()
              .atZone(java.time.ZoneId.systemDefault())
              .withEarlierOffsetAtOverlap()
              .format(formatter);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid date format pattern: " + pattern, e);
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (this == obj) {
      result = true;
    } else if (obj == null || getClass() != obj.getClass()) {
      result = false;
    } else {
      final SystemInfo systemInfo = (SystemInfo) obj;
      result = sunrise == systemInfo.sunrise && sunset == systemInfo.sunset;
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sunrise, sunset);
  }

  @Override
  public String toString() {
    return String.format(
        "SystemInfo{sunrise=%s, sunset=%s, daylight=%s}",
        getSunriseTime24h(), getSunsetTime24h(), getDayLengthDescription());
  }
}
