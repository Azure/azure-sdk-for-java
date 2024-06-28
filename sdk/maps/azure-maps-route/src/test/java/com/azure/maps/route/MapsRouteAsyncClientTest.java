// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteType;
import com.azure.maps.route.models.TravelMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapsRouteAsyncClientTest extends MapsRouteTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    private MapsRouteAsyncClient getRouteAsyncClient(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        return getRouteAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async begin request route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteMatrix(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85106, 52.36006), new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85003, 52.36241), new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);

        StepVerifier.create(client.beginGetRouteMatrix(options)
                .setPollInterval(durationTestMode)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(actualResult -> validateBeginRequestRouteMatrix(TestUtils.getExpectedBeginRequestRouteMatrix(),
                actualResult))
            .verifyComplete();
    }

    // Test async begin get route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginGetRouteMatrix(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85106, 52.36006), new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85003, 52.36241), new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);

        StepVerifier.create(client.beginGetRouteMatrix(options)
                .setPollInterval(durationTestMode)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(actualResult -> validateBeginRequestRouteMatrix(TestUtils.getExpectedGetRequestRouteMatrix(),
                actualResult))
            .verifyComplete();
    }

    // Test async begin get route matrix with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginGetRouteMatrixWithContext(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85106, 52.36006), new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(
            Arrays.asList(new GeoPoint(4.85003, 52.36241), new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);

        StepVerifier.create(client.beginGetRouteMatrix(options, null)
                .setPollInterval(durationTestMode)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(actualResult -> validateBeginRequestRouteMatrix(TestUtils.getExpectedGetRequestRouteMatrix(),
                actualResult))
            .verifyComplete();
    }

    // Test async get route directions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirections(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirections(routeOptions))
            .assertNext(
                actualResults -> validateGetRouteDirections(TestUtils.getExpectedRouteDirections(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get route directions with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithResponse(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirectionsWithResponse(routeOptions))
            .assertNext(
                response -> validateGetRouteDirectionsWithResponse(TestUtils.getExpectedRouteDirections(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteDirectionsWithResponse(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(-1000000, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirectionsWithContextWithResponse(routeOptions, null))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get route directions with additional parameters
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithAdditionalParameters(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(
            Arrays.asList(new GeoPoint(13.42936, 52.5093), new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(-122.39456176757811, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.489368981370724)))), new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(100.0, 0.0), new GeoPosition(101.0, 0.0), new GeoPosition(101.0, 1.0),
                new GeoPosition(100.0, 1.0), new GeoPosition(100.0, 0.0)))));
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters().setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirections(routeOptions, parameters))
            .assertNext(actualResults -> validateGetRouteDirections(
                TestUtils.getExpectedRouteDirectionsWithAdditionalParameters(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get route directions with additional parameters with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(
            Arrays.asList(new GeoPoint(13.42936, 52.5093), new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(-122.39456176757811, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.489368981370724)))), new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(100.0, 0.0), new GeoPosition(101.0, 0.0), new GeoPosition(101.0, 1.0),
                new GeoPosition(100.0, 1.0), new GeoPosition(100.0, 0.0)))));
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters().setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirectionsWithResponse(routeOptions, parameters))
            .assertNext(actualResults -> validateGetRouteDirectionsWithResponse(
                TestUtils.getExpectedRouteDirectionsWithAdditionalParameters(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(new GeoPosition(-1000000, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(
            Arrays.asList(new GeoPoint(13.42936, 52.5093), new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(-122.39456176757811, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.489368981370724),
                new GeoPosition(-122.00454711914061, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.65151268066222),
                new GeoPosition(-122.39456176757811, 47.489368981370724)))), new GeoPolygon(new GeoLinearRing(
            Arrays.asList(new GeoPosition(100.0, 0.0), new GeoPosition(101.0, 0.0), new GeoPosition(101.0, 1.0),
                new GeoPosition(100.0, 1.0), new GeoPosition(100.0, 0.0)))));
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters().setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirectionsWithResponse(routeOptions, parameters))
            .expectErrorSatisfies(ex -> {
                final HttpResponseException httpResponseException = (HttpResponseException) ex;
                assertEquals(400, httpResponseException.getResponse().getStatusCode());
            })
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get route range
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteRange(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452, 5.86605),
            Duration.ofSeconds(6000));
        StepVerifier.create(client.getRouteRange(rangeOptions))
            .assertNext(actualResults -> validateGetRouteRange(TestUtils.getExpectedRouteRange(), actualResults))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Test async get route range with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteRangeWithResponse(HttpClient httpClient, MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452, 5.86605),
            Duration.ofSeconds(6000));
        StepVerifier.create(client.getRouteRangeWithResponse(rangeOptions))
            .assertNext(response -> validateGetRouteRangeWithResponse(TestUtils.getExpectedRouteRange(), response))
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteRangeWithResponse(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(-1000000, 5.86605),
            Duration.ofSeconds(6000));
        StepVerifier.create(client.getRouteRangeWithResponse(rangeOptions)).expectErrorSatisfies(ex -> {
            final HttpResponseException httpResponseException = (HttpResponseException) ex;
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
        }).verify(DEFAULT_TIMEOUT);
    }

    // Test async begin request route directions batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteDirectionsBatch(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.639987, -122.128384), new GeoPosition(47.621252, -122.184408),
                new GeoPosition(47.596437, -122.332000))).setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);
        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.620659, -122.348934),
                new GeoPosition(47.610101, -122.342015))).setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);
        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(40.759856, -73.985108), new GeoPosition(40.771136, -73.973506))).setRouteType(
            RouteType.SHORTEST).setTravelMode(TravelMode.PEDESTRIAN);
        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);

        StepVerifier.create(client.beginRequestRouteDirectionsBatch(optionsList)
                .setPollInterval(durationTestMode)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(actualResult -> validateBeginRequestRouteDirections(
                TestUtils.getExpectedBeginRequestRouteDirectionsBatch(), actualResult))
            .verifyComplete();
    }

    // Test async begin request route directions batch with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteDirectionsBatchWithContext(HttpClient httpClient,
        MapsRouteServiceVersion serviceVersion) {
        MapsRouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.639987, -122.128384), new GeoPosition(47.621252, -122.184408),
                new GeoPosition(47.596437, -122.332000))).setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);
        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.620659, -122.348934),
                new GeoPosition(47.610101, -122.342015))).setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);
        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(40.759856, -73.985108), new GeoPosition(40.771136, -73.973506))).setRouteType(
            RouteType.SHORTEST).setTravelMode(TravelMode.PEDESTRIAN);
        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);

        StepVerifier.create(client.beginRequestRouteDirectionsBatch(optionsList, null)
                .setPollInterval(durationTestMode)
                .last()
                .flatMap(AsyncPollResponse::getFinalResult))
            .assertNext(actualResult -> validateBeginRequestRouteDirections(
                TestUtils.getExpectedBeginRequestRouteDirectionsBatch(), actualResult))
            .verifyComplete();
    }
}
