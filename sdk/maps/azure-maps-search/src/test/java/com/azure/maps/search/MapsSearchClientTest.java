package com.azure.maps.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.Polygon;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressOptions;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressOptions;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAlongRouteOptions;
import com.azure.maps.search.models.SearchInsideGeometryOptions;
import com.azure.maps.search.models.SearchNearbyPointsOfInterestOptions;
import com.azure.maps.search.models.SearchPointOfInterestCategoryOptions;
import com.azure.maps.search.models.SearchPointOfInterestOptions;
import com.azure.maps.search.models.SearchStructuredAddressOptions;
import com.azure.maps.search.models.StructuredAddress;
import com.nimbusds.jose.shaded.json.parser.ParseException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MapsSearchClientTest extends MapsSearchClientTestBase {

    private MapsSearchClient client;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private MapsSearchClient getMapsSearchClient(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        return getMapsSearchAsyncClientBuilder(httpClient, serviceVersion).buildClient();
    }

    // Test get polygons
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetMultiPolygons(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException, ParseException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<String> geometryIds = Arrays.asList("8bceafe8-3d98-4445-b29b-fd81d3e9adf5", "00005858-5800-1200-0000-0000773694ca");
        List<Polygon> actualResult = client.getPolygons(geometryIds);
        List<Polygon> expectedResult = TestUtils.getMultiPolygonsResults();
        validateGetPolygons(expectedResult, actualResult);
    }

    // Test get polygons with response
    // Case 1: Response 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetPolygonsWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<String> geometryIds = Arrays.asList("8bceafe8-3d98-4445-b29b-fd81d3e9adf5", "00005858-5800-1200-0000-0000773694ca");
        validateGetPolygonsWithResponse(TestUtils.getMultiPolygonsResults(), 200, client.getPolygonsWithResponse(geometryIds, Context.NONE));
    }

    // Case 2: Respone 400, incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testGetInvalidInputPolygonsWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<String> geometryIds = new ArrayList<>();
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.getPolygonsWithResponse(geometryIds, Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test fuzzy search
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testFuzzySearch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.fuzzySearch(new FuzzySearchOptions("starbucks"));
        SearchAddressResult expectedResult = TestUtils.getExpectedFuzzySearchResults();
        validateFuzzySearch(expectedResult, actualResult);
    }

    // Test fuzzy search with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testFuzzySearchWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateFuzzySearchWithResponse(TestUtils.getExpectedFuzzySearchResults(), 200, client.fuzzySearchWithResponse(new FuzzySearchOptions("starbucks"), Context.NONE));
    }

    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidFuzzySearchWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.fuzzySearchWithResponse(new FuzzySearchOptions(""), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search point of interest
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterest(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.searchPointOfInterest(new SearchPointOfInterestOptions("caviar lobster pasta", new GeoPosition(-121.97483, 36.98844)));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchPointOfInterestResults();
        validateSearchPointOfInterest(expectedResult, actualResult);
    }

    // Test search point of interest with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchPointOfInterestWithResponse(TestUtils.getExpectedSearchPointOfInterestResults(), 200, client.searchPointOfInterestWithResponse(new SearchPointOfInterestOptions("caviar lobster pasta", new GeoPosition(-121.97483, 36.98844)), Context.NONE));
    }

    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchPointOfInterestWithResponse(new SearchPointOfInterestOptions("", new GeoPosition(0.0, 0.0)), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search nearby point of interest
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchNearbyPointOfInterest(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.searchNearbyPointOfInterest(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270)));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchNearbyPointOfInterestResults();
        validateSearchNearbyPointOfInterest(expectedResult, actualResult);
    }

    // Test search nearby point of interest with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchNearbyPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchNearbyPointOfInterestWithResponse(TestUtils.getExpectedSearchNearbyPointOfInterestResults(), 200, client.searchNearbyPointOfInterestWithResponse(new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270)), Context.NONE));
    }

    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchNearbyPointOfInterestWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchNearbyPointOfInterestWithResponse(new SearchNearbyPointsOfInterestOptions(new GeoPosition(-100, -100)), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search point of interest category
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterestCategory(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.searchPointOfInterestCategory(
            new SearchPointOfInterestCategoryOptions("atm", new GeoPosition(-74.011454, 40.706270)));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchPointOfInterestCategoryResults();
        validateSearchPointOfInterestCategory(expectedResult, actualResult);
    }

    // Test search point of interest category with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterestCategoryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchPointOfInterestCategoryWithResponse(TestUtils.getExpectedSearchPointOfInterestCategoryResults(), 200, client.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("atm", new GeoPosition(-74.011454, 40.706270)), Context.NONE));
    }

    // Case 2: 400 incorrect input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchPointOfInterestCategoryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchPointOfInterestCategoryWithResponse(
                    new SearchPointOfInterestCategoryOptions("atm", new GeoPosition(-100, -100)), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test get point of interest category tree
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterestCategoryTree(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        PointOfInterestCategoryTreeResult actualResult = client.getPointOfInterestCategoryTree("pizza");
        PointOfInterestCategoryTreeResult expectedResult = TestUtils.getExpectedSearchPointOfInterestCategoryTreeResults();
        validateSearchPointOfInterestCategoryTree(expectedResult, actualResult);
    }

    // Test get point of interest category tree with response
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchPointOfInterestCategoryTreeWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchPointOfInterestCategoryTreeWithResponse(TestUtils.getExpectedSearchPointOfInterestCategoryTreeResults(), 200, client.getPointOfInterestCategoryTreeWithResponse("pizza", Context.NONE));
    }

    // Test search address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.searchAddress(new SearchAddressOptions("NE 24th Street, Redmond, WA 98052"));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchAddressResults();
        validateSearchAddress(expectedResult, actualResult);
    }

    // Test search address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchAddressWithResponse(TestUtils.getExpectedSearchAddressResults(), 200, client.searchAddressWithResponse(new SearchAddressOptions("NE 24th Street, Redmond, WA 98052"), Context.NONE));
    }

    // Case 2: 400 Invalid Input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchAddressWithResponse(new SearchAddressOptions(""), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test reverse search address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testReverseSearchAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        ReverseSearchAddressResult actualResult = client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)));
        ReverseSearchAddressResult expectedResult = TestUtils.getExpectedReverseSearchAddressResults();
        validateReverseSearchAddress(expectedResult, actualResult);
    }

    // Test reverse search address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testReverseSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateReverseSearchAddressWithResponse(TestUtils.getExpectedReverseSearchAddressResults(), 200, client.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337)), Context.NONE));
    }

    // Case 2: 400 Invalid Input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidReverseSearchAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.reverseSearchAddressWithResponse(
                    new ReverseSearchAddressOptions(new GeoPosition(-121.89, -100)), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test reverse search cross street address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testReverseSearchCrossStreetAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        ReverseSearchCrossStreetAddressResult actualResult = client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)));
        ReverseSearchCrossStreetAddressResult expectedResult = TestUtils.getExpectedReverseSearchCrossStreetAddressResults();
        validateReverseSearchCrossStreetAddress(expectedResult, actualResult);
    }

    // Test reverse search cross street address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testReverseSearchCrossStreetAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateReverseSearchCrossStreetAddressWithResponse(TestUtils.getExpectedReverseSearchCrossStreetAddressResults(), 200, client.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337)), Context.NONE));
    }

    // Case 2: 400 Invalid Input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidReverseSearchCrossStreetAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.reverseSearchCrossStreetAddressWithResponse(
                    new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, -100)), Context.NONE));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search structured address
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchStructuredAddress(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        SearchAddressResult actualResult = client.searchStructuredAddress(new StructuredAddress("US"), null);
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchStructuredAddress();
        validateSearchStructuredAddress(expectedResult, actualResult);
    }

    // Test search structured address with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchStructuredAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        validateSearchStructuredAddressWithResponse(
            TestUtils.getExpectedSearchStructuredAddress(),
            200,
        client.searchStructuredAddressWithResponse(
            new StructuredAddress("US"),
            new SearchStructuredAddressOptions(), null));
    }

    // Case 2: 400 Invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchStructuredAddressWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchStructuredAddressWithResponse(
                    new StructuredAddress(""),
                    new SearchStructuredAddressOptions(), null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search inside geometry
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchInsideGeometry(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        SearchAddressResult actualResult = client.searchInsideGeometry(
            new SearchInsideGeometryOptions("pizza", obj));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchInsideGeometry();
        validateSearchInsideGeometry(expectedResult, actualResult);
    }

    // Test search inside geometry with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchInsideGeometryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        validateSearchInsideGeometryWithResponse(TestUtils.getExpectedSearchInsideGeometry(), 200, client.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("pizza", obj), null));
    }

    // Case 2: 400 invalid input
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchInsideGeometryWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geoobjectone.json");
        GeoObject obj = TestUtils.getGeoObject(file);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchInsideGeometryWithResponse(
                    new SearchInsideGeometryOptions("", obj), null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test search along route
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchAlongRoute(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        SearchAddressResult actualResult = client.searchAlongRoute(new SearchAlongRouteOptions("burger", 1000, obj));
        SearchAddressResult expectedResult = TestUtils.getExpectedSearchAlongRoute();
        validateSearchAlongRoute(expectedResult, actualResult);
    }

    // Test search along route with response
    // Case 1: 200
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testSearchAlongRouteWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        validateSearchAlongRouteWithResponse(TestUtils.getExpectedSearchAddressResults(), 200, client.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("burger", 1000, obj), null));
    }

    // Case 2: 400
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testInvalidSearchAlongRouteWithResponse(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        File file = new File("src/main/resources/geolinestringone.json");
        GeoLineString obj = TestUtils.getGeoLineString(file);
        final HttpResponseException httpResponseException = assertThrows(HttpResponseException.class,
                () -> client.searchAlongRouteWithResponse(
                    new SearchAlongRouteOptions("", 1000, obj), null));
            assertEquals(400, httpResponseException.getResponse().getStatusCode());
    }

    // Test begin fuzzy search batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testBeginFuzzySearchBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<FuzzySearchOptions> fuzzyOptionsList = new ArrayList<>();
        fuzzyOptionsList.add(new FuzzySearchOptions("atm", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000).setTop(5));
        fuzzyOptionsList.add(new FuzzySearchOptions("Statue of Liberty").setTop(2));
        fuzzyOptionsList.add(new FuzzySearchOptions("Starbucks", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000));
        SyncPoller<BatchSearchResult, BatchSearchResult> syncPoller = client.beginFuzzySearchBatch(fuzzyOptionsList);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        BatchSearchResult actualResult = syncPoller.getFinalResult();
        BatchSearchResult expectedResult = TestUtils.getExpectedBeginFuzzySearchBatch();
        validateBeginFuzzySearchBatch(expectedResult, actualResult);
    }

    // Test begin search address batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testBeginSearchAddressBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<SearchAddressOptions> searchAddressOptionsList = new ArrayList<>();
        searchAddressOptionsList.add(new SearchAddressOptions("400 Broad St, Seattle, WA 98109").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("One, Microsoft Way, Redmond, WA 98052").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("350 5th Ave, New York, NY 10118").setTop(3));
        searchAddressOptionsList.add(new SearchAddressOptions("1 Main Street"));
        SyncPoller<BatchSearchResult, BatchSearchResult> syncPoller = client.beginSearchAddressBatch(searchAddressOptionsList);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        BatchSearchResult actualResult = syncPoller.getFinalResult();
        BatchSearchResult expectedResult = TestUtils.getExpectedBeginSearchAddressBatch();
        validateBeginSearchAddressBatch(expectedResult, actualResult);
    }

    // Test begin reverse search address batch
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.maps.search.TestUtils#getTestParameters")
    public void testBeginReverseSearchAddressBatch(HttpClient httpClient, MapsSearchServiceVersion serviceVersion) throws IOException {
        client = getMapsSearchClient(httpClient, serviceVersion);
        List<ReverseSearchAddressOptions> reverseOptionsList = new ArrayList<>();
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(2.294911, 48.858561)));
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.127896, 47.639765)).setRadiusInMeters(5000));
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.348170, 47.621028)));
        SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> syncPoller = client.beginReverseSearchAddressBatch(reverseOptionsList);
        syncPoller = setPollInterval(syncPoller);
        syncPoller.waitForCompletion();
        BatchReverseSearchResult actualResult = syncPoller.getFinalResult();
        BatchReverseSearchResult expectedResult = TestUtils.getExpectedReverseSearchAddressBatch();
        validateBeginReverseSearchAddressBatch(expectedResult, actualResult);
    }
}
