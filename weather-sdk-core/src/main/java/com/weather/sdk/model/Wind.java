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
 * Represents wind data including speed and direction information.
 *
 * <p>This class encapsulates wind information from the OpenWeatherAPI response, providing the wind
 * speed in meters per second and optionally wind direction in degrees.
 *
 * <h2>Wind Speed Characteristics</h2>
 *
 * <ul>
 *   <li><strong>Speed:</strong> Wind speed in meters per second (m/s)
 *   <li><strong>Direction:</strong> Wind direction in meteorological degrees (0-360°)
 * </ul>
 *
 * <h2>Wind Speed Reference</h2>
 *
 * <ul>
 *   <li>0-2 m/s: Calm to light air (0-4.5 mph)
 *   <li>2-4 m/s: Light breeze (4.5-9 mph)
 *   <li>4-8 m/s: Moderate breeze (9-18 mph)
 *   <li>8-13 m/s: Fresh breeze (18-29 mph)
 *   <li>13-18 m/s: Strong breeze (29-40 mph)
 *   <li>18-25 m/s: Near gale (40-55 mph)
 *   <li>25+ m/s: Gale or higher (55+ mph)
 * </ul>
 *
 * <h2>JSON Mapping</h2>
 *
 * <p>Mapped from OpenWeatherAPI response:
 *
 * <pre>
 * {
 *   "wind": {
 *     "speed": 5.2,
 *     "deg": 270
 *   }
 * }
 * </pre>
 *
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings({
  "PMD.ShortClassName", // "Wind" is a domain-appropriate name
  "PMD.TooManyMethods" // Domain model with rich wind-related functionality
})
public final class Wind {

  /** Beaufort scale 1 upper limit in m/s. */
  private static final double BEAUFORT_1_MAX = 1.5;

  /** Beaufort scale 2 upper limit in m/s. */
  private static final double BEAUFORT_2_MAX = 3.3;

  /** Beaufort scale 3 upper limit in m/s. */
  private static final double BEAUFORT_3_MAX = 5.4;

  /** Beaufort scale 4 upper limit in m/s. */
  private static final double BEAUFORT_4_MAX = 7.9;

  /** Beaufort scale 5 upper limit in m/s. */
  private static final double BEAUFORT_5_MAX = 10.7;

  /** Beaufort scale 6 upper limit in m/s. */
  private static final double BEAUFORT_6_MAX = 13.8;

  /** Beaufort scale 7 upper limit in m/s. */
  private static final double BEAUFORT_7_MAX = 17.1;

  /** Beaufort scale 8 upper limit in m/s. */
  private static final double BEAUFORT_8_MAX = 20.7;

  /** Beaufort scale 9 upper limit in m/s. */
  private static final double BEAUFORT_9_MAX = 24.4;

  /** Beaufort scale 10 upper limit in m/s. */
  private static final double BEAUFORT_10_MAX = 28.4;

  /** Beaufort scale 11 upper limit in m/s. */
  private static final double BEAUFORT_11_MAX = 32.6;

  /** Strong wind threshold in m/s. */
  private static final double WIND_STRONG = 13.0;

  /** Calm wind threshold in m/s. */
  private static final double WIND_CALM = 2.0;

  /** Beaufort scale 0 upper limit in m/s. */
  private static final double BEAUFORT_0_MAX = 0.0;

  /** The wind speed in meters per second. */
  @JsonProperty("speed")
  private double speed;

  /** The wind direction in degrees (0-360), or null if not available. */
  @JsonProperty("deg")
  private Integer direction;

  /** Default constructor for Jackson deserialization. */
  public Wind() {
    // Default constructor for Jackson
  }

  /**
   * Constructs a new Wind instance with speed and optional direction.
   *
   * @param speed the wind speed in meters per second. Must be non-negative.
   * @param direction the wind direction in degrees (0-360), or null if not available.
   * @throws IllegalArgumentException if speed is negative or direction is out of range
   */
  public Wind(final double speed, final Integer direction) {
    setSpeed(speed);
    setDirection(direction);
  }

  /**
   * Constructs a new Wind instance with speed only (no direction information).
   *
   * @param speed the wind speed in meters per second. Must be non-negative.
   * @throws IllegalArgumentException if speed is negative
   */
  public Wind(final double speed) {
    this(speed, null);
  }

  /**
   * Returns the wind speed in meters per second.
   *
   * @return the wind speed in m/s. Always non-negative.
   */
  public double getSpeed() {
    return speed;
  }

  /**
   * Returns the wind direction in meteorological degrees.
   *
   * @return the wind direction in degrees (0-360), or null if not available
   */
  public Integer getDirection() {
    return direction;
  }

  /**
   * Sets the wind speed in meters per second.
   *
   * @param speed the wind speed in meters per second. Must be non-negative.
   * @throws IllegalArgumentException if speed is negative
   */
  public void setSpeed(final double speed) {
    if (speed < 0) {
      throw new IllegalArgumentException("Wind speed must be non-negative, was: " + speed + " m/s");
    }
    this.speed = speed;
  }

  /**
   * Sets the wind direction in degrees (0-360).
   *
   * @param direction the wind direction in degrees (0-360), or null if not available.
   * @throws IllegalArgumentException if direction is out of range
   */
  public void setDirection(final Integer direction) {
    if (direction != null && (direction < 0 || direction > 360)) {
      throw new IllegalArgumentException("Wind direction must be 0-360 degrees, was: " + direction);
    }
    this.direction = direction;
  }

  /**
   * Converts wind speed to miles per hour.
   *
   * @return the wind speed in mph
   */
  public double getSpeedMph() {
    return speed * 2.237; // m/s to mph
  }

