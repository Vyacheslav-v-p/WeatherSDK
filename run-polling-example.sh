#!/bin/bash

# Weather SDK Polling Mode Example Runner Script
# This script runs the polling mode example with proper classpath setup

set -e

echo "🌤️  Running Weather SDK Polling Mode Example..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if SDK is compiled
if [ ! -f "weather-sdk-core/target/classes/com/weather/sdk/DefaultWeatherSDK.class" ]; then
    echo "❌ SDK not compiled. Please run './setup-example.sh' first"
    exit 1
fi

# Check if dependencies are set up
if [ ! -d "weather-sdk-core/target/lib" ] || [ -z "$(ls -A weather-sdk-core/target/lib/*.jar 2>/dev/null)" ]; then
    echo "❌ Dependencies not found. Please run './setup-example.sh' first"
    exit 1
fi

# Check for API key
API_KEY="${OPENWEATHER_API_KEY:-dummy-key}"

echo "🔑 Using API key: ${API_KEY:0:10}..."
echo ""

# Compile the example if not already compiled
if [ ! -f "examples/java/com/weather/sdk/examples/PollingModeExample.class" ]; then
    echo "📦 Compiling example..."
    javac -cp "weather-sdk-core/target/classes:weather-sdk-core/target/lib/*" \
          -d examples/java \
          examples/java/com/weather/sdk/examples/PollingModeExample.java

    if [ $? -ne 0 ]; then
        echo "❌ Failed to compile example. Please check the source code."
        exit 1
    fi
else
    echo "📦 Example already compiled"
fi

# Run the example
echo "🏃 Running polling mode example..."
java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" \
     com.weather.sdk.examples.PollingModeExample

echo ""
echo "✅ Polling mode example completed successfully!"