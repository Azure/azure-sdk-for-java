package com.azure.maps.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.PointOfInterestCategory;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.Polygon;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAddressResultItem;

public class MapsSearchClientTestBase extends TestBase {
    static final String FAKE_API_KEY = "1234567890";

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

    MapsSearchClientBuilder getMapsSearchAsyncClientBuilder(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
            // System.out.println("beginning httpclient " + httpClient);
            MapsSearchClientBuilder builder = new MapsSearchClientBuilder()
            //.httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);
            String endpoint = getEndpoint();
            if (getEndpoint() != null) {
                builder.endpoint(endpoint);
            }
            // System.out.println("after httpclient " + httpClient);
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

    protected <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return syncPoller.setPollInterval(durationTestMode);
    }

    static void validateGetPolygons(List<Polygon> expected, List<Polygon> actual) {
        assertEquals(expected.size(), actual.size());
        List<String> ids = Arrays.asList(actual.get(0).getProviderID(), actual.get(1).getProviderID());
        assertTrue(ids.contains(expected.get(0).getProviderID()));
    }

    static void validateGetPolygonsWithResponse(List<Polygon> expected, int expectedStatusCode, Response<List<Polygon>> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateGetPolygons(expected, response.getValue());
    }

    static List<String> getStreetNameAndNumberList(List<SearchAddressResultItem> list) {
        List<String> streetNameAndNumberList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            streetNameAndNumberList.add(list.get(i).getAddress().getStreetNameAndNumber());
        }
        return streetNameAndNumberList;
    }

    static void validateFuzzySearch(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateFuzzySearchWithResponse(SearchAddressResult expected, int expectedStatusCode,Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateFuzzySearch(expected, response.getValue());
    }

    static void validateSearchPointOfInterest(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchPointOfInterestWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchPointOfInterest(expected, response.getValue());
    }

    static void validateSearchNearbyPointOfInterest(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchNearbyPointOfInterestWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchNearbyPointOfInterest(expected, response.getValue());
    }

    static void validateSearchPointOfInterestCategory(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchPointOfInterestCategoryWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchPointOfInterestCategory(expected, response.getValue());
    }

    static void validateSearchPointOfInterestCategoryTree(PointOfInterestCategoryTreeResult expected, PointOfInterestCategoryTreeResult actual) {
        assertEquals(expected.getCategories().size(), actual.getCategories().size());
        List<PointOfInterestCategory> pointOfInterestCategoryList = actual.getCategories();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < pointOfInterestCategoryList.size(); i++) {
            names.add(pointOfInterestCategoryList.get(i).getName());
        }
        assertTrue(names.contains(expected.getCategories().get(0).getName()));
    }

    static void validateSearchPointOfInterestCategoryTreeWithResponse(PointOfInterestCategoryTreeResult expected, int expectedStatusCode, Response<PointOfInterestCategoryTreeResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchPointOfInterestCategoryTree(expected, response.getValue());
    }

    static void validateSearchAddress(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchAddressWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchAddress(ReverseSearchAddressResult expected, ReverseSearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchAddressWithResponse(ReverseSearchAddressResult expected, int expectedStatusCode, Response<ReverseSearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateReverseSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchCrossStreetAddress(ReverseSearchCrossStreetAddressResult expected, ReverseSearchCrossStreetAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchCrossStreetAddressWithResponse(ReverseSearchCrossStreetAddressResult expected, int expectedStatusCode, Response<ReverseSearchCrossStreetAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateReverseSearchCrossStreetAddress(expected, response.getValue());
    }

    static void validateSearchStructuredAddress(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchStructuredAddressWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());
    }

    static void validateSearchInsideGeometry(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchInsideGeometryWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());

    }

    static void validateSearchAlongRoute(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());
        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());
        assertTrue(streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchAlongRouteWithResponse(SearchAddressResult expected, int expectedStatusCode, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());
    }


    static void validateBeginFuzzySearchBatch(BatchSearchResult expected, BatchSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }

    static void validateBeginSearchAddressBatch(BatchSearchResult expected, BatchSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }

    static void validateBeginReverseSearchAddressBatch(BatchReverseSearchResult expected, BatchReverseSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}
