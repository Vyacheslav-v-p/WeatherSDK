/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk.config;

import java.util.concurrent.TimeUnit;

/**
 * Enumeration representing the operating modes for the Weather SDK.
 *
 * <p>This enum defines the two available modes for weather data retrieval: on-demand (manual) and
 * polling (automatic). Each mode has different characteristics and is suited for different use
 * cases.
 *
 * <h2>Mode Characteristics</h2>
 *
 * <ul>
 *   <li><strong>ON_DEMAND:</strong> Fetches weather data when explicitly requested
 *   <li><strong>POLLING:</strong> Automatically fetches weather data at regular intervals
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>On-Demand Mode</h3>
 *
 * <pre>
 * // Create SDK in on-demand mode
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key")
 *     .mode(SDKMode.ON_DEMAND) // Manual data fetching
 *     .build();
 *
 * try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
 *
 * // Fetch weather data only when needed
 * WeatherData weather = sdk.getWeather("London"); // Makes API call
 * WeatherData cached = sdk.getWeather("London"); // Uses cache if fresh
 * </pre>
 *
 * <h3>Polling Mode</h3>
 *
 * <pre>
 * // Create SDK in polling mode with 5-minute intervals
 * SDKConfiguration config = new WeatherSDKBuilder()
 *     .apiKey("your-api-key")
 *     .mode(SDKMode.POLLING) // Automatic data fetching
 *     .pollingInterval(Duration.ofMinutes(5))
 *     .build();
 *
 * try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
 *
 * // Weather data is automatically updated in background
 * WeatherData weather = sdk.getWeather("Paris"); // Uses latest polled data
 * // Background thread fetches data every 5 minutes
 * </pre>
 *
 * <h2>Mode Selection Guide</h2>
 *
 * <h3>Use ON_DEMAND when:</h3>
 *
 * <ul>
 *   <li>Weather data is needed infrequently (sporadic usage)
 *   <li>Resource usage should be minimized
 *   <li>Explicit control over API calls is required
 *   <li>User interactions drive the data retrieval
 *   <li>Lower API costs are preferred
 * </ul>
 *
 * <h3>Use POLLING when:</h3>
 *
 * <ul>
 *   <li>Real-time weather updates are critical
 *   <li>Applications display continuously updated weather information
 *   <li>Background data refresh is acceptable
 *   <li>Response time is more important than API costs
 *   <li>Weather data is displayed continuously (dashboards, widgets)
 * </ul>
 *
 * <h2>Default Configuration</h2>
 *
 * <ul>
 *   <li><strong>Default Mode:</strong> ON_DEMAND
 *   <li><strong>Default Polling Interval:</strong> 5 minutes (when POLLING mode is used)
 *   <li><strong>Polling Interval Range:</strong> 1 minute to 24 hours
 * </ul>
 *
 * <h2>Performance Considerations</h2>
 *
 * <h3>On-Demand Mode:</h3>
 *
 * <ul>
 *   <li>API calls made only when explicitly requested
 *   <li>Lower network and API usage
 *   <li>Potential cache misses result in API calls
 *   <li>Suitable for memory-constrained environments
 * </ul>
 *
 * <h3>Polling Mode:</h3>
 *
 * <ul>
 *   <li>Regular background API calls regardless of usage
 *   <li>Higher network and API usage
 *   <li>Consistent cache freshness
 *   <li>Background thread overhead
 *   <li>Better user experience with immediate data availability
 * </ul>
 *
 * @since 1.0.0
 * @see com.weather.sdk.WeatherSDK
 * @see com.weather.sdk.config.WeatherSDKBuilder
 * @see com.weather.sdk.config.SDKConfiguration
 */
public enum SDKMode {

  /**
   * On-demand mode for manual weather data fetching.
   *
   * <p>In this mode, the SDK only makes API calls to OpenWeatherAPI when explicitly requested
   * through {@code getWeather()}. This is the default mode and is ideal for applications that need
   * weather data sporadically or in response to user interactions.
   *
   * <p><strong>Characteristics:</strong>
   *
   * <ul>
   *   <li>API calls made only when needed
   *   <li>Lower resource usage
   *   <li>Potential cache misses on first requests
   *   <li>Manual control over data fetching
   *   <li>Lower API costs
   * </ul>
   *
   * <p><strong>Use Cases:</strong>
   *
   * <ul>
   *   <li>Weather apps with infrequent usage
   *   <li>Batch processing of weather data
   *   <li>Manual weather lookups
   *   <li>Resource-constrained environments
   *   <li>Cost-sensitive applications
   * </ul>
   */
  ON_DEMAND("on-demand", "Manual weather data fetching on request"),

