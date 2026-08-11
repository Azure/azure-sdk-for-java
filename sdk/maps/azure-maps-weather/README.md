# Azure Maps SDK Weather client library for Java

Azure Maps SDK Weather client library for Java.

This package contains the Azure Maps SDK Weather client library which contains Azure Maps Weather APIs. For documentation on how to use this package, please see [Azure Maps Weather SDK for Java](https://learn.microsoft.com/rest/api/maps/weather).

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-maps-weather;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-maps-weather</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Maps Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

There are 3 ways to authenticate the client: Shared key authentication, Microsoft Entra ID authentication, and shared access signature (SAS) authentication.

We recommend using Microsoft Entra ID with [`DefaultAzureCredential`][azure_identity]. This allows you to configure authentication through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

By default, Microsoft Entra ID token authentication depends on correct configuration of the following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java com.azure.maps.weather.sync.builder.ad.instantiation
// Authenticates using Azure AD building a default credential
// This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

// Creates a client
WeatherClient client = new WeatherClientBuilder()
    .credential(tokenCredential)
    .weatherClientId(System.getenv("MAPS_CLIENT_ID"))
    .buildClient();
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

For SAS-based authentication, please refer to [AccountsListSasSamples.java][https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-resourcemanager-maps/src/samples/java/com/azure/resourcemanager/maps/generated/AccountsListSasSamples.java].

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples
Get Hourly Forecast
```java com.azure.maps.weather.sync.get_hourly_forecast
client.getHourlyForecast(new GeoPosition(-122.138874, 47.632346), null, 12, null);
```

Get Minute Forecast
```java com.azure.maps.weather.sync.get_minute_forecast
client.getMinuteForecast(new GeoPosition(-122.138874, 47.632346), 15, null);
```

Get Minute Forecast
```java com.azure.maps.weather.sync.get_quarter_day_forecast
client.getQuarterDayForecast(new GeoPosition(-122.138874, 47.632346), null, 1, null);
```

Get Current Conditions
```java com.azure.maps.weather.sync.get_current_conditions
client.getCurrentConditions(new GeoPosition(-122.125679, 47.641268),
    null, true, null, null);
```

Get Daily Forecast
```java com.azure.maps.weather.sync.get_daily_forecast
client.getDailyForecast(new GeoPosition(30.0734812, 62.6490341), null, 5, null);
```

Get Weather Along Route
```java com.azure.maps.weather.sync.get_weather_along_route
List<Waypoint> waypoints = Arrays.asList(
    new Waypoint(new GeoPosition(-77.037, 38.907), 0.0),
    new Waypoint(new GeoPosition(-77.009, 38.907), 10.0),
    new Waypoint(new GeoPosition(-76.928, 38.926), 20.0),
    new Waypoint(new GeoPosition(-76.852, 39.033), 30.0),
    new Waypoint(new GeoPosition(-76.732, 39.168), 40.0),
    new Waypoint(new GeoPosition(-76.634, 39.269), 50.0),
    new Waypoint(new GeoPosition(-76.612, 39.287), 60.0)
);
client.getWeatherAlongRoute(waypoints, "en");
```

Get Severe Weather Alerts
```java com.azure.maps.weather.sync.get_severe_weather_alerts
client.getSevereWeatherAlerts(new GeoPosition(-85.06431274043842, 30.324604968788467), null, true);
```

Get Daily Indices
```java com.azure.maps.weather.sync.get_daily_indices
client.getDailyIndices(new GeoPosition(-79.37849, 43.84745), null, null, null, 11);
```

Get Tropical Storm Active
```java com.azure.maps.weather.sync.get_tropical_storm_active
client.getTropicalStormActive();
```

Get Tropical Storm Search
```java com.azure.maps.weather.sync.get_tropical_storm_search
ActiveStormResult result = client.getTropicalStormActive();
if (result.getActiveStorms().size() > 0) {
    ActiveStorm storm = result.getActiveStorms().get(0);
    client.searchTropicalStorm(storm.getYear(), storm.getBasinId(), storm.getGovernmentId());
}
```

Get Tropical Storm Forecast
```java com.azure.maps.weather.sync.get_tropical_storm_forecast
ActiveStormResult result = client.getTropicalStormActive();
if (result.getActiveStorms().size() > 0) {
    ActiveStorm storm = result.getActiveStorms().get(0);
    TropicalStormForecastOptions forecastOptions = new TropicalStormForecastOptions(storm.getYear(),
            storm.getBasinId(), storm.getGovernmentId())
            .setIncludeWindowGeometry(true);
    client.getTropicalStormForecast(forecastOptions);
}
```

Get Tropical Storm Locations
```java com.azure.maps.weather.sync.get_tropical_storm_locations
ActiveStormResult result = client.getTropicalStormActive();
if (result.getActiveStorms().size() > 0) {
    ActiveStorm storm = result.getActiveStorms().get(0);
    TropicalStormLocationOptions locationOptions = new TropicalStormLocationOptions(storm.getYear(),
            storm.getBasinId(), storm.getGovernmentId());
    client.getTropicalStormLocations(locationOptions);
}
```

Get Current Air Quality
```java com.azure.maps.weather.sync.get_current_air_quality
client.getCurrentAirQuality(
        new GeoPosition(-122.138874, 47.632346), "es", false);
```

Get Air Quality Daily Forecasts
```java com.azure.maps.weather.sync.get_air_quality_daily_forecasts
client.getDailyAirQualityForecast(
        new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS);
```

Get Air Quality Hourly Forecasts
```java com.azure.maps.weather.sync.get_air_quality_daily_forecasts
client.getDailyAirQualityForecast(
        new GeoPosition(-122.138874, 47.632346), "en", DailyDuration.TWO_DAYS);
```

Get Daily Historical Actuals
```java com.azure.maps.weather.sync.get_daily_historical_actuals
LocalDate before = LocalDate.now().minusDays(30);
LocalDate today = LocalDate.now();
client.getDailyHistoricalActuals(new GeoPosition(30.0734812, 62.6490341), before, today, null);
```

Get Daily Historical Records
```java com.azure.maps.weather.sync.get_daily_historical_records
LocalDate before = LocalDate.now().minusDays(30);
LocalDate today = LocalDate.now();
client.getDailyHistoricalActuals(new GeoPosition(30.0734812, 62.6490341), before, today, null);
```

Get Daily Historical Normals
```java com.azure.maps.weather.sync.get_daily_historical_normals
LocalDate before = LocalDate.now().minusDays(30);
LocalDate today = LocalDate.now();
client.getDailyHistoricalNormals(new GeoPosition(30.0734812, 62.6490341), before, today, null);
```

## Troubleshooting
When you interact with the Azure Maps Services, errors returned by the Maps service correspond to the same HTTP status codes returned for REST API requests.

For example, if you search with an invalid coordinate, a error is returned, indicating "Bad Request".400

## Next steps

Several Azure Maps Weather Java SDK samples are available to you in the SDK's GitHub repository.
[Azure Maps Weather Samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-weather/src/samples)

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-weather/src
[samples]:  https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/maps/azure-maps-weather/src/samples
[rest_docs]: https://learn.microsoft.com/rest/api/maps
[product_docs]: https://learn.microsoft.com/azure/azure-maps/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential

