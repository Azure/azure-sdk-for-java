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
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Arrays;
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
        return TestBase.getHttpClients()
            .flatMap(httpClient -> Arrays.stream(WeatherServiceVersion.values())
                .map(serviceVersion -> Arguments.of(httpClient, serviceVersion)));
    }

    static HourlyForecastResult getExpectedHourlyForecast() {
        return deserialize("gethourlyforecast.json", HourlyForecastResult::fromJson);
    }

    static MinuteForecastResult getExpectedMinuteForecast() {
        return deserialize("getminuteforecast.json", MinuteForecastResult::fromJson);
    }

    static QuarterDayForecastResult getExpectedQuarterDayForecast() {
        return deserialize("getquarterdayforecast.json", QuarterDayForecastResult::fromJson);
    }

    static CurrentConditionsResult getExpectedCurrentConditions() {
        return deserialize("getcurrentconditions.json", CurrentConditionsResult::fromJson);
    }

    static DailyForecastResult getExpectedDailyForecast() {
        return deserialize("getdailyforecast.json", DailyForecastResult::fromJson);
    }

    static WeatherAlongRouteResult getExpectedWeatherAlongRoute() {
        return deserialize("getweatheralongroute.json", WeatherAlongRouteResult::fromJson);
    }

    static SevereWeatherAlertsResult getExpectedSevereWeatherAlerts() {
        return deserialize("getsevereweatheralerts.json", SevereWeatherAlertsResult::fromJson);
    }

    static DailyIndicesResult getExpectedDailyIndices() {
        return deserialize("getdailyindices.json", DailyIndicesResult::fromJson);
    }

    static ActiveStormResult getExpectedTropicalStormActive() {
        return deserialize("gettropicalstormactive.json", ActiveStormResult::fromJson);
    }

    static StormSearchResult getExpectedSearchTropicalStorm() {
        return deserialize("gettropicalstormactive.json", StormSearchResult::fromJson);
    }

    static StormForecastResult getExpectedTropicalStormForecast() {
        return deserialize("gettropicalstormforecast.json", StormForecastResult::fromJson);
    }

    static StormLocationsResult getExpectedTropicalStormLocations() {
        return deserialize("gettropicalstormlocations.json", StormLocationsResult::fromJson);
    }

    static AirQualityResult getExpectedCurrentAirQuality() {
        return deserialize("getcurrentairquality.json", AirQualityResult::fromJson);
    }

    static DailyAirQualityForecastResult getExpectedAirQualityDailyForecasts() {
        return deserialize("getairqualitydailyforecasts.json", DailyAirQualityForecastResult::fromJson);
    }

    static AirQualityResult getExpectedAirQualityHourlyForecasts() {
        return deserialize("getairqualityhourlyforecasts.json", AirQualityResult::fromJson);
    }

    static DailyHistoricalRecordsResult getExpectedDailyHistoricalRecords() {
        return deserialize("getdailyhistoricalrecords.json", DailyHistoricalRecordsResult::fromJson);
    }

    static DailyHistoricalActualsResult getExpectedDailyHistoricalActuals() {
        return deserialize("getdailyhistoricalactuals.json", DailyHistoricalActualsResult::fromJson);
    }

    static DailyHistoricalNormalsResult getExpectedDailyHistoricalNormalsResult() {
        return deserialize("getdailyhistoricalnormals.json", DailyHistoricalNormalsResult::fromJson);
    }

    private static <T> T deserialize(String resourceName, ReadValueCallback<JsonReader, T> deserialization) {
        try (JsonReader jsonReader = JsonProviders.createReader(ClassLoader.getSystemResourceAsStream(resourceName))) {
            return deserialization.read(jsonReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
