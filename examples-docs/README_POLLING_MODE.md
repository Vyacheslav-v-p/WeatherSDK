# Weather SDK Polling Mode Example
![Java 17+](https://img.shields.io/badge/java-17%2B-blue.svg)  
![License: CC BY-SA 4.0](https://img.shields.io/badge/license-CC%20BY--SA%204.0-lightgrey.svg)

This document provides a simple example of how to use the Weather SDK in polling mode.

## What is Polling Mode?

Polling mode is a feature of the Weather SDK that automatically updates weather data for cached cities at regular intervals. Instead of making API calls every time you request weather data, the SDK keeps the cache fresh by periodically updating the weather information in the background.

## Simple Example

Here's a basic example showing how to use the polling mode:

```java
import com.weather.sdk.WeatherSDK;
import com.weather.sdk.DefaultWeatherSDK;
import com.weather.sdk.config.SDKMode;
import com.weather.sdk.config.SDKConfiguration;
import com.weather.sdk.config.WeatherSDKBuilder;
import com.weather.sdk.model.WeatherData;
import java.time.Duration;

public class SimplePollingExample {
  public static void main(String[] args) throws Exception {
    // Create SDK configuration for polling mode
    SDKConfiguration config = new WeatherSDKBuilder()
        .apiKey("your-api-key-here")
        .mode(SDKMode.POLLING)                    // Enable polling mode
        .pollingInterval(Duration.ofMinutes(1))    // Update every 1 minute
        .cacheSize(10)                            // Cache up to 10 cities
        .cacheTTL(Duration.ofMinutes(10))         // Cache entries expire after 10 minutes
        .connectionTimeout(Duration.ofSeconds(5))  // Connection timeout
        .readTimeout(Duration.ofSeconds(10))       // Read timeout
        .maxRetries(3)                           // Maximum retry attempts
        .retryDelayMs(50L)                      // Delay between retries in milliseconds
        .build();

    // Create the WeatherSDK instance with the configuration
    WeatherSDK sdk = new DefaultWeatherSDK(config);

    // Add a city to the cache - it will be automatically updated by polling
    WeatherData weather = sdk.getWeather("London");
    System.out.println("Current London temperature: " + String.format("%.1f°C", weather.getTemperatureCelsius()));

    // The SDK will continue updating weather data in the background
    // Future calls to getWeather("London") will return cached data that's
    // regularly updated by the polling mechanism

    // Don't forget to shutdown when done
    sdk.shutdown();
  }
}
```

## Key Features of Polling Mode

- **Automatic Updates**: Weather data for cached cities is updated automatically at configured intervals
- **Reduced API Calls**: Minimizes direct API calls by using cached data that's kept fresh
- **Background Processing**: Updates happen in the background without blocking your application
- **Configurable Intervals**: Customize how often the SDK polls for updates
- **Resource Management**: Properly shutdown polling when no longer needed

## Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `apiKey` | OpenWeatherAPI key (required) | - |
| `mode` | Operation mode (ON_DEMAND or POLLING) | ON_DEMAND |
| `pollingInterval` | How often to update cached weather data (POLLING mode only) | 5 minutes |
| `cacheSize` | Maximum number of cities to cache | 10 cities |
| `cacheTTL` | How long cached data remains valid | 10 minutes |
| `connectionTimeout` | HTTP connection timeout | 5 seconds |
| `readTimeout` | HTTP read timeout | 10 seconds |
| `maxRetries` | Maximum number of retry attempts for failed API calls | 3 |
| `retryDelayMs` | Delay between retry attempts in milliseconds | 500ms |

## When to Use Polling Mode

Polling mode is ideal for applications that:
- Need regularly updated weather information
- Want to minimize API calls
- Have a known set of cities to monitor
- Can tolerate slightly delayed weather data (based on polling interval)

## How to Run the Polling Mode Example

We've created an automated script to handle everything for you:

```bash
# 1. Set your API key
export OPENWEATHER_API_KEY="your-actual-api-key-here"

# 2. Run the example (the script handles compilation and execution)
./run-polling-example.sh
```

Alternatively, you can run it manually:

1. **Set up your API key**:
   ```bash
   export OPENWEATHER_API_KEY="your-actual-api-key-here"
   ```

2. **Compile the example**:
   ```bash
   mvn compile
   ```

3. **Run the example**:
   ```bash
   java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" \
        com.weather.sdk.examples.PollingModeExample
   ```

4. **Expected Output**:
   ```
   Adding cities to cache...
   London temperature: 12.5°C
   Paris temperature: 10.1°C
   
   The SDK is now automatically polling for updates!
   Weather data for London and Paris will be refreshed every 1 minute.
   Press Ctrl+C to stop the example.
   
   Updated London temperature: 12.5°C
   
   SDK Metrics:
   Total API requests: 2
   Cache hits: 1
   Cache misses: 2
   Cache hit rate: 33.3333336%
   ```

## Thread Safety

The Weather SDK is fully thread-safe, so you can safely use it from multiple threads even in polling mode.

### Getting Help

- Check the [SDK documentation](../README.md)
- Review the [minimal example](README_MINIMAL_EXAMPLE.md) for basic SDK usage

## Contact

Weather SDK Documentation © 2025 by <vyacheslav.v.pl@yandex.ru> is licensed under CC BY-SA 4.0 
Author: <vyacheslav.v.pl@yandex.ru> 
