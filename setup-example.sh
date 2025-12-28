#!/bin/bash

# Weather SDK Example Setup Script
# This script automatically sets up dependencies and compiles the example

set -e

echo "🌤️  Setting up Weather SDK Example..."

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Step 1: Compile the SDK
echo "📦 Compiling Weather SDK..."
cd weather-sdk-core && mvn clean compile -q

# Step 2: Create lib directory and copy dependencies
echo "📚 Setting up dependencies..."
mkdir -p target/lib

# Use Maven to copy dependencies (this should work regardless of local Maven repository structure)
echo "📦 Copying dependencies via Maven..."
mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q

# Verify dependencies
echo "📋 Checking dependencies..."
if [ -d "target/lib" ] && [ -n "$(ls -A target/lib/*.jar 2>/dev/null)" ]; then
    echo "✅ Dependencies downloaded via Maven:"
    JAR_COUNT=$(ls -la target/lib/*.jar 2>/dev/null | wc -l)
    echo "  Found $JAR_COUNT JAR files"
    echo "  Total size: $(du -sh target/lib 2>/dev/null | cut -f1)"
else
    echo "⚠️  Maven dependency copy didn't work, trying to locate dependencies manually..."
    
    # Find Jackson and SLF4J JARs in Maven repository (excluding sources)
    M2_REPO="${HOME}/.m2/repository"
    if [ -d "$M2_REPO" ]; then
        
        # Find and copy Jackson databind JAR (not sources)
        databind_jar=$(find "$M2_REPO" -name "jackson-databind-*.jar" -type f 2>/dev/null | grep -v "sources" | head -1)
        if [ -n "$databind_jar" ]; then
            cp "$databind_jar" target/lib/
            echo "  Copied: $(basename "$databind_jar")"
        else
            echo "  ⚠️  Could not find jackson-databind"
        fi
        
        # Find and copy Jackson core JAR
        core_jar=$(find "$M2_REPO" -name "jackson-core-*.jar" -type f 2>/dev/null | grep -v "sources" | head -1)
        if [ -n "$core_jar" ]; then
            cp "$core_jar" target/lib/
            echo "  Copied: $(basename "$core_jar")"
        else
            echo "  ⚠️  Could not find jackson-core"
        fi
        
        # Find and copy Jackson annotations JAR (not sources)
        annotations_jar=$(find "$M2_REPO" -name "jackson-annotations-*.jar" -type f 2>/dev/null | grep -v "sources" | head -1)
        if [ -n "$annotations_jar" ]; then
            cp "$annotations_jar" target/lib/
            echo "  Copied: $(basename "$annotations_jar")"
        else
            echo "  ⚠️  Could not find jackson-annotations"
        fi
        
        # Find and copy SLF4J API JAR (prefer version 2.0.9 if available)
        slf4j_jar=$(find "$M2_REPO" -name "slf4j-api-2.0.9.jar" -type f 2>/dev/null | head -1)
        if [ -z "$slf4j_jar" ]; then
            slf4j_jar=$(find "$M2_REPO" -name "slf4j-api-*.jar" -type f 2>/dev/null | grep -v "sources" | head -1)
        fi
        
        if [ -n "$slf4j_jar" ]; then
            cp "$slf4j_jar" target/lib/
            echo "  Copied: $(basename "$slf4j_jar")"
        else
            echo "  ⚠️  Could not find SLF4J API"
        fi
        
    else
        echo "❌ Maven repository not found at $M2_REPO"
        echo "   Please ensure Maven is installed and dependencies are resolved"
        exit 1
    fi
fi

# Final verification
FINAL_JAR_COUNT=$(ls -la target/lib/*.jar 2>/dev/null | wc -l)
if [ "$FINAL_JAR_COUNT" -eq 0 ]; then
    echo "❌ No JAR files found. Cannot proceed."
    exit 1
fi

echo "✅ Dependencies ready ($FINAL_JAR_COUNT JAR files)"

# Step 3: Compile the examples
echo "🔨 Compiling examples..."
javac -cp "target/classes:target/lib/*" ../examples/java/com/weather/sdk/examples/MinimalWeatherSDKExample.java
javac -cp "target/classes:target/lib/*" ../examples/java/com/weather/sdk/examples/PollingModeExample.java
javac -cp "target/classes:target/lib/*" ../examples/java/com/weather/sdk/examples/SimpleSDKRegistryExample.java

echo "✅ Setup complete! Now run:"
echo "   Minimal example:"
echo "   OPENWEATHER_API_KEY=\"your-api-key\" ./run-example.sh"
echo ""
echo "   Polling example:"
echo "   OPENWEATHER_API_KEY=\"your-api-key\" ./run-polling-example.sh"
echo ""
echo "   Simple SDKRegistry example:"
echo "   PRIMARY_WEATHER_API_KEY=\"your-primary-key\" \\"
echo "   BACKUP_WEATHER_API_KEY=\"your-backup-key\" \\"
echo "   ./run-simple-sdkregistry-example.sh"
echo ""
echo "   Or test with dummy keys:"
echo "   OPENWEATHER_API_KEY=\"dummy-key\" ./run-example.sh"
echo "   OPENWEATHER_API_KEY=\"dummy-key\" ./run-polling-example.sh"
echo "   PRIMARY_WEATHER_API_KEY=\"test-primary\" \\"
echo "   BACKUP_WEATHER_API_KEY=\"test-backup\" \\"
echo "   ./run-simple-sdkregistry-example.sh"