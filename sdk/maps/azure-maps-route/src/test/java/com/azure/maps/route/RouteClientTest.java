package com.azure.maps.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
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
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteRangeResult;
import com.azure.maps.route.models.RouteType;
import com.azure.maps.route.models.TravelMode;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class RouteClientTest extends RouteTestBase {
    private RouteClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private RouteClient getRouteClient(HttpClient httpClient, RouteServiceVersion serviceVersion) {
        return getRouteAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test begin request route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginRequestRouteMatrix(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
        new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
        new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = client.beginRequestRouteMatrix(options);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteMatrixResult actualResult = syncPoller.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedBeginRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test begin request route matrix with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginRequestRouteMatrixWithContext(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
        new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
        new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = client.beginRequestRouteMatrix(options, null);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteMatrixResult actualResult = syncPoller.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedBeginRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test begin get route matrix
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginGetRouteMatrix(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
        new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
        new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = client.beginRequestRouteMatrix(options);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteMatrixResult routeMatrixResult = syncPoller.getFinalResult();
        String routeMatrixId = routeMatrixResult.getMatrixId();
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller2 = client.beginGetRouteMatrix(routeMatrixId);
        syncPoller2 = setPollInterval(syncPoller2);
        syncPoller2.waitForCompletion();
        RouteMatrixResult actualResult = syncPoller2.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedGetRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test begin get route matrix with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginGetRouteMatrixWithContext(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteMatrixQuery matrixQuery = new RouteMatrixQuery();
        GeoPointCollection origins = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85106, 52.36006), 
        new GeoPoint(4.85056, 52.36187)));
        GeoPointCollection destinations = new GeoPointCollection(Arrays.asList(new GeoPoint(4.85003, 52.36241),
        new GeoPoint(13.42937, 52.50931)));
        matrixQuery.setDestinations(destinations);
        matrixQuery.setOrigins(origins);
        RouteMatrixOptions options = new RouteMatrixOptions(matrixQuery);
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller = client.beginRequestRouteMatrix(options, null);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteMatrixResult routeMatrixResult = syncPoller.getFinalResult();
        String routeMatrixId = routeMatrixResult.getMatrixId();
        SyncPoller<RouteMatrixResult, RouteMatrixResult> syncPoller2 = client.beginGetRouteMatrix(routeMatrixId);
        syncPoller2 = setPollInterval(syncPoller2);
        syncPoller2.waitForCompletion();
        RouteMatrixResult actualResult = syncPoller2.getFinalResult();
        RouteMatrixResult expectedResult = TestUtils.getExpectedGetRequestRouteMatrix();
        validateBeginRequestRouteMatrix(expectedResult, actualResult);
    }

    // Test get route directions
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteDirections(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        RouteDirections actualResult = client.getRouteDirections(routeOptions);
        RouteDirections expectedResult = TestUtils.getExpectedRouteDirections();
        validateGetRouteDirections(expectedResult, actualResult);
    }

    // Test get route directions with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteDirectionsWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(13.42936, 52.50931),
            new GeoPosition(13.43872, 52.50274));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        validateGetRouteDirectionsWithResponse(TestUtils.getExpectedRouteDirections(), 200, client.getRouteDirectionsWithResponse(routeOptions, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testInvalidGetRouteDirectionsWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(-1000000, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getRouteDirectionsWithResponse(routeOptions, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get route directions with additional parameters
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteDirectionsWithAdditionalParameters(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
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
        RouteDirections actualResult = client.getRouteDirectionsWithAdditionalParameters(routeOptions,parameters);
        RouteDirections expectedResult = TestUtils.getExpectedRouteDirectionsWithAdditionalParameters();
        validateGetRouteDirections(expectedResult, actualResult);
    }

    // Test get route directions with additional parameters with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
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
        validateGetRouteDirectionsWithResponse(TestUtils.getExpectedRouteDirections(), 200, client.getRouteDirectionsWithAdditionalParametersWithResponse(routeOptions,parameters, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testInvalidGetRouteDirectionsWithAdditionalParametersWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        List<GeoPosition> routePoints = Arrays.asList(
            new GeoPosition(52.50931, 13.42936),
            new GeoPosition(52.50274, 13.43872));
        RouteDirectionsOptions routeOptions = new RouteDirectionsOptions(routePoints);
        GeoCollection supportingPoints = new GeoCollection(Arrays.asList(new GeoPoint(13.42936, 52.5093),
            new GeoPoint(13.42859, 52.50844)));
        List<GeoPolygon> polygons = Arrays.asList(
            new GeoPolygon(
                new GeoLinearRing(Arrays.asList(
                    new GeoPosition(-100000000, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.489368981370724),
                    new GeoPosition(-122.00454711914061, 47.65151268066222),
                    new GeoPosition(-122.39456176757811, 47.65151268066222),
                    new GeoPosition(-100000000, 47.489368981370724)
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
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getRouteDirectionsWithAdditionalParametersWithResponse(routeOptions,parameters, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get route range
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteRange(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452,5.86605), 6000.0);
        RouteRangeResult actualResult = client.getRouteRange(rangeOptions);
        RouteRangeResult expectedResult = TestUtils.getExpectedRouteRange();
        validateGetRouteRange(expectedResult, actualResult);
    }

    // Test get route range with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testGetRouteRangeWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452,5.86605), 6000.0);
        validateGetRouteRangeWithResponse(TestUtils.getExpectedRouteRange(), 200, client.getRouteRangeWithResponse(rangeOptions, null));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testInvalidGetRouteRangeWithResponse(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
        RouteRangeOptions rangeOptions = new RouteRangeOptions(new GeoPosition(50.97452,-10000000), 6000.0);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getRouteRangeWithResponse(rangeOptions, null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test begin request route directions batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginRequestRouteDirectionsBatch(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
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
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> syncPoller =
            client.beginRequestRouteDirectionsBatch(optionsList);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteDirectionsBatchResult actualResult = syncPoller.getFinalResult();
        RouteDirectionsBatchResult expectedResult = TestUtils.getExpectedBeginRequestRouteDirectionsBatch();
        validateBeginRequestRouteDirections(actualResult, expectedResult);
    }

    // Test begin request route directions batch with context
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.route.TestUtils#getTestParameters")
    public void testBeginRequestRouteDirectionsBatchWithContext(HttpClient httpClient, RouteServiceVersion serviceVersion) throws IOException {
        client = getRouteClient(httpClient, serviceVersion);
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
        SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> syncPoller =
            client.beginRequestRouteDirectionsBatch(optionsList, null);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        RouteDirectionsBatchResult actualResult = syncPoller.getFinalResult();
        RouteDirectionsBatchResult expectedResult = TestUtils.getExpectedBeginRequestRouteDirectionsBatch();
        validateBeginRequestRouteDirections(actualResult, expectedResult);
    }
}