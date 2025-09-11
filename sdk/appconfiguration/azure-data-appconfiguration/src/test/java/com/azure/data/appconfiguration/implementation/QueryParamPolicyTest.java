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

/**
 * Unit tests for QueryParamPolicy
 */
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
            assertTrue(actualUrl.equals(BASE_URL + ENDPOINT_PATH) || actualUrl.equals(originalUrl),
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
     * Test that duplicate query parameter keys are handled correctly
     */
    @SyncAsyncTest
    public void duplicateQueryParameterKeysAreHandled() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?filter=condition1&select=field1&filter=condition2";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();
            // The policy should handle duplicates gracefully (TreeMap behavior)
            assertTrue(actualUrl.contains("filter="), "Filter parameter should be present");
            assertTrue(actualUrl.contains("select="), "Select parameter should be present");
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

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify alphabetical ordering and lowercase conversion
            String[] expectedOrder = { "$filter", "$select", "$top", "api-version", "label", "maxitems" };
            String queryString = actualUrl.substring(actualUrl.indexOf('?') + 1);
            String[] actualParams = queryString.split("&");

            for (int i = 0; i < expectedOrder.length && i < actualParams.length; i++) {
                String actualKey = actualParams[i].split("=")[0];
                assertEquals(expectedOrder[i], actualKey,
                    "Parameter at position " + i + " should be " + expectedOrder[i]);
            }

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
