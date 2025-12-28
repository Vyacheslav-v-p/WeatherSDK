#!/bin/bash

# Weather SDK Example Cleanup Script
# This script cleans up build artifacts and temporary files created by setup-example.sh

set -e

echo "🧹 Cleaning up Weather SDK build artifacts..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Remove the lib directory created by setup-example.sh
if [ -d "weather-sdk-core/target/lib" ]; then
    echo "🗑️ Removing dependency JARs..."
    rm -rf weather-sdk-core/target/lib
    echo "✅ Dependency JARs removed"
else
    echo "ℹ️  Dependency directory not found, skipping..."
fi

# Remove compiled example class files
EXAMPLE_CLASS_FILES=(
    "examples/java/com/weather/sdk/examples/MinimalWeatherSDKExample.class"
    "examples/java/com/weather/sdk/examples/PollingModeExample.class"
)

for class_file in "${EXAMPLE_CLASS_FILES[@]}"; do
    if [ -f "$class_file" ]; then
        echo "🗑️  Removing compiled example: $class_file"
        rm "$class_file"
        echo "✅ Removed $class_file"
    else
        echo "ℹ️  $class_file not found, skipping..."
    fi
done

# Clean up any other temporary build artifacts
echo "🧹 Cleaning up other build artifacts..."

# Remove any .class files in the examples directory
find examples/java -name "*.class" -type f -delete 2>/dev/null || true

# Clean up Maven target directories (if they exist)
if [ -d "weather-sdk-core/target" ]; then
    echo "🗑️ Removing weather-sdk-core target directory..."
    rm -rf weather-sdk-core/target
    echo "✅ Removed weather-sdk-core target directory"
fi

if [ -d "weather-sdk-ai-tools/target" ]; then
    echo "🗑️  Removing weather-sdk-ai-tools target directory..."
    rm -rf weather-sdk-ai-tools/target
    echo "✅ Removed weather-sdk-ai-tools target directory"
fi

if [ -d "weather-sdk-mcp/target" ]; then
    echo "🗑️  Removing weather-sdk-mcp target directory..."
    rm -rf weather-sdk-mcp/target
    echo "✅ Removed weather-sdk-mcp target directory"
fi

echo "✅ Cleanup complete!"
echo ""
echo "Directories and files removed:"
echo "- weather-sdk-core/target/lib (dependency JARs)"
echo "- weather-sdk-core/target (build artifacts)"
echo "- Compiled example .class files"
echo "- weather-sdk-ai-tools/target (build artifacts)"
echo "- weather-sdk-mcp/target (build artifacts)"
echo ""
echo "You can now run setup-example.sh again to perform a fresh setup."