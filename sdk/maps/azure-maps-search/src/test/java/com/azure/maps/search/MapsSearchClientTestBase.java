// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AzureKeyCredentialPolicy;
import com.azure.core.http.policy.ExponentialBackoff;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
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
    private static final String SDK_NAME = "client_name";
    private static final String SDK_VERSION = "client_version";

    static final String FAKE_API_KEY = "fakeKeyPlaceholder";

    static InterceptorManager interceptorManagerTestBase;

    Duration durationTestMode;

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
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder()
            .pipeline(getHttpPipeline(httpClient))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.endpoint("https://localhost:8080");
        }

        return builder;
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            interceptorManager.addSanitizers(
                Collections.singletonList(
                    new TestProxySanitizer("subscription-key", ".+", "REDACTED", TestProxySanitizerType.HEADER)));
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(
                new CustomMatcher().setHeadersKeyOnlyMatch(Collections.singletonList("subscription-key")));
            interceptorManager.addMatchers(customMatchers);
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        policies.add(new UserAgentPolicy(null, SDK_NAME, SDK_VERSION, Configuration.getGlobalConfiguration().clone()));

        HttpPolicyProviders.addBeforeRetryPolicies(policies);

        policies.add(new RetryPolicy(new ExponentialBackoff(5, Duration.ofSeconds(2), Duration.ofSeconds(16))));
        policies.add(
            new AzureKeyCredentialPolicy(
                MapsSearchClientBuilder.MAPS_SUBSCRIPTION_KEY,
                new AzureKeyCredential(interceptorManager.isPlaybackMode()
                                       ? FAKE_API_KEY
                                       : Configuration.getGlobalConfiguration().get("SUBSCRIPTION_KEY"))));

        HttpPolicyProviders.addAfterRetryPolicies(policies);

        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    protected <T, U> SyncPoller<T, U> setPollInterval(SyncPoller<T, U> syncPoller) {
        return syncPoller.setPollInterval(durationTestMode);
    }

    static void validateGetPolygons(List<MapsPolygon> expected, List<MapsPolygon> actual) {
        assertEquals(expected.size(), actual.size());

        List<String> ids = Arrays.asList(actual.get(0).getProviderId(), actual.get(1).getProviderId());

        assertTrue(ids.contains(expected.get(0).getProviderId()));
    }

    static void validateGetPolygonsWithResponse(List<MapsPolygon> expected, int expectedStatusCode,
                                                Response<List<MapsPolygon>> response) {
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

        assertTrue(
            streetNameAndNumberList.contains(expected.getResults().get(0).getAddress().getStreetNameAndNumber()));
    }

    static void validateFuzzySearchWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchPointOfInterestWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                          Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchNearbyPointOfInterestWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                                Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchPointOfInterestCategoryWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                                  Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchPointOfInterestCategory(expected, response.getValue());
    }

    static void validateSearchPointOfInterestCategoryTree(PointOfInterestCategoryTreeResult expected,
                                                          PointOfInterestCategoryTreeResult actual) {
        List<PointOfInterestCategory> pointOfInterestCategoryList = actual.getCategories();
        List<String> names = new ArrayList<>();

        for (int i = 0; i < pointOfInterestCategoryList.size(); i++) {
            names.add(pointOfInterestCategoryList.get(i).getName());
        }

        assertTrue(names.contains(expected.getCategories().get(0).getName()));
    }

    static void validateSearchPointOfInterestCategoryTreeWithResponse(PointOfInterestCategoryTreeResult expected,
                                                                      int expectedStatusCode,
                                                                      Response<PointOfInterestCategoryTreeResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchAddressWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                  Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchAddress(ReverseSearchAddressResult expected, ReverseSearchAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchAddressWithResponse(ReverseSearchAddressResult expected, int expectedStatusCode,
                                                         Response<ReverseSearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
        validateReverseSearchAddress(expected, response.getValue());
    }

    static void validateReverseSearchCrossStreetAddress(ReverseSearchCrossStreetAddressResult expected,
                                                        ReverseSearchCrossStreetAddressResult actual) {
        assertNotNull(actual.getSummary());
        assertNotNull(actual.getAddresses());
        assertEquals(expected.getAddresses().size(), actual.getAddresses().size());
    }

    static void validateReverseSearchCrossStreetAddressWithResponse(ReverseSearchCrossStreetAddressResult expected,
                                                                    int expectedStatusCode,
                                                                    Response<ReverseSearchCrossStreetAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchStructuredAddressWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                            Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchInsideGeometryWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                         Response<SearchAddressResult> response) {
        assertNotNull(response);
        assertEquals(expectedStatusCode, response.getStatusCode());
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

    static void validateSearchAlongRouteWithResponse(SearchAddressResult expected, int expectedStatusCode,
                                                     Response<SearchAddressResult> response) {
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

    static void validateBeginReverseSearchAddressBatch(BatchReverseSearchResult expected,
                                                       BatchReverseSearchResult actual) {
        assertEquals(expected.getBatchItems().size(), actual.getBatchItems().size());
    }
}
