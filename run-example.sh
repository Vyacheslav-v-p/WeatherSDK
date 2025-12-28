#!/bin/bash

# Weather SDK Example Runner Script
# This script runs the minimal example with proper classpath setup

set -e

echo "🌤️  Running Weather SDK Minimal Example..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if dependencies are set up
if [ ! -f "weather-sdk-core/target/classes/com/weather/sdk/DefaultWeatherSDK.class" ]; then
    echo "❌ SDK not compiled. Please run ./setup-example.sh first"
    exit 1
fi

if [ ! -d "weather-sdk-core/target/lib" ] || [ -z "$(ls -A weather-sdk-core/target/lib/*.jar 2>/dev/null)" ]; then
    echo "❌ Dependencies not found. Please run ./setup-example.sh first"
    exit 1
fi

if [ ! -f "examples/java/com/weather/sdk/examples/MinimalWeatherSDKExample.class" ]; then
    echo "❌ Example not compiled. Please run ./setup-example.sh first"
    exit 1
fi

# Check for API key
API_KEY="${OPENWEATHER_API_KEY:-dummy-key}"

echo "🔑 Using API key: ${API_KEY:0:10}..."
echo ""

# Run the example
java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" \
     com.weather.sdk.examples.MinimalWeatherSDKExample

echo ""
echo "✅ Example completed successfully!"