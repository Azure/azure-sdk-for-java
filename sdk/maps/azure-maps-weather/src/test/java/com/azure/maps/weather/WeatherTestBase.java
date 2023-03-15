// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
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

public class WeatherTestBase extends TestBase {
    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    private final String endpoint = Configuration.getGlobalConfiguration().get("API-LEARN_ENDPOINT");
    Duration durationTestMode;
    static InterceptorManager interceptorManagerTestBase;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
        interceptorManagerTestBase = interceptorManager;
    }
    
    WeatherClientBuilder getWeatherAsyncClientBuilder(HttpClient httpClient, WeatherServiceVersion serviceVersion) {
        WeatherClientBuilder builder = new WeatherClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
        String endpoint = getEndpoint();
        if (getEndpoint() != null) {
            builder.endpoint(endpoint);
        }
        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(FAKE_API_KEY)).httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.credential((new AzureKeyCredential(
                Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));
        }
        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, endpoint.replaceFirst("/$", "") + "/.default"));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
    }

    static void validateGetHourlyForecast(HourlyForecastResult expected, HourlyForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getForecasts().size(), actual.getForecasts().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetHourlyForecastWithResponse(HourlyForecastResult expected, int expectedStatusCode, Response<HourlyForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetHourlyForecast(expected, response.getValue());
    }

    static void validateGetMinuteForecast(MinuteForecastResult expected, MinuteForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getIntervals().size(), actual.getIntervals().size());
    }

    static void validateGetMinuteForecastWithResponse(MinuteForecastResult expected, int expectedStatusCode, Response<MinuteForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetMinuteForecast(expected, response.getValue());
    }

    static void validateGetQuarterDayForecast(QuarterDayForecastResult expected, QuarterDayForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getForecasts().size(), actual.getForecasts().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetQuarterDayForecastWithResponse(QuarterDayForecastResult expected, int expectedStatusCode, Response<QuarterDayForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetQuarterDayForecast(expected, response.getValue());
    }

    static void validateGetCurrentConditions(CurrentConditionsResult expected, CurrentConditionsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getResults().size(), actual.getResults().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetCurrentConditionsWithResponse(CurrentConditionsResult expected, int expectedStatusCode, Response<CurrentConditionsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCurrentConditions(expected, response.getValue());
    }

    static void validateGetDailyForecast(DailyForecastResult expected, DailyForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        if (actual.getForecasts().size() > 0) {
            assertNotNull(expected.getSummary().getCategory());
            assertNotNull(actual.getSummary().getCategory());
        }
    }

    static void validateGetDailyForecastWithResponse(DailyForecastResult expected, int expectedStatusCode, Response<DailyForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDailyForecast(expected, response.getValue());
    }


    static void validateGetExpectedWeatherAlongRoute(WeatherAlongRouteResult expected, WeatherAlongRouteResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getWaypoints().size(), actual.getWaypoints().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
    }

    static void validateGetExpectedWeatherAlongRouteWithResponse(WeatherAlongRouteResult expected, int expectedStatusCode, Response<WeatherAlongRouteResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetExpectedWeatherAlongRoute(expected, response.getValue());
    }

    static void validateGetSevereWeatherAlerts(SevereWeatherAlertsResult expected, SevereWeatherAlertsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetSevereWeatherAlertsWithResponse(SevereWeatherAlertsResult expected, int expectedStatusCode, Response<SevereWeatherAlertsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetSevereWeatherAlerts(expected, response.getValue());
    }

    static void validateGetDailyIndices(DailyIndicesResult expected, DailyIndicesResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getResults().size(), actual.getResults().size());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyIndicesWithResponse(DailyIndicesResult expected, int expectedStatusCode, Response<DailyIndicesResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDailyIndices(expected, response.getValue());
    }

    static void validateGetExpectedTropicalStormActive(ActiveStormResult expected, ActiveStormResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        if (actual.getActiveStorms().size() > 0) {
            assertEquals(expected.getClass().getName(), actual.getClass().getName());
            assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
        }
    }

    static void validateGetExpectedTropicalStormActiveWithResponse(ActiveStormResult expected, int expectedStatusCode, Response<ActiveStormResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetExpectedTropicalStormActive(expected, response.getValue());
    }

    static void validateGetSearchTropicalStorm(StormSearchResult expected, StormSearchResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
        assertEquals(expected.getNextLink(), actual.getNextLink());
    }

    static void validateGetSearchTropicalStormWithResponse(StormSearchResult expected, int expectedStatusCode, Response<StormSearchResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetSearchTropicalStorm(expected, response.getValue());
    }

    static void validateGetTropicalStormForecast(StormForecastResult expected, StormForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetTropicalStormForecastWithResponse(StormForecastResult expected, int expectedStatusCode, Response<StormForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTropicalStormForecast(expected, response.getValue());
    }

    static void validateGetTropicalStormLocations(StormLocationsResult expected, StormLocationsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetTropicalStormLocationsWithResponse(StormLocationsResult expected, int expectedStatusCode, Response<StormLocationsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetTropicalStormLocations(expected, response.getValue());
    }

    static void validateGetCurrentAirQuality(AirQualityResult expected, AirQualityResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getAirQualityResults().size(), actual.getAirQualityResults().size());
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetCurrentAirQualityWithResponse(AirQualityResult expected, int expectedStatusCode, Response<AirQualityResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetCurrentAirQuality(expected, response.getValue());
    }

    static void validateGetAirQualityDailyForecasts(DailyAirQualityForecastResult expected, DailyAirQualityForecastResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetAirQualityDailyForecastsWithResponse(DailyAirQualityForecastResult expected, int expectedStatusCode, Response<DailyAirQualityForecastResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetAirQualityDailyForecasts(expected, response.getValue());
    }

    static void validateGetAirQualityHourlyForecasts(AirQualityResult expected, AirQualityResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetAirQualityHourlyForecastsWithResponse(AirQualityResult expected, int expectedStatusCode, Response<AirQualityResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetAirQualityHourlyForecasts(expected, response.getValue());
    }

    static void validateGetDailyHistoricalRecords(DailyHistoricalRecordsResult expected, DailyHistoricalRecordsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalRecordsWithResponse(DailyHistoricalRecordsResult expected, int expectedStatusCode, Response<DailyHistoricalRecordsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDailyHistoricalRecords(expected, response.getValue());
    }

    static void validateGetDailyHistoricalActuals(DailyHistoricalActualsResult expected, DailyHistoricalActualsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalActualsWithResponse(DailyHistoricalActualsResult expected, int expectedStatusCode, Response<DailyHistoricalActualsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDailyHistoricalActuals(expected, response.getValue());
    }

    static void validateGetDailyHistoricalNormalsResult(DailyHistoricalNormalsResult expected, DailyHistoricalNormalsResult actual) {
        assertNotNull(actual);
        assertNotNull(expected);
        assertEquals(expected.getNextLink(), actual.getNextLink());
        assertEquals(expected.getClass().getName(), actual.getClass().getName());
        assertEquals(expected.getClass().getSimpleName(), actual.getClass().getSimpleName());
    }

    static void validateGetDailyHistoricalNormalsResultWithResponse(DailyHistoricalNormalsResult expected, int expectedStatusCode, Response<DailyHistoricalNormalsResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetDailyHistoricalNormalsResult(expected, response.getValue());
    }
}
