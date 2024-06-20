// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.maps.weather.models.ActiveStormResult;
import com.azure.maps.weather.models.AirQualityResult;
import com.azure.maps.weather.models.CurrentConditionsResult;
import com.azure.maps.weather.models.DailyAirQualityForecastResult;
import com.azure.maps.weather.models.DailyForecastResult;
import com.azure.maps.weather.models.DailyHistoricalActualsResult;
import com.azure.maps.weather.models.DailyHistoricalNormalsResult;
import com.azure.maps.weather.models.DailyHistoricalRecordsResult;
import com.azure.maps.weather.models.DailyIndicesResult;
import com.azure.maps.weather.models.HourlyForecastResult;
import com.azure.maps.weather.models.MinuteForecastResult;
import com.azure.maps.weather.models.QuarterDayForecastResult;
import com.azure.maps.weather.models.SevereWeatherAlertsResult;
import com.azure.maps.weather.models.StormForecastResult;
import com.azure.maps.weather.models.StormLocationsResult;
import com.azure.maps.weather.models.StormSearchResult;
import com.azure.maps.weather.models.WeatherAlongRouteResult;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestUtils {
    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(30);

    /**
     * Returns a stream of arguments that includes all combinations of eligible {@link HttpClient HttpClients} and
     * service versions that should be tested.
     *
     * @return A stream of HttpClient and service version combinations to test.
     */
    public static Stream<Arguments> getTestParameters() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        TestBase.getHttpClients().forEach(httpClient -> Arrays.stream(WeatherServiceVersion.values())
            .forEach(serviceVersion -> argumentsList.add(Arguments.of(httpClient, serviceVersion))));
        return argumentsList.stream();
    }

    static HourlyForecastResult getExpectedHourlyForecast() throws IOException {
        return deserialize("gethourlyforecast.json", HourlyForecastResult::fromJson);
    }

    static MinuteForecastResult getExpectedMinuteForecast() throws IOException {
        return deserialize("getminuteforecast.json", MinuteForecastResult::fromJson);
    }

    static QuarterDayForecastResult getExpectedQuarterDayForecast() throws IOException {
        return deserialize("getquarterdayforecast.json", QuarterDayForecastResult::fromJson);
    }

    static CurrentConditionsResult getExpectedCurrentConditions() throws IOException {
        return deserialize("getcurrentconditions.json", CurrentConditionsResult::fromJson);
    }

    static DailyForecastResult getExpectedDailyForecast() throws IOException {
        return deserialize("getdailyforecast.json", DailyForecastResult::fromJson);
    }

    static WeatherAlongRouteResult getExpectedWeatherAlongRoute() throws IOException {
        return deserialize("getweatheralongroute.json", WeatherAlongRouteResult::fromJson);
    }

    static SevereWeatherAlertsResult getExpectedSevereWeatherAlerts() throws IOException {
        return deserialize("getsevereweatheralerts.json", SevereWeatherAlertsResult::fromJson);
    }

    static DailyIndicesResult getExpectedDailyIndices() throws IOException {
        return deserialize("getdailyindices.json", DailyIndicesResult::fromJson);
    }

    static ActiveStormResult getExpectedTropicalStormActive() throws IOException {
        return deserialize("gettropicalstormactive.json", ActiveStormResult::fromJson);
    }

    static StormSearchResult getExpectedSearchTropicalStorm() throws IOException {
        return deserialize("gettropicalstormactive.json", StormSearchResult::fromJson);
    }

    static StormForecastResult getExpectedTropicalStormForecast() throws IOException {
        return deserialize("gettropicalstormforecast.json", StormForecastResult::fromJson);
    }

    static StormLocationsResult getExpectedTropicalStormLocations() throws IOException {
        return deserialize("gettropicalstormlocations.json", StormLocationsResult::fromJson);
    }

    static AirQualityResult getExpectedCurrentAirQuality() throws IOException {
        return deserialize("getcurrentairquality.json", AirQualityResult::fromJson);
    }

    static DailyAirQualityForecastResult getExpectedAirQualityDailyForecasts() throws IOException {
        return deserialize("getairqualitydailyforecasts.json", DailyAirQualityForecastResult::fromJson);
    }

    static AirQualityResult getExpectedAirQualityHourlyForecasts() throws IOException {
        return deserialize("getairqualityhourlyforecasts.json", AirQualityResult::fromJson);
    }

    static DailyHistoricalRecordsResult getExpectedDailyHistoricalRecords() throws IOException {
        return deserialize("getdailyhistoricalrecords.json", DailyHistoricalRecordsResult::fromJson);
    }

    static DailyHistoricalActualsResult getExpectedDailyHistoricalActuals() throws IOException {
        return deserialize("getdailyhistoricalactuals.json", DailyHistoricalActualsResult::fromJson);
    }

    static DailyHistoricalNormalsResult getExpectedDailyHistoricalNormalsResult() throws IOException {
        return deserialize("getdailyhistoricalnormals.json", DailyHistoricalNormalsResult::fromJson);
    }

    private static <T> T deserialize(String resourceName, ReadValueCallback<JsonReader, T> deserialization)
        throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserialization.read(jsonReader);
        }
    }
}
