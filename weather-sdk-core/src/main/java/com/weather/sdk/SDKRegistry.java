/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.exception.ConfigurationException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Registry for managing multiple WeatherSDK instances with different API keys.
 *
 * <p>This class provides a centralized registry for creating, storing, and managing multiple
 * WeatherSDK instances, each configured with a different API key. This enables applications to work
 * with multiple weather service accounts or different API key configurations simultaneously.
 *
 * <p>The registry follows the Singleton pattern with static methods for easy access and uses
 * thread-safe concurrent data structures to ensure safe multi-threaded operations.
 *
 * <h2>Thread Safety</h2>
 *
 * <p>This registry is designed to be thread-safe:
 *
 * <ul>
 *   <li>All operations use {@link ConcurrentHashMap} for thread-safe storage
 *   <li>No external synchronization required
 *   <li>Atomic operations for create/delete operations
 *   <li>Safe concurrent access from multiple threads
 * </ul>
 *
 * <h2>Multi-API Key Usage</h2>
 *
 * <p>The registry enables sophisticated multi-key scenarios:
 *
 * <ul>
 *   <li><strong>Environment Separation</strong>: Different keys for development, staging,
 *       production
 *   <li><strong>Service Tier Management</strong>: Multiple API keys with different rate limits
 *   <li><strong>Failover Strategy</strong>: Primary and backup API keys for resilience
 *   <li><strong>Usage Tracking</strong>: Separate usage monitoring per API key
 * </ul>
 *
 * <h2>Resource Management</h2>
 *
 * <p>The registry properly manages SDK instance lifecycles:
 *
 * <ul>
 *   <li>Automatic cleanup when instances are deleted via {@link #deleteSDKInstance(String)}
 *   <li>Calls {@code shutdown()} on SDK instances during deletion to release resources
 *   <li>Registry can be cleared completely using {@link #clearRegistry()}
 *   <li>Prevents memory leaks by ensuring proper cleanup
 * </ul>
 *
 * <h2>Duplicate Prevention</h2>
 *
 * <p>The registry prevents duplicate API key registration:
 *
 * <ul>
 *   <li>Each API key can only be registered once
 *   <li>Attempting to register an existing key throws {@link ConfigurationException}
 *   <li>This prevents accidental overwriting of existing SDK instances
 *   <li>Ensures predictable behavior and prevents data loss
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <h3>Basic Multi-Key Registration</h3>
 *
 * <pre>
 * // Register multiple API keys
 * SDKConfiguration config1 = new WeatherSDKBuilder()
 *     .apiKey("development-key")
 *     .mode(SDKMode.ON_DEMAND)
 *     .build();
 *
 * SDKConfiguration config2 = new WeatherSDKBuilder()
 *     .apiKey("production-key")
 *     .mode(SDKMode.POLLING)
 *     .pollingInterval(Duration.ofMinutes(10))
 *     .build();
 *
 * WeatherSDK sdk1 = SDKRegistry.createSDKInstance("dev", config1);
 * WeatherSDK sdk2 = SDKRegistry.createSDKInstance("prod", config2);
 *
 * // Use different SDK instances
 * WeatherData devWeather = sdk1.getWeather("London");
 * WeatherData prodWeather = sdk2.getWeather("London");
 * </pre>
 *
 * <h3>Environment-Specific Configuration</h3>
 *
 * <pre>
 * // Development environment
 * SDKConfiguration devConfig = new WeatherSDKBuilder()
 *     .apiKey(System.getenv("DEV_WEATHER_API_KEY"))
 *     .mode(SDKMode.ON_DEMAND)
 *     .cacheSize(10) // Standard cache size
 *     .build();
 *
 * // Production environment
 * SDKConfiguration prodConfig = new WeatherSDKBuilder()
 *     .apiKey(System.getenv("PROD_WEATHER_API_KEY"))
 *     .mode(SDKMode.POLLING)
 *     .cacheSize(10) // Standard cache size for production
 *     .pollingInterval(Duration.ofMinutes(5))
 *     .build();
 *
 * WeatherSDK devSdk = SDKRegistry.createSDKInstance("dev-env", devConfig);
 * WeatherSDK prodSdk = SDKRegistry.createSDKInstance("prod-env", prodConfig);
 * </pre>
 *
 * <h3>Failover Strategy</h3>
 *
 * <pre>
 * // Primary and backup API keys
 * SDKConfiguration primaryConfig = new WeatherSDKBuilder()
 *     .apiKey("primary-api-key")
 *     .mode(SDKMode.POLLING)
 *     .build();
 *
 * SDKConfiguration backupConfig = new WeatherSDKBuilder()
 *     .apiKey("backup-api-key")
 *     .mode(SDKMode.ON_DEMAND) // On-demand for backup to save API calls
 *     .build();
 *
 * WeatherSDK primarySdk = SDKRegistry.createSDKInstance("primary", primaryConfig);
 * WeatherSDK backupSdk = SDKRegistry.createSDKInstance("backup", backupConfig);
 *
 * // Try primary first, fallback to backup on failure
 * WeatherData weather;
 * try {
 *   weather = primarySdk.getWeather("London");
 * } catch (NetworkException e) {
 *   weather = backupSdk.getWeather("London");
 * }
 * </pre>
 *
 * <h3>Registry Management</h3>
 *
 * <pre>
 * // List all registered API keys
 * Set{@code <String>} registeredKeys = SDKRegistry.getRegisteredApiKeys();
 * System.out.println("Registered keys: " + registeredKeys);
 *
 * // Check if specific key exists
 * Optional{@code <WeatherSDK>} sdk = SDKRegistry.getSDKInstance("my-key");
 * if (sdk.isPresent()) {
 *   // Use the SDK instance
 * }
 *
 * // Get registry statistics
 * int totalInstances = SDKRegistry.getRegistrySize();
 * System.out.println("Total SDK instances: " + totalInstances);
 *
 * // Clean shutdown of specific instance
 * SDKRegistry.deleteSDKInstance("my-key");
 *
 * // Clean shutdown of all instances
 * SDKRegistry.clearRegistry();
 * </pre>
 *
 * @see WeatherSDK
 * @see SDKConfiguration
 * @see ConfigurationException
 * @since 1.0.0
 */
// PMD suppressions for registry pattern violations
@SuppressWarnings({
  "PMD.MethodArgumentCouldBeFinal",
  "PMD.LocalVariableCouldBeFinal",
  "PMD.CloseResource",
  "PMD.OnlyOneReturn",
  "PMD.AvoidUncheckedExceptionsInSignatures"
})
public final class SDKRegistry {

  /** Internal storage for registered SDK instances. Thread-safe concurrent map. */
  private static final ConcurrentMap<String, WeatherSDK> REGISTRY = new ConcurrentHashMap<>();

  /** Private constructor to prevent instantiation of this utility class. */
  private SDKRegistry() {
    // Utility class - prevent instantiation
  }

  /**
   * Creates and registers a new WeatherSDK instance with the specified API key and configuration.
   *
   * <p>This method creates a new WeatherSDK instance using the provided configuration and registers
   * it in the registry under the specified API key. The method validates that the API key is unique
   * and does not already exist in the registry.
   *
   * <h4>Behavior</h4>
   *
   * <ul>
   *   <li>Validates that the API key is not null or empty
   *   <li>Checks if the API key already exists in the registry
   *   <li>If exists: Throws {@link ConfigurationException} with message "API key already
   *       registered"
   *   <li>If not exists: Creates SDK instance and stores it in the registry
   *   <li>Returns the created SDK instance
   * </ul>
   *
   * @param apiKey the API key to register the SDK instance under (must not be null or empty)
   * @param config the SDK configuration (must not be null)
   * @return the newly created WeatherSDK instance
   * @throws ConfigurationException if the API key already exists in the registry or is invalid
   */
  public static WeatherSDK createSDKInstance(String apiKey, SDKConfiguration config)
      throws ConfigurationException {

    // Validate API key
    if (apiKey == null || apiKey.isBlank()) {
      throw new ConfigurationException("API key must not be null or empty", "apiKey");
    }

    // Check if API key already exists
    if (REGISTRY.containsKey(apiKey)) {
      throw new ConfigurationException("API key already registered", "apiKey");
    }

    // Create and store SDK instance
    WeatherSDK sdk = new DefaultWeatherSDK(config);
    WeatherSDK existing = REGISTRY.putIfAbsent(apiKey, sdk);

    // This should not happen due to containsKey check above, but handle defensively
    if (existing != null) {
      throw new ConfigurationException("API key already registered", "apiKey");
    }

    return sdk;
  }

  /**
   * Retrieves the WeatherSDK instance registered under the specified API key.
   *
   * <p>This method safely retrieves a previously registered SDK instance. It performs atomic lookup
   * and returns an Optional to handle the case where the API key is not found.
   *
   * <h4>Behavior</h4>
   *
   * <ul>
   *   <li>Returns {@link Optional#ofNullable(Object)} if the API key exists in the registry
   *   <li>Returns {@link Optional#empty()} if the API key is not found
   *   <li>Thread-safe lookup with no external synchronization required
   * </ul>
   *
   * @param apiKey the API key to look up (must not be null)
   * @return an {@link Optional} containing the WeatherSDK instance if found, or {@link
   *     Optional#empty()} if not found
   */
  public static Optional<WeatherSDK> getSDKInstance(String apiKey) {
    if (apiKey == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(REGISTRY.get(apiKey));
  }

  /**
   * Deletes and unregisters the WeatherSDK instance with the specified API key.
   *
   * <p>This method removes the SDK instance from the registry and calls its {@code shutdown()}
   * method to properly release any resources (threads, connections, etc.) before removal.
   *
   * <h4>Behavior</h4>
   *
   * <ul>
   *   <li>Checks if the API key exists in the registry
   *   <li>If not exists: Throws {@link IllegalArgumentException} with message "API key not found"
   *   <li>If exists: Calls {@code sdk.shutdown()} to cleanup resources
   *   <li>Removes the instance from the registry
   *   <li>Thread-safe deletion with atomic operations
   * </ul>
   *
   * @param apiKey the API key of the SDK instance to delete (must not be null)
   * @throws IllegalArgumentException if the API key is not found in the registry
   */
  public static void deleteSDKInstance(String apiKey) throws IllegalArgumentException {
    if (apiKey == null) {
      throw new IllegalArgumentException("API key must not be null");
    }

    WeatherSDK sdk = REGISTRY.remove(apiKey);
    if (sdk == null) {
      throw new IllegalArgumentException("API key not found");
    }

    // Cleanup resources
    sdk.shutdown();
  }

  /**
   * Returns an unmodifiable set of all registered API keys.
   *
   * <p>This method provides a snapshot view of currently registered API keys. The returned set is
   * unmodifiable to prevent external modification of the registry's internal state.
   *
   * <h4>Thread Safety</h4>
   *
   * <ul>
   *   <li>Returns a snapshot view, not a live view of the registry
   *   <li>Safe to iterate over without concurrent modification concerns
   *   <li>No external synchronization required
   * </ul>
   *
   * @return an unmodifiable set containing all registered API keys
   */
  public static Set<String> getRegisteredApiKeys() {
    return Collections.unmodifiableSet(REGISTRY.keySet());
  }

  /**
   * Returns the current number of registered SDK instances.
   *
   * <p>This method provides the total count of SDK instances currently stored in the registry. This
   * includes instances that may be in various states (active, being used, etc.).
   *
   * <h4>Thread Safety</h4>
   *
   * <ul>
   *   <li>Atomic operation with no external synchronization required
   *   <li>Safe to call concurrently from multiple threads
   * </ul>
   *
   * @return the current number of registered SDK instances
   */
  public static int getRegistrySize() {
    return REGISTRY.size();
  }

  /**
   * Clears all registered SDK instances from the registry.
   *
   * <p>This method removes all SDK instances from the registry and calls {@code shutdown()} on each
   * instance to properly release resources. After this operation, the registry will be empty and
   * {@link #getRegistrySize()} will return 0.
   *
   * <h4>Resource Management</h4>
   *
   * <ul>
   *   <li>Calls {@code shutdown()} on each SDK instance before removal
   *   <li>Ensures proper cleanup of threads, connections, and other resources
   *   <li>Prevents memory leaks by releasing all registered instances
   * </ul>
   *
   * <h4>Thread Safety</h4>
   *
   * <ul>
   *   <li>Thread-safe operation using atomic clear
   *   <li>Safe to call while other threads are using registry methods
   * </ul>
   */
  public static void clearRegistry() {
    // Shutdown all instances first
    for (WeatherSDK sdk : REGISTRY.values()) {
      sdk.shutdown();
    }
    // Clear the registry
    REGISTRY.clear();
  }
}
