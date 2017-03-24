#!/bin/bash

# Populates version of SDK defined in version.gradle in the Comapi class

COMAPI_VER="$(./gradlew -q printFoundationVersion)"
BUILD_NUM="$(./gradlew -q printBuildNumber)"
echo ver "${COMAPI_VER}"
echo build number "${BUILD_NUM}"
sed -i -e "s/VER_TO_REPLACE/${COMAPI_VER}/g" foundation/src/main/java/com/comapi/BaseComapi.java
sed -i -e "s/BUILD_NUM_TO_REPLACE/${BUILD_NUM}/g" foundation/src/main/java/com/comapi/BaseComapi.java
rm foundation/src/main/java/com/comapi/BaseComapi.java-e