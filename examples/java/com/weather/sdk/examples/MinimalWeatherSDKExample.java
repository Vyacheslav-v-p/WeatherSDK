/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit 
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */

package com.weather.sdk.examples;

import com.weather.sdk.DefaultWeatherSDK;
import com.weather.sdk.SDKMetrics;
import com.weather.sdk.WeatherSDK;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.exception.ConfigurationException;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.model.WeatherData;
import java.time.Duration;

/**
 * Minimal Working Example for Weather SDK
 * 
 * This example demonstrates the basic usage of the Weather SDK including:
 * - SDK configuration and initialization
 * - Fetching weather data for a city
 * - Working with weather data objects
 * - Error handling
 * - Resource management with try-with-resources
 * 
 * Prerequisites:
 * - You need a valid OpenWeatherAPI key (get one free at https://openweathermap.org/api)
 * - Add the API key to your environment variables or replace the placeholder below
 * 
 * How to run:
 * 1. Set environment variable: export OPENWEATHER_API_KEY="your-api-key-here"
 * 2. Compile: javac -cp "weather-sdk-core/target/classes:weather-sdk-core/target/lib/*" examples/java/com/weather/sdk/examples/MinimalWeatherSDKExample.java
 * 3. Run: java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" com.weather.sdk.examples.MinimalWeatherSDKExample
 */
public class MinimalWeatherSDKExample {

    public static void main(String[] args) {
        // Get API key from environment variable or use placeholder
        String apiKey = System.getenv("OPENWEATHER_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("Error: Please set OPENWEATHER_API_KEY environment variable");
            System.err.println("Get your free API key at: https://openweathermap.org/api");
            System.exit(1);
        }

        // Example 1: Basic on-demand weather fetching
        basicWeatherExample(apiKey);
        
        // Example 2: Error handling
        errorHandlingExample(apiKey);
        
        // Example 3: Working with different cities
        multipleCitiesExample(apiKey);
        
        // Example 4: Query weather by city name (returns first match)
        cityNameQueryExample(apiKey);
    }