  /**
   * Converts wind speed to kilometers per hour.
   *
   * @return the wind speed in km/h
   */
  public double getSpeedKmh() {
    return speed * 3.6; // m/s to km/h
  }

  /**
   * Converts wind speed to knots (nautical miles per hour).
   *
   * @return the wind speed in knots
   */
  public double getSpeedKnots() {
    return speed * 1.943_84; // m/s to knots
  }

  /**
   * Returns the wind direction as a cardinal direction (N, NE, E, SE, S, SW, W, NW).
   *
   * @return the cardinal direction, or null if direction is not available
   */
  public String getCardinalDirection() {
    final String result;
    if (direction == null) {
      result = null;
    } else {
      final String[] directions = {
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW",
        "NNW"
      };

      final int index = (int) Math.round(direction / 22.5) % 16;
      result = directions[index];
    }
    return result;
  }

  /**
   * Checks if the wind is coming from the specified direction range.
   *
   * @param center the center direction in degrees (0-360)
   * @param range the range in degrees on either side of center (e.g., 45° for 8 compass points)
   * @return true if wind is from the specified direction range
   */
  public boolean isFromDirection(final double center, final double range) {
    final boolean result;
    if (direction == null) {
      result = false;
    } else {
      final double normalizedCenter = ((center - range) % 360 + 360) % 360;
      final double normalizedEnd = ((center + range) % 360 + 360) % 360;
      final double normalizedDir = ((direction - range) % 360 + 360) % 360;

      if (normalizedCenter <= normalizedEnd) {
        result = normalizedDir >= normalizedCenter && normalizedDir <= normalizedEnd;
      } else {
        // Range crosses 0°/360° boundary
        result = normalizedDir >= normalizedCenter || normalizedDir <= normalizedEnd;
      }
    }
    return result;
  }

  /**
   * Indicates whether the wind is considered strong (≥ 13 m/s or 29+ mph).
   *
   * @return true if wind is strong, false otherwise
   */
  public boolean isStrong() {
    return speed >= WIND_STRONG;
  }

  /**
   * Indicates whether the wind is considered calm or light (≤ 2 m/s or ≤ 4.5 mph).
   *
   * @return true if wind is calm to light, false otherwise
   */
  public boolean isCalm() {
    return speed <= WIND_CALM;
  }

  /**
   * Gets the Beaufort scale equivalent for the wind speed.
   *
   * <p>Beaufort Scale:
   *
   * <ul>
   *   <li>0: Calm (0 m/s)
   *   <li>1: Light air (0.3-1.5 m/s)
   *   <li>2: Light breeze (1.6-3.3 m/s)
   *   <li>3: Gentle breeze (3.4-5.4 m/s)
   *   <li>4: Moderate breeze (5.5-7.9 m/s)
   *   <li>5: Fresh breeze (8.0-10.7 m/s)
   *   <li>6: Strong breeze (10.8-13.8 m/s)
   *   <li>7: Near gale (13.9-17.1 m/s)
   *   <li>8: Gale (17.2-20.7 m/s)
   *   <li>9: Severe gale (20.8-24.4 m/s)
   *   <li>10: Storm (24.5-28.4 m/s)
   *   <li>11: Violent storm (28.5-32.6 m/s)
   *   <li>12: Hurricane force (32.7+ m/s)
   * </ul>
   *
   * @return the Beaufort scale number (0-12)
   */
  public int getBeaufortScale() {
    final double[] beaufortLimits = {
      BEAUFORT_0_MAX, BEAUFORT_1_MAX, BEAUFORT_2_MAX, BEAUFORT_3_MAX,
      BEAUFORT_4_MAX, BEAUFORT_5_MAX, BEAUFORT_6_MAX, BEAUFORT_7_MAX,
      BEAUFORT_8_MAX, BEAUFORT_9_MAX, BEAUFORT_10_MAX, BEAUFORT_11_MAX
    };

    int result = 12;
    for (int i = 0; i < beaufortLimits.length; i++) {
      if (speed <= beaufortLimits[i]) {
        result = i;
        break;
      }
    }
    return result;
  }

  /**
   * Gets a human-readable description of the wind conditions.
   *
   * @return a descriptive wind condition
   */
  public String getDescription() {
    final int scale = getBeaufortScale();
    return getBeaufortDescription(scale);
  }

  /**
   * Gets the Beaufort scale description for the given scale number.
   *
   * @param scale the Beaufort scale number (0-12)
   * @return the description of the Beaufort scale level
   */
  public String getBeaufortDescription(final int scale) {
    final String[] descriptions = {
      "Calm",
      "Light air",
      "Light breeze",
      "Gentle breeze",
      "Moderate breeze",
      "Fresh breeze",
      "Strong breeze",
      "Near gale",
      "Gale",
      "Severe gale",
      "Storm",
      "Violent storm",
      "Hurricane force"
    };

    final String result;
    if (scale >= 0 && scale < descriptions.length) {
      result = descriptions[scale];
    } else {
      result = "Unknown";
    }
    return result;
  }

  /**
   * Returns a formatted wind string.
   *
   * @return formatted wind information
   */
  public String format() {
    final String result;
    if (direction != null) {
      result =
          String.format(
              "Wind{%.1f m/s (%s) from %s (%d°)}",
              speed, getDescription(), getCardinalDirection(), direction);
    } else {
      result = String.format("Wind{%.1f m/s (%s)}", speed, getDescription());
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
      final Wind wind = (Wind) obj;
      result = Double.compare(speed, wind.speed) == 0 && Objects.equals(direction, wind.direction);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(speed, direction);
  }

  @Override
  public String toString() {
    return format();
  }
}
