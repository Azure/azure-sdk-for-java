# Azure Maps Weather for Java

> see https://aka.ms/autorest

This is the AutoRest configuration file for Maps Weather.
---
## Getting Started

To build the SDK for Maps Weather, simply [Install AutoRest](https://aka.ms/autorest) and in this folder, run:

> `autorest`

To see additional help and options, run:

> `autorest --help`

### Setup
```ps
npm install -g autorest
```

### Generation

```ps
cd <swagger-folder>
autorest
```

### Code generation settings

## Java

``` yaml
directive:
  - from: swagger-document
    where: "$"
    transform: >
      $["securityDefinitions"] = {};
      $["security"] = [];

title: WeatherClient
input-file: https://github.com/Azure/azure-rest-api-specs/blob/b43042075540b8d67cce7d3d9f70b9b9f5a359da/specification/maps/data-plane/Weather/stable/1.1/weather.json
namespace: com.azure.maps.weather
java: true
use: '@autorest/java@4.1.52'
output-folder: ../
license-header: MICROSOFT_MIT_SMALL
payload-flattening-threshold: 0
add-context-parameter: true
context-client-method-parameter: true
client-logger: true
generate-client-as-impl: true
sync-methods: all
generate-sync-async-clients: false
polling: {}
models-subpackage: implementation.models
custom-types-subpackage: models
custom-types: ActiveStorm,ActiveStormResult,AirAndPollen,AirQuality,AirQualityResult,AlertDetails,BasinId,ColorValue,CurrentConditions,CurrentConditionsResult,DailyAirQuality,DailyAirQualityForecastResult,DailyDuration,DailyForecast,DailyForecastDetail,DailyForecastResult,DailyForecastSummary,DailyHistoricalActuals,DailyHistoricalActualsResult,DailyHistoricalNormals,DailyHistoricalNormalsResult,DailyHistoricalRecords,DailyHistoricalRecordsResult,DailyIndex,DailyIndicesResult,DayQuarter,DegreeDaySummary,DominantPollutant,ForecastInterval,HazardDetail,HazardIndex,HourlyDuration,HourlyForecast,HourlyForecastResult,IconCode,IntervalSummary,LatestStatus,LatestStatusKeyword,LocalSource,MinuteForecastResult,MinuteForecastSummary,PastHoursTemperature,Pollutant,PollutantType,PrecipitationSummary,PrecipitationType,PressureTendency,QuarterDayForecast,QuarterDayForecastResult,RadiusSector,SevereWeatherAlert,SevereWeatherAlertDescription,SevereWeatherAlertsResult,StormForecast,StormForecastResult,StormLocation,StormLocationsResult,StormSearchResult,StormSearchResultItem,StormWindRadiiSummary,SunGlare,TemperatureSummary,UnitType,WaypointForecast,WeatherAlongRoutePrecipitation,WeatherAlongRouteResult,WeatherAlongRouteSummary,WeatherDataUnit,WeatherHazards,WeatherNotification,WeatherUnitDetails,WeatherValueMaxMinAvg,WeatherValueRange,WeatherValueYear,WeatherValueYearMax,WeatherValueYearMaxMinAvg,WeatherWindow,WindDetails,WindDirection
customization-class: src/main/java/WeatherCustomization.java
generic-response-type: true
no-custom-headers: true

```

### Rename WeatherUnit to WeatherUnitDetails

``` yaml
directive:
  - from: swagger-document
    where: $.definitions.WeatherUnit
    transform: >
      $["x-ms-client-name"] = "WeatherUnitDetails";
```

### Fix cases where date-time changed to string

``` yaml
directive:
  - from: swagger-document
    where: $.definitions
    transform: >
      $.StormForecast.properties.dateTime.format = "date-time";
      $.StormLocation.properties.dateTime.format = "date-time";
      $.StormWindRadiiSummary.properties.dateTime.format = "date-time";
```
