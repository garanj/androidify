#!/usr/bin/env bash
#
# Copyright 2025 The Android Open Source Project
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

# IGNORE this file, it's only used in the internal Google release process
# Fail on any error to ensure the script stops if a step fails.
set -e

# --- Configuration ---
# Get the script's directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Define the Android SDK version you want to target.
ANDROID_SDK_VERSION="36"
ANDROID_BUILD_TOOLS_VERSION="36.0.0"

# Switched from 'google_apis' to 'google_atd' (Google Automated Test Device).
# This system image is designed for headless, automated testing in CI environments
# and is more compatible with software rendering. It will be installed but may not
# be used by the new build command.
EMULATOR_IMAGE="system-images;android-36;google_atd;x86_64"

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

echo "Copying gradle.properties"
echo "" >> ${DIR}/gradle.properties # add a new line to the file
cat /tmpfs/src/git/androidify-prebuilts/gradle.properties >> ${DIR}/gradle.properties

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

# --- Artifact Collection ---
echo "INFO: Preparing artifacts for Kokoro..."

# Default output path for the bundle
AAB_SRC_DIR="app/build/outputs/bundle/release"
# The default name of the AAB for a release bundle
AAB_FILE="app-release.aab"
AAB_PATH="${AAB_SRC_DIR}/${AAB_FILE}"

# Check if the AAB exists
if [[ -f "$AAB_PATH" ]]; then
  # Create a directory within Kokoro's artifact collection area
  ARTIFACT_DEST_DIR="${KOKORO_ARTIFACTS_DIR}/artifacts"
  mkdir -p "${ARTIFACT_DEST_DIR}"

  # Copy the AAB
  cp "${AAB_PATH}" "${ARTIFACT_DEST_DIR}/app-release-unsigned.aab"
  echo "SUCCESS: AAB copied to ${ARTIFACT_DEST_DIR}"

  # Copy any .intointo.jsonl files to the artifact directory
  echo "INFO: Searching for and copying .intointo.jsonl files..."
  ls
  echo "INFO: Logging output directory contents"
  ls "$AAB_SRC_DIR/"
  find . -type f -name "*.intointo.jsonl" -print0 | xargs -0 -I {} cp {} "${ARTIFACT_DEST_DIR}/"
  echo "INFO: Finished copying .intointo.jsonl files."

else
  echo "FAILURE: AAB not found at ${AAB_PATH}"
  # Optionally fail the build: exit 1
fi

exit 0
