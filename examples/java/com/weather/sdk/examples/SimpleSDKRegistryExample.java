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
import com.weather.sdk.SDKRegistry;
import com.weather.sdk.WeatherSDK;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.exception.ConfigurationException;
import com.weather.sdk.exception.WeatherSDKException;
import com.weather.sdk.exception.NetworkException;
import com.weather.sdk.model.WeatherData;
import java.time.Duration;
import java.util.Optional;

/**
 * Simple SDKRegistry Example for 2 API Keys
 * 
 * This example demonstrates basic usage of the Weather SDK Registry for managing
 * multiple WeatherSDK instances with different API keys. It covers:
 * 
 * - Registering 2 SDK instances with different API keys
 * - Basic configuration differences between instances
 * - Simple failover strategy between primary and backup keys
 * - Registry management and cleanup
 * 
 * Prerequisites:
 * - You need 2 valid OpenWeatherAPI keys (get them free at https://openweathermap.org/api)
 * - Add the API keys to your environment variables
 * 
 * Environment Variables Required:
 * - PRIMARY_WEATHER_API_KEY: Primary API key
 * - BACKUP_WEATHER_API_KEY: Backup API key
 * 
 * How to run:
 * 1. Set environment variables: 
 *    export PRIMARY_WEATHER_API_KEY="your-primary-key"
 *    export BACKUP_WEATHER_API_KEY="your-backup-key"
 * 2. Compile: javac -cp "weather-sdk-core/target/classes:weather-sdk-core/target/lib/*" examples/java/com/weather/sdk/examples/SimpleSDKRegistryExample.java
 * 3. Run: java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" com.weather.sdk.examples.SimpleSDKRegistryExample
 */
public class SimpleSDKRegistryExample {

    public static void main(String[] args) {
        // Get API keys from environment variables
        String primaryApiKey = System.getenv("PRIMARY_WEATHER_API_KEY");
        String backupApiKey = System.getenv("BACKUP_WEATHER_API_KEY");
        
        if (primaryApiKey == null || primaryApiKey.trim().isEmpty() ||
            backupApiKey == null || backupApiKey.trim().isEmpty()) {
            System.err.println("Error: Please set both PRIMARY_WEATHER_API_KEY and BACKUP_WEATHER_API_KEY environment variables");
            System.err.println("Get your free API keys at: https://openweathermap.org/api");
            System.exit(1);
        }

        try {
            // Example 1: Register 2 SDK instances with different configurations
            registerTwoSdkInstances(primaryApiKey, backupApiKey);
            
            // Example 2: Use both instances and demonstrate failover
            demonstrateFailover(primaryApiKey, backupApiKey);
            
            // Example 3: Registry management
            demonstrateRegistryManagement();
            
        } finally {
            // Always cleanup registry resources
            System.out.println("\n=== Cleaning up registry ===");
            SDKRegistry.clearRegistry();
            System.out.println("Registry cleaned up successfully");
        }
    }

