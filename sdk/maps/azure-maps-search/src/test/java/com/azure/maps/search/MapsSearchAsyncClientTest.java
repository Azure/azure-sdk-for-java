package com.azure.maps.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressOptions;
import com.azure.maps.search.models.SearchAddressOptions;
import com.azure.maps.search.models.SearchAlongRouteOptions;
import com.azure.maps.search.models.SearchInsideGeometryOptions;
import com.azure.maps.search.models.SearchNearbyPointsOfInterestOptions;
import com.azure.maps.search.models.SearchPointOfInterestCategoryOptions;
import com.azure.maps.search.models.SearchPointOfInterestOptions;
import com.azure.maps.search.models.SearchStructuredAddressOptions;
import com.azure.maps.search.models.StructuredAddress;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

public class MapsSearchAsyncClientTest extends MapsSearchClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @BeforeAll
    public static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    public static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    private MapsSearchAsyncClient getMapsSearchAsyncClient(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        return getMapsSearchAsyncClientBuilder(httpClient, serviceVersion).buildAsyncClient();
    }

    // Test async get polygons
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncGetMultiPolygons(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<String> geometryIds = Arrays.asList("8bceafe8-3d98-4445-b29b-fd81d3e9adf5", "00005858-5800-1200-0000-0000773694ca");
        StepVerifier.create(client.getPolygons(geometryIds))
        .assertNext(actualResults -> {
            try {
                validateGetPolygons(TestUtils.getMultiPolygonsResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get polygon from json file");
            }
        }).verifyComplete();
    }

    // Test async get polygons with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncGetPolygonsWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<String> geometryIds = Arrays.asList("8bceafe8-3d98-4445-b29b-fd81d3e9adf5", "00005858-5800-1200-0000-0000773694ca");
        StepVerifier.create(client.getPolygonsWithResponse(geometryIds, Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateGetPolygonsWithResponse(TestUtils.getMultiPolygonsResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get polygon from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidGetPolygonsWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<String> geometryIds = new ArrayList<>();
        StepVerifier.create(client.getPolygonsWithResponse(geometryIds, Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async fuzzy search
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncFuzzySearch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.fuzzySearch(new FuzzySearchOptions("starbucks"))).assertNext(actualResults ->
        {
            try {
                validateFuzzySearch(TestUtils.getExpectedFuzzySearchResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test fuzzy search with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncFuzzySearchWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.fuzzySearchWithResponse(new FuzzySearchOptions("starbucks"), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateFuzzySearchWithResponse(TestUtils.getExpectedFuzzySearchResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidFuzzySearchWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.fuzzySearchWithResponse(new FuzzySearchOptions(""), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search point of interest
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterest(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterest(new SearchPointOfInterestOptions("caviar lobster pasta", new GeoPosition(-121.97483, 36.98844)))).assertNext(actualResults ->
        {
            try {
                validateSearchPointOfInterest(TestUtils.getExpectedSearchPointOfInterestResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search point of interest with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterestWithResponse(new SearchPointOfInterestOptions("caviar lobster pasta", new GeoPosition(-121.97483, 36.98844)), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateSearchPointOfInterestWithResponse(TestUtils.getExpectedSearchPointOfInterestResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterestWithResponse(new SearchPointOfInterestOptions("", new GeoPosition(-121.97483, 36.98844)), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search nearby point of interest
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchNearbyPointOfInterest(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchNearbyPointOfInterest(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270)))).assertNext(actualResults ->
        {
            try {
                validateSearchNearbyPointOfInterest(TestUtils.getExpectedSearchNearbyPointOfInterestResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search nearby point of interest with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchNearbyPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchNearbyPointOfInterestWithResponse(new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270)), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateSearchNearbyPointOfInterestWithResponse(TestUtils.getExpectedSearchNearbyPointOfInterestResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchNearbyPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchNearbyPointOfInterestWithResponse(new SearchNearbyPointsOfInterestOptions(new GeoPosition(-100, -100)), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search point of interest category
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterestCategory(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterestCategory(
            new SearchPointOfInterestCategoryOptions("atm", new GeoPosition(-74.011454, 40.706270)))).assertNext(actualResults ->
        {
            try {
                validateSearchPointOfInterestCategory(TestUtils.getExpectedSearchPointOfInterestCategoryResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search point of interest category with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterestCategoryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("atm", new GeoPosition(-74.011454, 40.706270)), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateSearchPointOfInterestCategoryWithResponse(TestUtils.getExpectedSearchPointOfInterestCategoryResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchPointOfInterestCategoryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("", new GeoPosition(-100, -100)), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async get point of interest category tree
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterestCategoryTree(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getPointOfInterestCategoryTree(null)).assertNext(actualResults ->
        {
            try {
                validateSearchPointOfInterestCategoryTree(TestUtils.getExpectedSearchPointOfInterestCategoryTreeResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get PointOfInterestCategoryTreeResult from json file");
            }
        }).verifyComplete();
    }

    // Test async get point of interest category tree with response
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchPointOfInterestCategoryTreeWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.getPointOfInterestCategoryTreeWithResponse("pizza", Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateSearchPointOfInterestCategoryTreeWithResponse(TestUtils.getExpectedSearchPointOfInterestCategoryTreeResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get PointOfInterestCategoryTreeResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Test async search address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchAddress(new SearchAddressOptions("NE 24th Street, Redmond, WA 98052"))).assertNext(actualResults ->
        {
            try {
                validateSearchAddress(TestUtils.getExpectedSearchAddressResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search address with response
    // Case 1:
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchAddressWithResponse(new SearchAddressOptions("NE 24th Street, Redmond, WA 98052"), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateSearchAddressWithResponse(TestUtils.getExpectedSearchAddressResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchAddressWithResponse(new SearchAddressOptions(""), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async reverse search address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncReverseSearchAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)))).assertNext(actualResults ->
        {
            try {
                validateReverseSearchAddress(TestUtils.getExpectedReverseSearchAddressResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get ReverseSearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async reverse search address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncReverseSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateReverseSearchAddressWithResponse(TestUtils.getExpectedReverseSearchAddressResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get ReverseSearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidReverseSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, -100)), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async reverse search cross street address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncReverseSearchCrossStreetAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)))).assertNext(actualResults ->
        {
            try {
                validateReverseSearchCrossStreetAddress(TestUtils.getExpectedReverseSearchCrossStreetAddressResults(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get ReverseSearchCrossStreetAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async reverse search cross street address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncReverseSearchCrossStreetAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)), Context.NONE))
                .assertNext(response ->
                {
                    try {
                        validateReverseSearchCrossStreetAddressWithResponse(TestUtils.getExpectedReverseSearchCrossStreetAddressResults(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get ReverseSearchCrossStreetAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidReverseSearchCrossStreetAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, -100)), Context.NONE))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search structured address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchStructuredAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchStructuredAddress(new StructuredAddress("US"), null)).assertNext(actualResults ->
        {
            try {
                validateSearchStructuredAddress(TestUtils.getExpectedSearchStructuredAddress(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search structured address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchStructuredAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchStructuredAddressWithResponse(
            new StructuredAddress("US"),
            new SearchStructuredAddressOptions(), null))
                .assertNext(response ->
                {
                    try {
                        validateSearchStructuredAddressWithResponse(TestUtils.getExpectedSearchStructuredAddress(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchStructuredAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        StepVerifier.create(client.searchStructuredAddressWithResponse(new StructuredAddress(""), null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search inside geometry
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchInsideGeometry(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        StepVerifier.create(client.searchInsideGeometry(
            new SearchInsideGeometryOptions("pizza", obj))).assertNext(actualResults ->
        {
            try {
                validateSearchInsideGeometry(TestUtils.getExpectedSearchInsideGeometry(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search inside geometry with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchInsideGeometryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        StepVerifier.create(client.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("pizza", obj), null))
                .assertNext(response ->
                {
                    try {
                        validateSearchInsideGeometryWithResponse(TestUtils.getExpectedSearchInsideGeometry(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchInsideGeometryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        StepVerifier.create(client.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("", obj), null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async search along route
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchAlongRoute(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        StepVerifier.create(client.searchAlongRoute(new SearchAlongRouteOptions("burger", 1000, obj))).assertNext(actualResults ->
        {
            try {
                validateSearchAlongRoute(TestUtils.getExpectedSearchAlongRoute(), actualResults);
            } catch (IOException e) {
                Assertions.fail("Unable to get SearchAddressResult from json file");
            }
        }).verifyComplete();
    }

    // Test async search along route with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncSearchAlongRouteWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        StepVerifier.create(client.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("burger", 1000, obj), null))
                .assertNext(response ->
                {
                    try {
                        validateSearchAlongRouteWithResponse(TestUtils.getExpectedSearchAlongRoute(), 200, response);
                    } catch (IOException e) {
                        Assertions.fail("Unable to get SearchAddressResult from json file");
                    }
                })
                .verifyComplete();
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncInvalidSearchAlongRouteWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        StepVerifier.create(client.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("", 1000, obj), null))
                .verifyErrorSatisfies(ex -> {
                    final HttpResponseException httpResponseException = (HttpResponseException) ex;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                });
    }

    // Test async begin fuzzy search batch
     @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
     @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
     public void testAsyncBeginFuzzySearchBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<FuzzySearchOptions> fuzzyOptionsList = new ArrayList<>();
        fuzzyOptionsList.add(new FuzzySearchOptions("atm", new GeoPosition(-122.128362, 47.639769))
             .setRadiusInMeters(5000).setTop(5));
        fuzzyOptionsList.add(new FuzzySearchOptions("Statue of Liberty").setTop(2));
        fuzzyOptionsList.add(new FuzzySearchOptions("Starbucks", new GeoPosition(-122.128362, 47.639769))
             .setRadiusInMeters(5000));
        PollerFlux<BatchSearchResult, BatchSearchResult> pollerFlux = client.beginFuzzySearchBatch(fuzzyOptionsList);
        SyncPoller<BatchSearchResult, BatchSearchResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        BatchSearchResult actualResult =  syncPoller.getFinalResult();
        BatchSearchResult expectedResult = TestUtils.getExpectedBeginFuzzySearchBatch();
        validateBeginFuzzySearchBatch(expectedResult, actualResult);
     }

    // Test async begin search address batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncBeginSearchAddressBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<SearchAddressOptions> searchAddressOptionsList = new ArrayList<>();
        searchAddressOptionsList.add(new SearchAddressOptions("400 Broad St, Seattle, WA 98109").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("One, Microsoft Way, Redmond, WA 98052").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("350 5th Ave, New York, NY 10118").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("1 Main Street"));
        PollerFlux<BatchSearchResult, BatchSearchResult> pollerFlux = client.beginSearchAddressBatch(searchAddressOptionsList);
        SyncPoller<BatchSearchResult, BatchSearchResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        BatchSearchResult actualResult = syncPoller.getFinalResult();
        BatchSearchResult expectedResult = TestUtils.getExpectedBeginSearchAddressBatch();
        validateBeginSearchAddressBatch(expectedResult, actualResult);
    }

    // Test async begin reverse search address batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testAsyncBeginReverSearchAddressBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        MapsSearchAsyncClient client = getMapsSearchAsyncClient(httpClient, serviceVersion);
        List<ReverseSearchAddressOptions> reverseOptionsList = new ArrayList<>();
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(2.294911, 48.858561)));
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.127896, 47.639765)).setRadiusInMeters(5000));
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.348170, 47.621028)));
        PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> pollerFlux = client.beginReverseSearchAddressBatch(reverseOptionsList);
        SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> syncPoller = setPollInterval(pollerFlux.getSyncPoller());
        BatchReverseSearchResult actualResult = syncPoller.getFinalResult();
        BatchReverseSearchResult expectedResult = TestUtils.getExpectedReverseSearchAddressBatch();
        validateBeginReverseSearchAddressBatch(expectedResult, actualResult);
    }
}
