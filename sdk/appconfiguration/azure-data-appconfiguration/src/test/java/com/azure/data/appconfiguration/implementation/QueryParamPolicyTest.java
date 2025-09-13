// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;

import reactor.core.publisher.Mono;

public class QueryParamPolicyTest {
    private static final String BASE_URL = "http://localhost:8080";
    private static final String ENDPOINT_PATH = "/kv/test";

    /**
     * Test that query parameters are sorted alphabetically
     */
    @SyncAsyncTest
    public void queryParametersAreSortedAlphabetically() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?zebra=value1&alpha=value2&beta=value3";
        final String expectedUrl = BASE_URL + ENDPOINT_PATH + "?alpha=value2&beta=value3&zebra=value1";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl, "Query parameters should be sorted alphabetically");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that query parameter keys are converted to lowercase
     */
    @SyncAsyncTest
    public void queryParameterKeysAreConvertedToLowercase() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?SELECT=field1&FILTER=condition&orderBy=field2";
        final String expectedUrl = BASE_URL + ENDPOINT_PATH + "?filter=condition&orderby=field2&select=field1";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl, "Query parameter keys should be lowercase and sorted");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that OData-style parameters like $select are handled correctly
     */
    @SyncAsyncTest
    public void oDataParametersAreHandledCorrectly() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?$Select=name,value&$Filter=startsWith(key,'test')&api-version=1.0";
        final String expectedUrl
            = BASE_URL + ENDPOINT_PATH + "?$filter=startsWith(key,'test')&$select=name,value&api-version=1.0";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl, "OData parameters should be lowercase and sorted");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that URLs without query parameters are not modified
     */
    @SyncAsyncTest
    public void urlsWithoutQueryParametersAreNotModified() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH;

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(originalUrl, actualUrl, "URLs without query parameters should not be modified");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that empty query parameters are handled correctly
     */
    @SyncAsyncTest
    public void emptyQueryParametersAreHandledCorrectly() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            // The URL should either be cleaned up or preserved as is
            assertTrue((BASE_URL + ENDPOINT_PATH).equals(actualUrl) || actualUrl.equals(originalUrl),
                "Empty query parameters should be handled gracefully");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that query parameter values are preserved exactly
     */
    @SyncAsyncTest
    public void queryParameterValuesArePreserved() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?key1=Value%20With%20Spaces&key2=SimpleValue&key3=";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            // Check that all values are preserved
            assertTrue(actualUrl.contains("Value%20With%20Spaces"), "Values with spaces should be preserved");
            assertTrue(actualUrl.contains("SimpleValue"), "Simple values should be preserved");
            assertTrue(actualUrl.contains("key3="), "Empty values should be preserved");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that multiple query parameters with the same key are preserved as separate parameters
     */
    @SyncAsyncTest
    public void multipleParametersWithSameKeyArePreserved() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?filter=condition1&select=field1&filter=condition2";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Count how many filter parameters exist
            int filterCount = (actualUrl.length() - actualUrl.replace("filter=", "").length()) / "filter=".length();

            // The policy should preserve both filter parameters as separate entries
            assertEquals(2, filterCount, "Both filter parameters should be preserved separately");
            assertTrue(actualUrl.contains("filter=condition1"), "First filter parameter should be preserved");
            assertTrue(actualUrl.contains("filter=condition2"), "Second filter parameter should be preserved");
            assertTrue(actualUrl.contains("select=field1"), "Select parameter should be preserved");

            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that malformed URLs are handled gracefully
     */
    @Test
    public void malformedUrlsAreHandledGracefully() {
        // This test uses a synchronous approach since we're testing error handling
        final String malformedUrl = "not-a-valid-url://[invalid";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            // The URL should remain unchanged when malformed
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(malformedUrl, actualUrl, "Malformed URLs should remain unchanged");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        // Test should not throw an exception
        try {
            HttpRequest request = new HttpRequest(HttpMethod.GET, malformedUrl);
            pipeline.send(request, Context.NONE).block();
        } catch (Exception e) {
            // If an exception occurs, it should not be from the QueryParamPolicy
            assertTrue(e.getCause() instanceof MalformedURLException || e.getMessage().contains("not-a-valid-url"),
                "Exception should be related to the malformed URL, not the policy");
        }
    }

    /**
     * Test comprehensive scenario with mixed case, special characters, and sorting
     */
    @SyncAsyncTest
    public void comprehensiveQueryParameterNormalization() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH
            + "?$TOP=10&API-Version=2023-10-01&$select=key,value&label=prod&$filter=startsWith(key,'app')&maxItems=100";
        final String expectedUrl = BASE_URL + ENDPOINT_PATH
            + "?$filter=startsWith(key,'app')&$select=key,value&$top=10&api-version=2023-10-01&label=prod&maxitems=100";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl,
                "Complex query parameters should be normalized, sorted, and lowercased");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that validates the correct behavior for multiple tags parameters
     * 
     * This test verifies that the policy correctly preserves multiple tags parameters
     * as separate parameters instead of merging them into comma-separated values.
     * 
     * This test validates that the QueryParamPolicy implementation properly handles
     * the Azure App Configuration API requirement for separate tags parameters.
     */
    @SyncAsyncTest
    public void multipleTagsParameters() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3D&tags=tag2%3D";

        // The URL should preserve multiple tags parameters after processing (with alphabetical sorting)
        final String expectedUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3D&tags=tag2%3D";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify that multiple tags parameters are preserved as separate parameters
            int tagsCount = (actualUrl.length() - actualUrl.replace("tags=", "").length()) / "tags=".length();
            assertEquals(2, tagsCount, "Multiple tags parameters should be preserved separately. "
                + "Expected 2 separate tags parameters, but got " + tagsCount);

            // The URL should preserve multiple tags parameters in their original form
            assertEquals(expectedUrl, actualUrl, "Multiple tags parameters should be preserved with proper ordering");

            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test single tags parameter is handled correctly
     */
    @SyncAsyncTest
    public void singleTagsParameterPreserved() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&tags=environment%3Dprod";
        final String expectedUrl = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&tags=environment%3Dprod";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl, "Single tags parameter should be preserved with proper ordering");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that multiple tags parameters are preserved correctly with proper alphabetical ordering
     * 
     * Verifies that the policy preserves multiple tags parameters as separate parameters
     * while maintaining alphabetical ordering of all query parameters.
     */
    @SyncAsyncTest
    public void multipleTagsParametersWithOrderingVerification() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3D&tags=tag2%3D";
        final String expectedUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3D&tags=tag2%3D";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl,
                "Multiple tags parameters should be preserved with proper alphabetical ordering");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test tags parameters with complex values and URL encoding
     */
    @SyncAsyncTest
    public void tagsParametersWithComplexValues() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?tags=environment%3Dproduction&tags=team%3Dbackend&api-version=2023-11-01";
        final String expectedUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&tags=environment%3Dproduction&tags=team%3Dbackend";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl, "Tags parameters with complex values should be sorted and preserved");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test tags parameters mixed with other App Configuration parameters
     */
    @SyncAsyncTest
    public void tagsParametersMixedWithOtherParameters() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH
            + "?$select=key,value&tags=feature%3Dauth&label=*&api-version=2023-11-01&$filter=startsWith(key,'app')&tags=env%3Dtest";
        final String expectedUrl = BASE_URL + ENDPOINT_PATH
            + "?$filter=startsWith(key,'app')&$select=key,value&api-version=2023-11-01&label=*&tags=env%3Dtest&tags=feature%3Dauth";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl,
                "Tags parameters mixed with other parameters should be sorted correctly");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test tags parameters with special characters and case sensitivity
     */
    @SyncAsyncTest
    public void tagsParametersWithSpecialCharacters() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?TAGS=Priority%3DHigh&api-version=2023-11-01&Tags=Status%3DActive";
        final String expectedUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&tags=Priority%3DHigh&tags=Status%3DActive";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            assertEquals(expectedUrl, actualUrl,
                "Tags parameters with special characters should be normalized and sorted");
            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    private Mono<HttpResponse> sendRequest(HttpPipeline pipeline, String url) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.NONE);
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, String url) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.NONE).block();
    }

}
