// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.MapsPolygon;
import com.azure.maps.search.models.PointOfInterestCategory;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAddressResultItem;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapsSearchClientTestBase extends TestProxyTestBase {
    Duration durationTestMode;

    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = Duration.ofMillis(1);
        } else {
            durationTestMode = TestUtils.DEFAULT_POLL_INTERVAL;
        }
    }

    MapsSearchClientBuilder getMapsSearchAsyncClientBuilder(HttpClient httpClient,
        MapsSearchServiceVersion serviceVersion) {
        MapsSearchClientBuilder builder = modifyBuilder(httpClient, new MapsSearchClientBuilder()).serviceVersion(
            serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }

        return builder;
    }

    MapsSearchClientBuilder modifyBuilder(HttpClient httpClient, MapsSearchClientBuilder builder) {
        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(Collections.singletonList(
                new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        builder.retryPolicy(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .credential(new DefaultAzureCredentialBuilder().build())
                .mapsClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        } else if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential())
                .mapsClientId("testSearchClient");
        } else {
            builder.credential(new AzurePowerShellCredentialBuilder().build())
                .mapsClientId(Configuration.getGlobalConfiguration().get("MAPS_CLIENT_ID"));
        }

        return builder.httpClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
    }

    protected <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return syncPoller.setPollInterval(durationTestMode);
    }

    static void validateGetPolygons(List<MapsPolygon> expected, List<MapsPolygon> actual) {
        assertEquals(expected.size(), actual.size());

        List<String> ids = Arrays.asList(actual.get(0).getProviderId(), actual.get(1).getProviderId());

        assertTrue(ids.contains(expected.get(0).getProviderId()));
    }

    static void validateGetPolygonsWithResponse(List<MapsPolygon> expected, Response<List<MapsPolygon>> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateGetPolygons(expected, response.getValue());
    }

    static List<String> getStreetNameAndNumberList(List<SearchAddressResultItem> list) {
        List<String> streetNameAndNumberList = new ArrayList<>();

        for (SearchAddressResultItem searchAddressResultItem : list) {
            streetNameAndNumberList.add(searchAddressResultItem.getAddress().getStreetNameAndNumber());
        }

        return streetNameAndNumberList;
    }

    static void validateFuzzySearch(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateFuzzySearchWithResponse(SearchAddressResult expected, Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateFuzzySearch(expected, response.getValue());
    }

    static void validateSearchPointOfInterest(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchPointOfInterestWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchPointOfInterest(expected, response.getValue());
    }

    static void validateSearchNearbyPointOfInterest(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchNearbyPointOfInterestWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchNearbyPointOfInterest(expected, response.getValue());
    }

    static void validateSearchPointOfInterestCategory(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchPointOfInterestCategoryWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchPointOfInterestCategory(expected, response.getValue());
    }

    static void validateSearchPointOfInterestCategoryTree(PointOfInterestCategoryTreeResult expected,
        PointOfInterestCategoryTreeResult actual) {
        List<PointOfInterestCategory> pointOfInterestCategoryList = actual.getCategories();
        List<String> names = new ArrayList<>();

        for (PointOfInterestCategory pointOfInterestCategory : pointOfInterestCategoryList) {
            names.add(pointOfInterestCategory.getName());
        }

        assertTrue(names.contains(expected.getCategories().get(0).getName()));
    }

    static void validateSearchPointOfInterestCategoryTreeWithResponse(PointOfInterestCategoryTreeResult expected,
        Response<PointOfInterestCategoryTreeResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchPointOfInterestCategoryTree(expected, response.getValue());
    }

    static void validateSearchAddress(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchAddressWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchAddress(ReverseSearchAddressResult expected, ReverseSearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchAddressWithResponse(ReverseSearchAddressResult expected,
        Response<ReverseSearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateReverseSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchCrossStreetAddress(ReverseSearchCrossStreetAddressResult expected,
        ReverseSearchCrossStreetAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchCrossStreetAddressWithResponse(ReverseSearchCrossStreetAddressResult expected,
        Response<ReverseSearchCrossStreetAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateReverseSearchCrossStreetAddress(expected, response.getValue());
    }

    static void validateSearchStructuredAddress(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchStructuredAddressWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());
    }

    static void validateSearchInsideGeometry(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchInsideGeometryWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());

    }

    static void validateSearchAlongRoute(SearchAddressResult expected, SearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getResults());
        assertEquals(expected.getResults().size(), actual.getResults().size());

        List<String> streetNameAndNumberList = getStreetNameAndNumberList(actual.getResults());

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateSearchAlongRouteWithResponse(SearchAddressResult expected,
        Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        validateSearchStructuredAddress(expected, response.getValue());
    }

    static void validateBeginFuzzySearchBatch(BatchSearchResult expected, BatchSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }

    static void validateBeginSearchAddressBatch(BatchSearchResult expected, BatchSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }

    static void validateBeginReverseSearchAddressBatch(BatchReverseSearchResult expected,
        BatchReverseSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}
