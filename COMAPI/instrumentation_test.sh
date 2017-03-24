#!/bin/bash

# Starts Nexus5_API23 emulator and performs instrumentation tests

#Start the emulator
emulator -avd Nexus5_API23 -wipe-data & EMULATOR_PID=$!

# Wait for Android to finish booting
WAIT_CMD="adb wait-for-device shell getprop init.svc.bootanim"
until $WAIT_CMD | grep -m 1 stopped; do
  echo "Waiting..."
  sleep 1
done

# Unlock the Lock Screen
#adb shell input keyevent 82

# Clear and capture logcat
adb logcat -c
adb logcat > build/logcat.log & LOGCAT_PID=$!

# Run the tests
./gradlew connectedAndroidTest -i

# Stop the background processes
kill $LOGCAT_PID
kill $EMULATOR_PID