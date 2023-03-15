# Azure Maps Weather for Java

> see https://aka.ms/autorest

### Setup
> see https://github.com/Azure/autorest.java

### Generation
> see https://github.com/Azure/autorest.java/releases for the latest version of autorest
```ps
cd <swagger-folder>
mvn install
autorest --java --use:@autorest/java@4.0.x
```

### Code generation settings

## Java

``` yaml
directive:

  - from: swagger-document
    where: "$"
    transform: >
        $["securityDefinitions"] = {};
  - from: swagger-document
    where: "$"
    transform: >
        $["security"] = [];

title: WeatherClient
input-file: https://raw.githubusercontent.com/Azure/azure-rest-api-specs/main/specification/maps/data-plane/Weather/stable/1.1/weather.json
namespace: com.azure.maps.weather
java: true
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
custom-types: ActiveStorm,ActiveStormResult,AirAndPollen,AirQuality,AirQualityResult,AlertDetails,BasinId,ColorValue,CurrentConditions,CurrentConditionsResult,DailyAirQuality,DailyAirQualityForecastResult,DailyDuration,DailyForecast,DailyForecastDetail,DailyForecastResult,DailyForecastSummary,DailyHistoricalActuals,DailyHistoricalActualsResult,DailyHistoricalNormals,DailyHistoricalNormalsResult,DailyHistoricalRecords,DailyHistoricalRecordsResult,DailyIndex,DailyIndicesResult,DayQuarter,DegreeDaySummary,DominantPollutant,ForecastInterval,HazardDetail,HazardIndex,HourlyDuration,HourlyForecast,HourlyForecastResult,IconCode,IntervalSummary,LatestStatus,LatestStatusKeyword,LocalSource,MinuteForecastResult,MinuteForecastSummary,PastHoursTemperature,Pollutant,PollutantType,PrecipitationSummary,PrecipitationType,PressureTendency,QuarterDayForecast,QuarterDayForecastResult,RadiusSector,SevereWeatherAlert,SevereWeatherAlertDescription,SevereWeatherAlertsResult,StormForecast,StormForecastResult,StormLocation,StormLocationsResult,StormSearchResult,StormSearchResultItem,StormWindRadiiSummary,SunGlare,TemperatureSummary,UnitType,WaypointForecast,WeatherAlongRoutePrecipitation,WeatherAlongRouteResult,WeatherAlongRouteSummary,WeatherDataUnit,WeatherHazards,WeatherNotification,WeatherValue,WeatherValueMaxMinAvg,WeatherValueRange,WeatherValueYear,WeatherValueYearMax,WeatherValueYearMaxMinAvg,WeatherWindow,WindDetails,WindDirection
customization-jar-path: target/azure-maps-weather-customization-1.0.0-beta.1.jar
customization-class: WeatherCustomization
```
