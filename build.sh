#!/usr/bin/env bash

# Fail on any error to ensure the script stops if a step fails.
set -e

# --- Configuration ---
# Get the script's directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Define the Android SDK version you want to target.
ANDROID_SDK_VERSION="36"
ANDROID_BUILD_TOOLS_VERSION="35.0.0"

# Switched from 'google_apis' to 'google_atd' (Google Automated Test Device).
# This system image is designed for headless, automated testing in CI environments
# and is more compatible with software rendering. It will be installed but may not
# be used by the new build command.
EMULATOR_IMAGE="system-images;android-34;google_atd;x86_64"

# --- Environment Setup ---

# Step 1: Check for essential command-line tools.
echo "INFO: Checking for prerequisites (wget, unzip, tar)..."
for cmd in wget unzip tar; do
  if ! command -v $cmd &> /dev/null; then
    echo "ERROR: Command '$cmd' not found. Please install it using your system's package manager (e.g., 'sudo apt-get install $cmd') and try again."
    exit 1
  fi
done
echo "INFO: Prerequisites are installed."


# Step 2: Install and configure Java 17 system-wide.
echo "INFO: Setting up Java 17..."
# The build needs Java 17, set it as the default Java version.
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk
sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-amd64/bin/java
java -version

# Also clear JAVA_HOME variable so java -version is used instead
export JAVA_HOME=

# Add the local SDK and emulator tools to the PATH for this session.
# The system-wide Java will already be in the PATH.
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator"
echo "INFO: Local tools added to PATH."

# Now, accept licenses and install packages.
# It's best practice to accept licenses *after* the tools are in place.
echo "INFO: Accepting all pending SDK licenses..."
yes | sdkmanager --licenses

echo "INFO: Installing Android SDK packages, including emulator and system image..."
# This single command will install/update all necessary packages.
sdkmanager "platforms;android-${ANDROID_SDK_VERSION}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" "platform-tools" "${EMULATOR_IMAGE}" "emulator"

# Run license acceptance AGAIN after installing new packages. This is crucial.
echo "INFO: Accepting licenses for newly installed packages..."
yes | sdkmanager --licenses

echo "Copying google-services.json"
cp /tmpfs/src/git/androidify-prebuilts/google-services.json ${DIR}/app

echo "Copying local.properties"
cp /tmpfs/src/git/androidify-prebuilts/gradle.properties ${DIR}
ls

# --- Build Process ---

# This script assembles the release build of the Android application.
# Ensure gradlew is executable
chmod +x ./gradlew

# Clean the project (optional, but good for a fresh release build)
echo "INFO: Cleaning the project..."
./gradlew clean -Pandroid.sdk.path=$ANDROID_HOME

# Build the production release bundle without generating a baseline profile.
echo "INFO: Building the production release bundle..."
./gradlew app:bundleRelease -x test -Pandroid.sdk.path=$ANDROID_HOME

# Check if the build was successful
if [ $? -eq 0 ]; then
  echo "SUCCESS: Build successful! The AAB can be found in app/build/outputs/bundle/release/"
else
  echo "FAILURE: Build failed. Please check the console output for errors."
  exit 1
fi

exit 0
