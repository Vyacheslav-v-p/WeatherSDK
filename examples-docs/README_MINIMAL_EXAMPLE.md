# Minimal Weather SDK Working Example
![Java 17+](https://img.shields.io/badge/java-17%2B-blue.svg)  
![License: CC BY-SA 4.0](https://img.shields.io/badge/license-CC%20BY--SA%204.0-lightgrey.svg)

This example demonstrates how to use the Weather SDK to fetch weather data from OpenWeatherAPI.

**Location**: The example source code is located at `examples/java/com/weather/sdk/examples/MinimalWeatherSDKExample.java`

## Quick Start

### Prerequisites

1. **Java 17+** - The SDK requires Java 17 or later
2. **Maven** - For building the SDK
3. **OpenWeatherAPI Key** - Get a free API key from [OpenWeatherAPI](https://openweathermap.org/api) (optional for testing)

### Super Simple Setup (Recommended)

Automated scripts handle everything for you.

```bash
# 1. Run the setup script (compiles SDK, downloads dependencies, compiles example)
./setup-example.sh

# 2. Run the example
./run-example.sh

# 3. Clean up build artifacts (optional)
./cleanup-example.sh
```

### With Your API Key

To use real weather data:

```bash
# 1. Set your API key
export OPENWEATHER_API_KEY="your-actual-api-key-here"

# 2. Setup and run
./setup-example.sh && ./run-example.sh
```

### Quick Test Without API Key

To test the example structure and error handling:

```bash
# Test with dummy API key
OPENWEATHER_API_KEY="dummy-key" ./run-example.sh
```

### What the Scripts Do

#### `setup-example.sh`
- ✅ Compiles the weather-sdk-core module
- ✅ Downloads all required dependencies automatically
- ✅ Sets up proper classpath configuration
- ✅ Compiles the example
- ✅ Handles missing dependencies gracefully

#### `run-example.sh`
- ✅ Validates that setup is complete
- ✅ Runs the example with proper error handling
- ✅ Shows helpful messages if setup is needed
- ✅ Supports both real and dummy API keys


### Troubleshooting

**If setup fails:**
1. Ensure you're in the root project directory (where `pom.xml` is located)
2. Make sure you have Java 17+ and Maven installed
3. Check that your internet connection is working (for downloading dependencies)

**If scripts fail:**
The scripts will automatically try multiple approaches to find dependencies. If they still fail, the scripts will give you helpful error messages about what to check.

## What This Example Demonstrates

The `MinimalWeatherSDKExample.java` shows:

### 1. Basic SDK Configuration and Usage
- How to create an SDK instance using the builder pattern
- Proper resource management with try-with-resources
- Fetching weather data for a single city

### 2. Working with Weather Data
- Accessing temperature data (current and feels-like in Celsius)
- Getting weather conditions (clear, rain, etc.)
- Accessing wind and visibility information
- Checking data freshness

### 3. Error Handling
- Handling invalid cities
- Handling network errors
- Input validation

### 4. Multiple Cities and Caching
- Fetching weather for multiple cities
- Demonstrating caching behavior
- Performance metrics and statistics

### 5. SDK Metrics
- Cache hit/miss ratios
- Response times
- Success/failure rates

## Example Output

When you run the example successfully, you should see output similar to:

```
=== Basic Weather Example ===
Weather for London:
  Temperature: 21.0°C
  Feels like: 20.0°C
  Conditions: Clear
  Description: clear sky
  Visibility: 10.0 km
  Wind speed: 3.6 m/s
  Wind direction: NE
  Data freshness: Recent (within last hour)

=== Error Handling Example ===
Expected error caught: CityNotFoundException
Error message: City 'NonExistentCity12345' not found
Expected validation error: City name must not be null or empty

=== Multiple Cities Example ===
  London: 21.0°C, Clear
  Paris: 19.5°C, Clouds
  New York: 15.2°C, Rain
  Tokyo: 25.8°C, Clear
  Sydney: 22.3°C, Clear

Fetching London again (should use cache):
  London: 21.0°C

SDK Performance Metrics:
  Total requests: 6
  Successful requests: 5
  Failed requests: 1
  Cache hits: 1
  Cache misses: 5
  Cache hit ratio: 16.7%
  Average response time: 1254.32 ms
```

## Key SDK Concepts

### 1. Builder Pattern
The SDK uses a configuration builder pattern:

```java
// Step 1: Build configuration
SDKConfiguration config = new WeatherSDKBuilder()
        .apiKey(apiKey)
        .mode(SDKMode.ON_DEMAND)
        .cacheSize(10)
        .cacheTTL(Duration.ofMinutes(10))
        .build();

// Step 2: Create SDK instance
try (WeatherSDK sdk = new DefaultWeatherSDK(config)) {
    // Use the SDK
}
```

### 2. Operation Modes
- **ON_DEMAND** (default): Fetch weather data only when requested
- **POLLING**: Automatically fetch data at regular intervals

### 3. Caching
- Intelligent caching reduces API calls
- Configurable cache size and time-to-live
- LRU (Least Recently Used) eviction

### 4. Thread Safety
- All SDK operations are thread-safe
- Can be used from multiple threads simultaneously

### 5. Resource Management
- Implements `AutoCloseable` for try-with-resources
- Automatic cleanup of HTTP connections
- Graceful shutdown

## Additional Examples

The project includes additional examples for different use cases:

### Polling Mode Example
Demonstrates automatic background updates:
```bash
export OPENWEATHER_API_KEY="your-api-key"
./run-polling-example.sh
```

### SDK Registry Example
Shows how to manage multiple SDK instances with different API keys:
```bash
export PRIMARY_WEATHER_API_KEY="your-primary-key"
export BACKUP_WEATHER_API_KEY="your-backup-key"
./run-simple-sdkregistry-example.sh
```

## Next Steps

1. **Explore the Full API**: Check out the SDK interface and configuration options
2. **Try Different Modes**: Experiment with POLLING mode for real-time updates
3. **Custom Configuration**: Adjust cache settings, timeouts, and other parameters
4. **Error Handling**: Implement proper error handling in your applications
5. **Metrics Monitoring**: Use SDK metrics to monitor performance and costs

## Troubleshooting

### Common Issues

1. **"API key must not be null or empty"**
   - Make sure you've set the `OPENWEATHER_API_KEY` environment variable
   - Verify your API key is valid at OpenWeatherAPI

2. **"City not found"**
   - Check the city name spelling
   - Try using more specific city names (e.g., "New York,US" instead of just "New York")

3. **Network timeouts**
   - Check your internet connection
   - The SDK has default timeouts, but you can adjust them in configuration

4. **Class not found errors**
   - Make sure you've compiled the SDK: `mvn clean compile -pl weather-sdk-core`
   - Verify the classpath includes the SDK classes

### Getting Help

- Check the [SDK documentation](../README.md)


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

3. Use Maven to compile and run your application with the SDK dependency

**Note**: The example code itself is standalone and doesn't require Maven integration for basic usage.

## Contact

Weather SDK Documentation © 2025 by <vyacheslav.v.pl@yandex.ru> is licensed under CC BY-SA 4.0 
Author: <vyacheslav.v.pl@yandex.ru> 
