# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2025-02-19)

### Breaking Changes

- Update to latest API spec. `temperatureSummary` returns `past3Hours`, `past6Hours`, `past9Hours`, `past12Hours`, `past18Hours`, and `past24Hours`.
- Rename methods:
  - `getAirQualityDailyForecasts` to `getDailyAirQualityForecasts`
  - `getAirQualityHourlyForecasts` to `getHourlyAirQualityForecasts`
  - `getGovId` to `getGovernmentId` in `ActiveStorm` class
- Update `getCurrentConditions` function signature
- Remove `getNextLink` in all classes
- Remove `isActive` method from `ActiveStorm` class

### Other Changes

- Upgrade `@autorest/java` version

## 1.0.0-beta.2 (2024-12-27)

### Features Added

- Integrated support for SAS-based authentication

## 1.0.0-beta.1 (2022-11-08)

- Azure Maps SDK Weather client library for Java. This package contains the Azure Maps SDK Weather library. Package tag 1.0.0-beta.1. For documentation on how to use this package, please see [Microsoft Azure SDK for Weather SDK](https://docs.microsoft.com/rest/api/maps/weather).