  /**
   * Polling mode for automatic weather data fetching.
   *
   * <p>In this mode, the SDK automatically fetches weather data at regular intervals in the
   * background, regardless of user requests. This ensures that weather data is always fresh and
   * readily available when requested.
   *
   * <p><strong>Characteristics:</strong>
   *
   * <ul>
   *   <li>Background API calls at regular intervals
   *   <li>Higher resource usage
   *   <li>Consistent cache freshness
   *   <li>Automatic data updates
   *   <li>Background thread management
   * </ul>
   *
   * <p><strong>Use Cases:</strong>
   *
   * <ul>
   *   <li>Weather dashboards and widgets
   *   <li>Real-time weather monitoring
   *   <li>Continuous weather displays
   *   <li>Time-sensitive applications
   *   <li>Applications where response time is critical
   * </ul>
   */
  POLLING("polling", "Automatic weather data fetching at regular intervals");

  /** The human-readable display name for this mode. */
  private final String displayName;

  /** The description of this mode. */
  private final String description;

  /**
   * Constructs a new SDKMode with the specified display name and description.
   *
   * @param displayName the human-readable name for this mode. Must not be null.
   * @param description a brief description of this mode. Must not be null.
   * @throws NullPointerException if either parameter is null
   */
  SDKMode(final String displayName, final String description) {
    this.displayName = displayName;
    this.description = description;
  }

  /**
   * Returns the human-readable display name for this mode.
   *
   * @return the display name (e.g., "on-demand", "polling"). Never returns null.
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Returns the description of this mode.
   *
   * @return the mode description. Never returns null.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Indicates whether this mode requires background thread management.
   *
   * @return true if the mode requires background threads (polling), false otherwise
   */
  public boolean requiresBackgroundThreads() {
    return this == POLLING;
  }

  /**
   * Indicates whether this mode makes automatic API calls.
   *
   * @return true if the mode makes automatic API calls (polling), false otherwise
   */
  public boolean isAutomatic() {
    return this == POLLING;
  }

  /**
   * Returns the recommended default polling interval for this mode.
   *
   * <p>For ON_DEMAND mode, this returns a zero duration indicating no polling. For POLLING mode,
   * this returns the recommended 5-minute interval.
   *
   * @return the recommended polling interval as a duration
   */
  public java.time.Duration getRecommendedPollingInterval() {
    final java.time.Duration result;
    if (this == POLLING) {
      result = java.time.Duration.ofMinutes(5);
    } else {
      result = java.time.Duration.ZERO;
    }
    return result;
  }

  /**
   * Validates that a polling interval is appropriate for this mode.
   *
   * @param interval the polling interval to validate. May be null for ON_DEMAND.
   * @return true if the interval is valid for this mode
   * @throws IllegalArgumentException if the interval is invalid for this mode
   */
  public boolean validatePollingInterval(final java.time.Duration interval) {
    final boolean result;
    if (this == ON_DEMAND) {
      result = interval == null || interval.isZero() || interval.isNegative();
    } else { // POLLING
      if (interval == null || interval.isZero() || interval.isNegative()) {
        result = false;
      } else {
        // Validate range: 1 minute to 24 hours
        final long minutes = interval.toMinutes();
        result = minutes >= 1 && minutes <= (24 * 60);
      }
    }
    return result;
  }

  /**
   * Gets a suggested polling interval in the specified time unit.
   *
   * @param timeUnit the time unit for the result
   * @return the polling interval in the specified unit
   * @throws UnsupportedOperationException if this mode doesn't support polling
   */
  public long getRecommendedPollingInterval(final TimeUnit timeUnit) {
    if (this == ON_DEMAND) {
      throw new UnsupportedOperationException("ON_DEMAND mode does not support polling");
    }

    return timeUnit.convert(getRecommendedPollingInterval());
  }

  /**
   * Returns a string representation of this mode.
   *
   * @return the display name of this mode
   */
  @Override
  public String toString() {
    return displayName;
  }
}
