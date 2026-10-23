# Changelog

All notable changes to the dotdigital Android SDK will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.8.0]

### Fixed
- Prevent the same callback from receiving both error(...) and success(...) in invalid initialization scenarios
- The SDK now refreshes the current Firebase token instead of trusting a stale cached value
### Changed
- Token refresh handling was moved from the obsolete IDService to PushService.onNewToken(...)

## [1.7.0] - 2026-06-17

### Changed
- Added the isDdOriginated function to the PushDetails and PushHandleResult classes.
  This allows you to determine whether a push message originated from Dotdigital.

## [1.6.0] - 2026-04-17

### Changed
- **BREAKING**: Updated target SDK from 33 to 36 (Android 16)
- **BREAKING**: Updated minimum Java compatibility from 1.8 to 11
- Updated compile SDK from 33 to 36
- Changed Firebase Cloud Messaging dependencies from `implementation` to `api` for proper dependency propagation to consuming applications

### Fixed
- Removed deprecated `package` attribute from AndroidManifest.xml (now using `namespace` in build.gradle)

### Requirements
- **Minimum SDK**: 16 (unchanged)
- **Target SDK**: 36 (Android 16)
- **Java Version**: 11 or higher (previously 8)
- **Gradle**: 8.x or higher recommended

## [1.5.2] - Previous Release

- Previous stable release