    /**
     * Example 1: Register 2 SDK instances with different configurations
     */
    private static void registerTwoSdkInstances(String primaryApiKey, String backupApiKey) {
        System.out.println("=== Registering 2 SDK Instances ===");
        
        try {
            // Primary SDK: Production-like configuration with polling
            SDKConfiguration primaryConfig = new WeatherSDKBuilder()
                    .apiKey(primaryApiKey)
                    .mode(SDKMode.POLLING)
                    .pollingInterval(Duration.ofMinutes(5))  // 5-minute polling
                    .cacheSize(20)  // Cache for 20 cities
                    .cacheTTL(Duration.ofMinutes(10))  // Cache data for 10 minutes
                    .connectionTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(10))
                    .build();
            
            // Backup SDK: Conservative configuration with on-demand mode
            SDKConfiguration backupConfig = new WeatherSDKBuilder()
                    .apiKey(backupApiKey)
                    .mode(SDKMode.ON_DEMAND)  // Only fetch when requested
                    .cacheSize(10)  // Smaller cache
                    .cacheTTL(Duration.ofMinutes(15))  // Longer cache time
                    .connectionTimeout(Duration.ofSeconds(5))
                    .readTimeout(Duration.ofSeconds(10))
                    .build();
            
            // Register both instances in the registry
            WeatherSDK primarySdk = SDKRegistry.createSDKInstance("primary", primaryConfig);
            WeatherSDK backupSdk = SDKRegistry.createSDKInstance("backup", backupConfig);
            
            System.out.println("✓ Registered 2 SDK instances:");
            System.out.println("  - primary: Polling mode with 5-minute intervals");
            System.out.println("  - backup: On-demand mode for backup use");
            
        } catch (ConfigurationException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.err.println("Suggested fix: " + e.getSuggestedFix());
        }
    }

    /**
     * Example 2: Use both instances and demonstrate simple failover
     */
    private static void demonstrateFailover(String primaryApiKey, String backupApiKey) {
        System.out.println("\n=== Failover Demonstration ===");
        
        String city = "London";
        
        try {
            // Try primary first
            Optional<WeatherSDK> primaryOpt = SDKRegistry.getSDKInstance("primary");
            if (!primaryOpt.isPresent()) {
                System.err.println("Primary SDK not found in registry");
                return;
            }
            
            WeatherData weather = null;
            boolean usedBackup = false;
            
            try {
                System.out.println("Attempting to fetch weather from primary SDK...");
                weather = primaryOpt.get().getWeather(city);
                System.out.println("✓ Successfully retrieved data from primary SDK");
                
            } catch (NetworkException e) {
                System.out.println("Primary SDK failed: " + e.getMessage());
                System.out.println("Switching to backup SDK...");
                
                // Fallback to backup
                Optional<WeatherSDK> backupOpt = SDKRegistry.getSDKInstance("backup");
                if (backupOpt.isPresent()) {
                    weather = backupOpt.get().getWeather(city);
                    usedBackup = true;
                    System.out.println("✓ Successfully retrieved data from backup SDK");
                } else {
                    System.err.println("Backup SDK not found in registry");
                    return;
                }
            }
            
            if (weather != null) {
                System.out.printf("Weather data for %s: %.1f°C, %s%n",
                    city,
                    weather.getTemperatureCelsius(),
                    weather.getWeather() != null ? weather.getWeather().getMain() : "N/A");
                System.out.println("Source: " + (usedBackup ? "backup API key" : "primary API key"));
            }
            
        } catch (WeatherSDKException e) {
            System.err.println("Weather API Error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Example 3: Registry management operations
     */
    private static void demonstrateRegistryManagement() {
        System.out.println("\n=== Registry Management ===");
        
        // Show current registry state
        System.out.println("Current registry state:");
        System.out.println("  Total instances: " + SDKRegistry.getRegistrySize());
        System.out.println("  Registered keys: " + SDKRegistry.getRegisteredApiKeys());
        
        // Demonstrate retrieving a specific instance
        System.out.println("\nRetrieving primary SDK instance...");
        Optional<WeatherSDK> primarySdk = SDKRegistry.getSDKInstance("primary");
        if (primarySdk.isPresent()) {
            System.out.println("✓ Successfully retrieved primary SDK");
            
            // Use the retrieved SDK to show it works
            try {
                WeatherData weather = primarySdk.get().getWeather("Paris");
                System.out.println("✓ Retrieved weather data: " + weather.getName());
                
                // Show metrics
                SDKMetrics metrics = primarySdk.get().getMetrics();
                System.out.println("  Total requests: " + metrics.getTotalRequests());
                System.out.println("  Cache hit rate: " + String.format("%.1f%%", metrics.getCacheHitRate()));
                
            } catch (WeatherSDKException e) {
                System.err.println("Error fetching weather: " + e.getMessage());
            } catch (InterruptedException e) {
                System.err.println("Request interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        } else {
            System.err.println("Failed to retrieve primary SDK");
        }
        
        // Demonstrate deleting a specific instance
        System.out.println("\nDeleting backup SDK instance...");
        try {
            SDKRegistry.deleteSDKInstance("backup");
            System.out.println("✓ Successfully deleted backup SDK");
            
            // Verify it's gone
            Optional<WeatherSDK> deletedSdk = SDKRegistry.getSDKInstance("backup");
            if (!deletedSdk.isPresent()) {
                System.out.println("✓ Confirmed: backup SDK is no longer accessible");
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to delete backup SDK: " + e.getMessage());
        }
        
        // Show final registry state
        System.out.println("\nFinal registry state:");
        System.out.println("  Total instances: " + SDKRegistry.getRegistrySize());
        System.out.println("  Remaining keys: " + SDKRegistry.getRegisteredApiKeys());
    }
}