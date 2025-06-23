#!/usr/bin/env bash

#
# Copyright 2022 The Android Open Source Project
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
# Fail on any error.
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP_OUT=$DIR/app/build/outputs

# This script assembles the release build of the Android application.
# Ensure gradlew is executable
chmod +x ./gradlew

# Clean the project (optional, but good for a fresh release build)
echo "Cleaning the project..."
./gradlew clean

# Assemble the release build
echo "Assembling the release build..."
./gradlew app:bundleRelease

# Check if the build was successful
if [ $? -eq 0 ]; then
  echo "Build successful! The APK/AAB can be found in app/build/outputs/"
else
  echo "Build failed. Please check the console output for errors."
  exit 1
fi

exit 0