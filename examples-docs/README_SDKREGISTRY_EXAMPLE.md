# Weather SDK Simple SDKRegistry Example
![Java 17+](https://img.shields.io/badge/java-17%2B-blue.svg)  
![License: CC BY-SA 4.0](https://img.shields.io/badge/license-CC%20BY--SA%204.0-lightgrey.svg)

This example demonstrates how to use the Weather SDK Registry for managing multiple WeatherSDK instances with different API keys, including basic failover strategies and registry management operations.

**Location**: The example source code is located at `examples/java/com/weather/sdk/examples/SimpleSDKRegistryExample.java`

## Quick Start

### Prerequisites

1. **Java 17+** - The SDK requires Java 17 or later
2. **Maven** - For building the SDK
3. **Two OpenWeatherAPI Keys** - Get free API keys from [OpenWeatherAPI](https://openweathermap.org/api)
   - Primary API key (for main operations)
   - Backup API key (for failover scenarios)

### Super Simple Setup (Recommended)

We've created an automated script to handle everything for you:

```bash
# 1. Set your API keys
export PRIMARY_WEATHER_API_KEY="your-primary-api-key-here"
export BACKUP_WEATHER_API_KEY="your-backup-api-key-here"

# 2. Run the setup script (compiles SDK, downloads dependencies, compiles example)
./setup-example.sh

# 3. Run the SDKRegistry example
./run-simple-sdkregistry-example.sh

# 4. Clean up build artifacts (optional)
./cleanup-example.sh
```

### Quick Test Without API Keys

To test the example structure and error handling:

```bash
# Test without API keys (will show validation errors)
./run-simple-sdkregistry-example.sh
```

### What the Scripts Do

#### `setup-example.sh`
- ✅ Compiles the weather-sdk-core module
- ✅ Downloads all required dependencies automatically
- ✅ Sets up proper classpath configuration
- ✅ Compiles the SDKRegistry example
- ✅ Handles missing dependencies gracefully

#### `run-simple-sdkregistry-example.sh`
- ✅ Validates that setup is complete
- ✅ Checks for required API key environment variables
- ✅ Runs the example with proper error handling
- ✅ Shows helpful messages if setup is needed
- ✅ Provides clear guidance for API key configuration

## What This Example Demonstrates

The `SimpleSDKRegistryExample.java` shows:

### 1. Multiple SDK Instance Management
- How to register multiple WeatherSDK instances with different API keys
- Different configuration strategies for different use cases
- Centralized management through the SDKRegistry

### 2. Configuration Diversity
- Primary SDK: Production-like configuration with polling mode
- Backup SDK: Conservative configuration with on-demand mode
- Different cache sizes, polling intervals, and TTL settings

### 3. Failover Strategy Implementation
- Automatic failover from primary to backup API key on network failures
- Graceful error handling and recovery
- Transparent switching between API keys

### 4. Registry Management Operations
- Creating SDK instances in the registry
- Retrieving specific instances by key
- Deleting individual instances
- Clearing the entire registry
- Monitoring registry state and metrics

### 5. Resource Management
- Proper cleanup of SDK instances
- Registry resource management
- Thread-safe operations

## Environment Variables Required

```bash
export PRIMARY_WEATHER_API_KEY="your-primary-api-key"
export BACKUP_WEATHER_API_KEY="your-backup-api-key"
```

## Example Output

When you run the example successfully, you should see output similar to:

```
=== Registering 2 SDK Instances ===
✓ Registered 2 SDK instances:
  - primary: Polling mode with 5-minute intervals
  - backup: On-demand mode for backup use

=== Failover Demonstration ===
Attempting to fetch weather from primary SDK...
✓ Successfully retrieved data from primary SDK
Weather data for London: 15.2°C, Clear
Source: primary API key

=== Registry Management ===
Current registry state:
  Total instances: 2
  Registered keys: [primary, backup]

Retrieving primary SDK instance...
✓ Successfully retrieved primary SDK
✓ Retrieved weather data: Paris
  Total requests: 2
  Cache hit rate: 50.0%

Deleting backup SDK instance...
✓ Successfully deleted backup SDK
✓ Confirmed: backup SDK is no longer accessible

Final registry state:
  Total instances: 1
  Remaining keys: [primary]

=== Cleaning up registry ===
Registry cleaned up successfully
```

## Key SDKRegistry Concepts

### 1. Registry Operations
The SDKRegistry provides static methods for managing multiple SDK instances:

```java
// Create and register SDK instances
WeatherSDK primarySdk = SDKRegistry.createSDKInstance("primary", primaryConfig);
WeatherSDK backupSdk = SDKRegistry.createSDKInstance("backup", backupConfig);

// Retrieve specific instances
Optional<WeatherSDK> sdk = SDKRegistry.getSDKInstance("primary");

// Manage registry
SDKRegistry.deleteSDKInstance("backup");
SDKRegistry.clearRegistry(); // Clean up all instances
```

### 2. Configuration Strategies

#### Primary SDK Configuration
```java
SDKConfiguration primaryConfig = new WeatherSDKBuilder()
    .apiKey(primaryApiKey)
    .mode(SDKMode.POLLING)                    // Automatic updates
    .pollingInterval(Duration.ofMinutes(5))    // Frequent updates
    .cacheSize(20)                            // Larger cache
    .cacheTTL(Duration.ofMinutes(10))         // Shorter TTL for freshness
    .connectionTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .build();
```

#### Backup SDK Configuration
```java
SDKConfiguration backupConfig = new WeatherSDKBuilder()
    .apiKey(backupApiKey)
    .mode(SDKMode.ON_DEMAND)                  // Fetch only when needed
    .cacheSize(10)                            // Smaller cache
    .cacheTTL(Duration.ofMinutes(15))         // Longer TTL to save API calls
    .connectionTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(10))
    .build();
```

### 3. Duplicate Prevention
The registry prevents duplicate API key registration:
```java
try {
    // This will throw ConfigurationException if "primary" key already exists
    WeatherSDK sdk = SDKRegistry.createSDKInstance("primary", config);
} catch (ConfigurationException e) {
    System.out.println("API key already registered: " + e.getMessage());
}
```

### 4. Resource Management
The registry properly manages SDK instance lifecycles:
```java
// When deleting a specific instance, it's properly shut down
SDKRegistry.deleteSDKInstance("my-key");

// When clearing the entire registry, all instances are shut down
SDKRegistry.clearRegistry();
```

### 5. Thread Safety
- All registry operations are thread-safe
- Multiple threads can safely access the registry
- SDK instances can be used concurrently
- Uses `ConcurrentHashMap` for safe concurrent operations
```java
try {
    // Try primary first
    WeatherData weather = primarySdk.getWeather(city);
    // Use primary data
} catch (NetworkException e) {
    // Fallback to backup on network failures
    WeatherData weather = backupSdk.getWeather(city);
    // Use backup data
}
```

### 4. Resource Management
The registry properly manages SDK instance lifecycles:
```java
// When deleting a specific instance, it's properly shut down
SDKRegistry.deleteSDKInstance("my-key");

// When clearing the entire registry, all instances are shut down
SDKRegistry.clearRegistry();
```

### 5. Thread Safety
- All registry operations are thread-safe
- Multiple threads can safely access the registry
- SDK instances can be used concurrently
- Uses `ConcurrentHashMap` for safe concurrent operations

## When to Use SDKRegistry

SDKRegistry is ideal for applications that:

- **Environment Separation**: Different API keys for development, staging, production
- **Service Tier Management**: Multiple API keys with different rate limits
- **Failover Strategy**: Primary and backup API keys for resilience
- **Usage Tracking**: Separate monitoring and analytics per API key
- **Load Distribution**: Distribute requests across multiple accounts

## Configuration Options

### Registry Operations
| Operation | Description | Return Type |
|-----------|-------------|-------------|
| `createSDKInstance(key, config)` | Create and register new SDK instance | `WeatherSDK` |
| `getSDKInstance(key)` | Retrieve SDK instance by key | `Optional<WeatherSDK>` |
| `deleteSDKInstance(key)` | Remove and shutdown SDK instance | `void` |
| `getRegistrySize()` | Get total number of registered instances | `int` |
| `getRegisteredApiKeys()` | Get set of all registered keys | `Set<String>` |
| `clearRegistry()` | Remove and shutdown all instances | `void` |

### SDK Configuration Differences

| Setting | Primary SDK | Backup SDK |
|---------|-------------|------------|
| Mode | POLLING | ON_DEMAND |
| Polling Interval | 5 minutes | N/A |
| Cache Size | 20 cities | 10 cities |
| Cache TTL | 10 minutes | 15 minutes |
| Connection Timeout | 5 seconds | 5 seconds |
| Read Timeout | 10 seconds | 10 seconds |
| Purpose | Active monitoring | Failover only |

## Troubleshooting

### Common Issues

1. **"PRIMARY_WEATHER_API_KEY environment variable not set"**
   - Make sure you've set both required environment variables
   - Verify your API keys are valid at OpenWeatherAPI

2. **"SDK not compiled"**
   - Run `./setup-example.sh` to compile the SDK and example
   - Ensure you're in the root project directory

3. **"Dependencies not found"**
   - The setup script will download dependencies automatically
   - Check your internet connection

4. **"Configuration error"**
   - Verify your API keys are valid
   - Check that configuration parameters are within acceptable ranges

### Getting Help

- Check the [SDK documentation](../README.md)
- Review the [minimal example](README_MINIMAL_EXAMPLE.md) for basic SDK usage
- Examine the [polling mode example](README_POLLING_MODE.md) for background updates


## Building with Maven

If you prefer using Maven to manage dependencies:

1. Build the SDK locally:
   ```bash
   mvn clean install -pl weather-sdk-core
   ```

2. Add the SDK as a dependency to your project's `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.weather</groupId>
       <artifactId>weather-sdk-core</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```

3. Compile and run the example manually:
   ```bash
   # Compile SDK
   mvn compile -pl weather-sdk-core
   
   # Compile example
   javac -cp "weather-sdk-core/target/classes:weather-sdk-core/target/lib/*" \
        examples/java/com/weather/sdk/examples/SimpleSDKRegistryExample.java
   
   # Run example
   java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" \
        com.weather.sdk.examples.SimpleSDKRegistryExample
   ```

## Contact

Weather SDK Documentation © 2025 by <vyacheslav.v.pl@yandex.ru> is licensed under CC BY-SA 4.0
Author: <vyacheslav.v.pl@yandex.ru> 
