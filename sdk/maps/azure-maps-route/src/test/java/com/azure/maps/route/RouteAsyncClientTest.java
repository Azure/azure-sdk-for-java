package com.azure.maps.route;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPoint;
import com.azure.core.models.GeoPointCollection;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteType;
import com.azure.maps.route.models.TravelMode;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

public class RouteAsyncClientTest extends RouteTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private RouteAsyncClient getRouteAsyncClient(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        return getRouteAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }
    
    // Test async begin request route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteMatrix(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
            new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        PollerFlux<RouteMatrixResult, RouteMatrixResult> pollerFlux = client.beginRequestRouteMatrix(options);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        RouteMatrixResult actualResult =  syncPoller.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedBeginRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

     // Test async begin get route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginGetRouteMatrix(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
            new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        PollerFlux<RouteMatrixResult, RouteMatrixResult> pollerFlux = client.beginRequestRouteMatrix(options);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        RouteMatrixResult routeMatrixResult = syncPoller.getFinalResult();
        String routeMatrixId = routeMatrixResult.getMatrixId();
        PollerFlux<RouteMatrixResult, RouteMatrixResult> pollerFlux2 = client.beginGetRouteMatrix(routeMatrixId);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller2 = setPollInterval(pollerFlux2.getSyncPoller());
        RouteMatrixResult actualResult = syncPoller2.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedGetRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test async begin get route matrix with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginGetRouteMatrixWithContext(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
            new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
            new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        PollerFlux<RouteMatrixResult, RouteMatrixResult> pollerFlux = client.beginRequestRouteMatrix(options, null);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        RouteMatrixResult routeMatrixResult = syncPoller.getFinalResult();
        String routeMatrixId = routeMatrixResult.getMatrixId();
        PollerFlux<RouteMatrixResult, RouteMatrixResult> pollerFlux2 = client.beginGetRouteMatrix(routeMatrixId, null);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller2 = setPollInterval(pollerFlux2.getSyncPoller());
        RouteMatrixResult actualResult = syncPoller2.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedGetRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test async get route directions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirections(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirections(routeOptions))
        .assertNext(actualResults -> {
            try {
                validateGetRouteDirections(TestUtils.getExpectedRouteDirections(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get route directions");
            }
        }).verifyComplete();
    }

    // Test async get route directions with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirectionsWithResponse(routeOptions))
                .assertNext(response ->
                {
                    try {
                        validateGetRouteDirectionsWithResponse(TestUtils.getExpectedRouteDirections(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get route directions");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteDirectionsWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(-1000000, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        StepVerifier.create(client.getRouteDirectionsWithResponse(routeOptions, null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async get route directions with additional parameters
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithAdditionalParameters(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(Arrays.asList(new GeoPoint(13.42936, 52.5093),
            new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(-122.39456176757811, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.489368981370724)
                ))
            ),
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(100.0, 0.0),
                    new GeoPosition(101.0, 0.0),
                    new GeoPosition(101.0, 1.0),
                    new GeoPosition(100.0, 1.0),
                    new GeoPosition(100.0, 0.0)
                ))
            )
        );
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters()
            .setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirectionsWithAdditionalParameters(routeOptions,parameters))
        .assertNext(actualResults -> {
            try {
                validateGetRouteDirections(TestUtils.getExpectedRouteDirectionsWithAdditionalParameters(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get route directions with additional parameters");
            }
        }).verifyComplete();
    }

    // Test async get route directions with additional parameters with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(Arrays.asList(new GeoPoint(13.42936, 52.5093),
            new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(-122.39456176757811, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.489368981370724)
                ))
            ),
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(100.0, 0.0),
                    new GeoPosition(101.0, 0.0),
                    new GeoPosition(101.0, 1.0),
                    new GeoPosition(100.0, 1.0),
                    new GeoPosition(100.0, 0.0)
                ))
            )
        );
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters()
            .setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirectionsWithAdditionalParametersWithResponse(routeOptions,parameters))
        .assertNext(actualResults -> {
            try {
                validateGetRouteDirectionsWithResponse(TestUtils.getExpectedRouteDirectionsWithAdditionalParameters(), 200, actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get route directions with additional parameters");
            }
        }).verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(-1000000, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(Arrays.asList(new GeoPoint(13.42936, 52.5093),
            new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(-122.39456176757811, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.489368981370724)
                ))
            ),
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(100.0, 0.0),
                    new GeoPosition(101.0, 0.0),
                    new GeoPosition(101.0, 1.0),
                    new GeoPosition(100.0, 1.0),
                    new GeoPosition(100.0, 0.0)
                ))
            )
        );
        GeoPolygonCollection avoidAreas = new GeoPolygonCollection(polygons);
        RouteDirectionsParameters parameters = new RouteDirectionsParameters()
            .setSupportingPoints(supportingPoints)
            .setAvoidVignette(Arrays.asList("AUS", "CHE"))
            .setAvoidAreas(avoidAreas);
        StepVerifier.create(client.getRouteDirectionsWithAdditionalParametersWithResponse(routeOptions,parameters))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async get route range
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteRange(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452,5.86605), 6000.0);
        StepVerifier.create(client.getRouteRange(rangeOptions))
        .assertNext(actualResults -> {
            try {
                validateGetRouteRange(TestUtils.getExpectedRouteRange(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get route range");
            }
        }).verifyComplete();
    }

    // Test async get route range with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncGetRouteRangeWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452,5.86605), 6000.0);
        StepVerifier.create(client.getRouteRangeWithResponse(rangeOptions))
                .assertNext(response ->
                {
                    try {
                        validateGetRouteRangeWithResponse(TestUtils.getExpectedRouteRange(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get route range");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncInvalidGetRouteRangeWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(-1000000,5.86605), 6000.0);
        StepVerifier.create(client.getRouteRangeWithResponse(rangeOptions))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }
    
    // Test async begin request route directions batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteDirectionsBatch(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.639987, -122.128384),
                new GeoPosition(47.621252, -122.184408),
                new GeoPosition(47.596437,-122.332000)))
            .setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);
        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.620659, -122.348934),
                new GeoPosition(47.610101, -122.342015)))
            .setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);
        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(40.759856, -73.985108),
                new GeoPosition(40.771136, -73.973506)))
            .setRouteType(RouteType.SHORTEST)
            .setTravelMode(TravelMode.PEDESTRIAN);
        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);
        PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> pollerFlux =
            client.beginRequestRouteDirectionsBatch(optionsList);
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        RouteDirectionsBatchResult actualResult = syncPoller.getFinalResult();
        RouteDirectionsBatchResult expectedResult = TestUtils.getExpectedBeginRequestRouteDirectionsBatch();
        validateBeginRequestRouteDirections(actualResult, expectedResult);
    }

    // Test async begin request route directions batch with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testAsyncBeginRequestRouteDirectionsBatchWithContext(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        RouteAsyncClient client = getRouteAsyncClient(httpClient, serviceVersion);
        RouteDirectionsOptions options1 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.639987, -122.128384),
                new GeoPosition(47.621252, -122.184408),
                new GeoPosition(47.596437,-122.332000)))
            .setRouteType(RouteType.FASTEST)
            .setTravelMode(TravelMode.CAR)
            .setMaxAlternatives(5);
        RouteDirectionsOptions options2 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(47.620659, -122.348934),
                new GeoPosition(47.610101, -122.342015)))
            .setRouteType(RouteType.ECONOMY)
            .setTravelMode(TravelMode.BICYCLE)
            .setUseTrafficData(false);
        RouteDirectionsOptions options3 = new RouteDirectionsOptions(
            Arrays.asList(new GeoPosition(40.759856, -73.985108),
                new GeoPosition(40.771136, -73.973506)))
            .setRouteType(RouteType.SHORTEST)
            .setTravelMode(TravelMode.PEDESTRIAN);
        List<RouteDirectionsOptions> optionsList = Arrays.asList(options1, options2, options3);
        PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> pollerFlux =
            client.beginRequestRouteDirectionsBatch(optionsList, null);
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        RouteDirectionsBatchResult actualResult = syncPoller.getFinalResult();
        RouteDirectionsBatchResult expectedResult = TestUtils.getExpectedBeginRequestRouteDirectionsBatch();
        validateBeginRequestRouteDirections(actualResult, expectedResult);
    }
}