    /**
     * Example 1: Basic weather fetching using try-with-resources for automatic cleanup
     */
    private static void basicWeatherExample(String apiKey) {
        System.out.println("=== Basic Weather Example ===");
        
        try (WeatherSDK sdk = new DefaultWeatherSDK(createConfiguration(apiKey))) {

            // Fetch weather for London
            WeatherData weather = sdk.getWeather("London");
            
            System.out.println("Weather for " + weather.getName() + ":");
            System.out.println("  Temperature: " + String.format("%.1f°C", weather.getTemperatureCelsius()));
            System.out.println("  Feels like: " + String.format("%.1f°C", weather.getTemperature().getFeelsLikeCelsius()));
            
            if (weather.getWeather() != null) {
                System.out.println(" Conditions: " + weather.getWeather().getMain());
                System.out.println("  Description: " + weather.getWeather().getDescription());
            }
            
            System.out.println("  Visibility: " + weather.getVisibilityKm() + " km");
            System.out.println("  Wind speed: " + weather.getWind().getSpeed() + " m/s");
            System.out.println("  Wind direction: " + weather.getWind().getCardinalDirection());
            
            // Check if data is recent
            if (weather.isRecent()) {
                System.out.println("  Data freshness: Recent (within last hour)");
            }
            
        } catch (WeatherSDKException e) {
            System.err.println("Weather API Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Example 2: Demonstrating error handling for invalid cities
     */
    private static void errorHandlingExample(String apiKey) {
        System.out.println("\n=== Error Handling Example ===");
        
        try (WeatherSDK sdk = new DefaultWeatherSDK(createConfiguration(apiKey))) {

            // Try to fetch weather for an invalid city
            try {
                sdk.getWeather("NonExistentCity12345");
            } catch (WeatherSDKException e) {
                System.err.println("Expected error caught: " + e.getClass().getSimpleName());
                System.err.println("Error message: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Request interrupted: " + e.getMessage());
            }
            
            // Try with null city name
            try {
                sdk.getWeather(null);
            } catch (IllegalArgumentException e) {
                System.err.println("Expected validation error: " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Request interrupted: " + e.getMessage());
            }
            
        } catch (WeatherSDKException e) {
            System.err.println("SDK initialization error: " + e.getMessage());
        }
    }

    /**
     * Example 3: Fetching weather for multiple cities and demonstrating caching
     */
    private static void multipleCitiesExample(String apiKey) {
        System.out.println("\n=== Multiple Cities Example ===");
        
        // Create custom configuration with larger cache
        SDKConfiguration config;
        try {
            config = new WeatherSDKBuilder()
                    .apiKey(apiKey)
                    .mode(SDKMode.ON_DEMAND)
                    .cacheSize(10)  // Cache up to 10 cities
                    .cacheTTL(Duration.ofMinutes(10))  // Cache for 10 minutes
                    .build();
        } catch (ConfigurationException e) {
            System.err.println("Failed to create SDK configuration: " + e.getMessage());
            return;
        }
        
        try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {

            // Fetch weather for several cities
            String[] cities = {"London", "Paris", "New York", "Tokyo", "Sydney"};
            
            for (String city : cities) {
                try {
                    WeatherData weather = sdk.getWeather(city);
                    System.out.printf("  %s: %.1f°C, %s%n", 
                        weather.getName(), 
                        weather.getTemperatureCelsius(),
                        weather.getWeather() != null ? weather.getWeather().getMain() : "N/A");
                } catch (WeatherSDKException e) {
                    System.err.println("  " + city + ": Error - " + e.getMessage());
                }
            }
            
            // Fetch London again - this should use cached data
            System.out.println("\nFetching London again (should use cache):");
            WeatherData weather = sdk.getWeather("London");
            System.out.println("  " + weather.getName() + ": " + String.format("%.1f°C", weather.getTemperatureCelsius()));
            
            // Display SDK metrics
            System.out.println("\nSDK Performance Metrics:");
            SDKMetrics metrics = sdk.getMetrics();
            System.out.println("  Total requests: " + metrics.getTotalRequests());
            System.out.println("  Successful requests: " + metrics.getSuccessfulRequests());
            System.out.println("  Failed requests: " + metrics.getFailedRequests());
            System.out.println("  Cache hits: " + metrics.getCacheHits());
            System.out.println("  Cache misses: " + metrics.getCacheMisses());
            System.out.println("  Cache hit ratio: " + String.format("%.1f%%", metrics.getCacheHitRate()));
            System.out.println("  Average response time: " + String.format("%.2f ms", metrics.getAverageResponseTimeMs()));
            
        } catch (WeatherSDKException e) {
            System.err.println("SDK Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates a basic SDK configuration for the provided API key.
     */
    private static SDKConfiguration createConfiguration(String apiKey) {
        try {
            return new WeatherSDKBuilder()
                    .apiKey(apiKey)
                    .mode(SDKMode.ON_DEMAND)
                    .build();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Failed to create SDK configuration", e);
        }
    }
    
    /**
     * Example 4: Demonstrating that querying by city name returns the first match
     * from the OpenWeatherAPI. This shows how the API handles potentially ambiguous
     * city names by returning the most relevant/recent match.
     */
    private static void cityNameQueryExample(String apiKey) {
        System.out.println("\n=== City Name Query Example (Returns First Match) ===");
        
        try (WeatherSDK sdk = new DefaultWeatherSDK(createConfiguration(apiKey))) {
            
            // The SDK queries weather by city name and returns the first match from the API
            // This demonstrates the requirement: "Query weather by city name (returns first match)"
            String[] ambiguousCities = {"Paris", "London", "Tokyo"}; // These could have multiple matches globally
            
            for (String city : ambiguousCities) {
                try {
                    WeatherData weather = sdk.getWeather(city);
                    
                    // The API returns the first match for the city name
                    System.out.printf("Query: '%s' -> Result: '%s'%n",
                        city,
                        weather.getName());
                    System.out.printf("  Temperature: %.1f°C, Conditions: %s%n",
                        weather.getTemperatureCelsius(),
                        weather.getWeather() != null ? weather.getWeather().getMain() : "N/A");
                    
                    // Note: The SDK returns the first match from OpenWeatherAPI
                    System.out.println("  Note: OpenWeatherAPI returns the first match for ambiguous city names");
                    
                } catch (WeatherSDKException e) {
                    System.err.println(" " + city + ": Error - " + e.getMessage());
                }
            }
            
            // Example with a more specific query to show the behavior
            System.out.println("\nQuerying more specific locations:");
            String[] specificCities = {"Paris, France", "London, UK", "Tokyo, Japan"};
            
            for (String city : specificCities) {
                try {
                    WeatherData weather = sdk.getWeather(city);
                    
                    System.out.printf("Query: '%s' -> Result: '%s'%n",
                        city,
                        weather.getName());
                        
                } catch (WeatherSDKException e) {
                    System.err.println(" " + city + ": Error - " + e.getMessage());
                } catch (InterruptedException e) {
                    System.err.println(" " + city + ": Request interrupted - " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
            
        } catch (InterruptedException e) {
            System.err.println("Request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}