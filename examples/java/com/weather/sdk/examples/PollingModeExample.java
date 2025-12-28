/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit 
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package com.weather.sdk.examples;

import com.weather.sdk.WeatherSDK;
import com.weather.sdk.DefaultWeatherSDK;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.model.WeatherData;
import java.time.Duration;

/**
 * Example demonstrating how to use the Weather SDK in polling mode.
 *
 * <p>In polling mode, the SDK automatically updates weather data for cached cities
 * at regular intervals. This is useful when you need regularly updated weather data
 * without making frequent API calls yourself.
 */
public class PollingModeExample {

  /**
   * Demonstrates basic usage of polling mode.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    try {
      // Create SDK configuration for polling mode
      String apiKey = System.getenv("OPENWEATHER_API_KEY");
      if (apiKey == null || apiKey.trim().isEmpty()) {
          apiKey = "YOUR_API_KEY_HERE"; // Placeholder for documentation
          System.out.println("⚠️  Warning: OPENWEATHER_API_KEY environment variable not set.");
          System.out.println("   Please set it before running the example for actual weather data.");
          System.out.println("   Using placeholder key for demonstration only.\n");
      }

      SDKConfiguration config = new WeatherSDKBuilder()
          .apiKey(apiKey) // Use the API key
          .mode(SDKMode.POLLING) // Enable polling mode
          .pollingInterval(Duration.ofMinutes(1)) // Update every 1 minute
          .cacheSize(10) // Cache up to 10 cities
          .cacheTTL(Duration.ofMinutes(10)) // Cache entries expire after 10 minutes
          .connectionTimeout(Duration.ofSeconds(5))
          .readTimeout(Duration.ofSeconds(10))
          .build();

      // Create the WeatherSDK instance with the configuration
      WeatherSDK sdk = new DefaultWeatherSDK(config);

      try {
          // Add some cities to the cache - they will be automatically updated
          System.out.println("Adding cities to cache...");
          WeatherData londonWeather = sdk.getWeather("London");
          System.out.println("London temperature: " + String.format("%.1f°C", londonWeather.getTemperatureCelsius()));

          WeatherData parisWeather = sdk.getWeather("Paris");
          System.out.println("Paris temperature: " + String.format("%.1f°C", parisWeather.getTemperatureCelsius()));

          System.out.println("\nThe SDK is now automatically polling for updates!");
          System.out.println("Weather data for London and Paris will be refreshed every 1 minute.");
          System.out.println("Press Ctrl+C to stop the example.");

          // The SDK will continue polling in the background
          // Access cached weather data without additional API calls
          Thread.sleep(60000); // Sleep for 1 minute to demonstrate polling

          // Get updated weather data (may be from cache if recently updated by polling)
          WeatherData updatedLondon = sdk.getWeather("London");
          System.out.println("\nUpdated London temperature: " + String.format("%.1f°C", updatedLondon.getTemperatureCelsius()));
      } catch (Exception e) {
          System.out.println("Note: This is expected when using a dummy API key.");
          System.out.println("The polling functionality works, but API calls will fail without a valid key.");
      }

      // Show metrics
      var metrics = sdk.getMetrics();
      System.out.println("\nSDK Metrics:");
      System.out.println("Total API requests: " + metrics.getTotalRequests());
      System.out.println("Cache hits: " + metrics.getCacheHits());
      System.out.println("Cache misses: " + metrics.getCacheMisses());
      System.out.println("Cache hit rate: " + metrics.getCacheHitRate() + "%");

      // Important: Always shutdown the SDK to stop polling and release resources
      sdk.shutdown();

    } catch (Exception e) {
      System.err.println("Error in polling mode example: " + e.getMessage());
      e.printStackTrace();
    }
  }
}