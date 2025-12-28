#!/bin/bash

# Weather SDK Simple SDKRegistry Example Runner Script
# This script runs the SimpleSDKRegistryExample with proper classpath setup

set -e

echo "🌤️  Running Weather SDK Simple SDKRegistry Example..."

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

if [ ! -f "examples/java/com/weather/sdk/examples/SimpleSDKRegistryExample.class" ]; then
    echo "❌ Example not compiled. Please run ./setup-example.sh first"
    exit 1
fi

# Check for required API keys
PRIMARY_KEY="${PRIMARY_WEATHER_API_KEY:-}"
BACKUP_KEY="${BACKUP_WEATHER_API_KEY:-}"

if [ -z "$PRIMARY_KEY" ]; then
    echo "❌ PRIMARY_WEATHER_API_KEY environment variable not set"
    echo "Please set your API keys:"
    echo "  export PRIMARY_WEATHER_API_KEY=\"your-primary-api-key\""
    echo "  export BACKUP_WEATHER_API_KEY=\"your-backup-api-key\""
    echo ""
    echo "Get free API keys at: https://openweathermap.org/api"
    exit 1
fi

if [ -z "$BACKUP_KEY" ]; then
    echo "❌ BACKUP_WEATHER_API_KEY environment variable not set"
    echo "Please set your API keys:"
    echo "  export PRIMARY_WEATHER_API_KEY=\"your-primary-api-key\""
    echo "  export BACKUP_WEATHER_API_KEY=\"your-backup-api-key\""
    echo ""
    echo "Get free API keys at: https://openweathermap.org/api"
    exit 1
fi

echo "🔑 Using API keys:"
echo "  Primary: ${PRIMARY_KEY:0:10}..."
echo "  Backup: ${BACKUP_KEY:0:10}..."
echo ""

# Run the example
java -cp ".:weather-sdk-core/target/classes:weather-sdk-core/target/lib/*:examples/java" \
     com.weather.sdk.examples.SimpleSDKRegistryExample

echo ""
echo "✅ Example completed successfully!"