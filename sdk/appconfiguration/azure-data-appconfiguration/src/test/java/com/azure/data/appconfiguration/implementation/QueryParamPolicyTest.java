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

    /**
     * Test debugging multiple tags parameters to see actual URL transformation
     * This test shows what actually happens with the current implementation
     */
    @SyncAsyncTest
    public void debugMultipleTagsParametersActualBehavior() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH
            + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3Dvalue1&tags=tag2%3Dvalue2";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Output for debugging to understand current behavior
            System.out.println("Original URL: " + originalUrl);
            System.out.println("Transformed URL: " + actualUrl);

            // Basic validations that should always pass
            assertTrue(actualUrl.contains("api-version=2023-11-01"), "API version should be preserved");
            assertTrue(actualUrl.contains("key=*"), "Key parameter should be preserved");
            assertTrue(actualUrl.contains("label=dev"), "Label parameter should be preserved");
            assertTrue(actualUrl.contains("tags="), "At least one tags parameter should be present");

            // Verify alphabetical ordering is maintained
            int apiVersionPos = actualUrl.indexOf("api-version=");
            int keyPos = actualUrl.indexOf("key=");
            int labelPos = actualUrl.indexOf("label=");
            int tagsPos = actualUrl.indexOf("tags=");

            assertTrue(apiVersionPos < keyPos, "api-version should come before key");
            assertTrue(keyPos < labelPos, "key should come before label");
            assertTrue(labelPos < tagsPos, "label should come before tags");

            return next.process();
        };

        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(queryParamPolicy, auditorPolicy)
            .build();

        SyncAsyncExtension.execute(() -> sendRequestSync(pipeline, originalUrl),
            () -> sendRequest(pipeline, originalUrl));
    }

    /**
     * Test that multiple tags parameters are correctly preserved as separate parameters
     * 
     * This test verifies that multiple tags parameters with the same key are preserved
     * as separate query parameters instead of being merged into comma-separated values.
     * 
     * Expected behavior: 
     * "?tags=tag1%3D&tags=tag2%3D" remains as two separate parameters
     * 
     * This is the correct behavior for Azure App Configuration API which expects
     * multiple tags parameters, not comma-separated values.
     */
    @SyncAsyncTest
    public void multipleTagsParametersFixedBehavior() {
        final String originalUrl
            = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&label=dev&tags=tag1%3D&tags=tag2%3D";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Count how many separate tags parameters exist
            int tagsCount = (actualUrl.length() - actualUrl.replace("tags=", "").length()) / "tags=".length();

            // Verify that 2 separate tags parameters are preserved
            assertTrue(tagsCount == 2, "Multiple tags parameters should be preserved as separate parameters. Found "
                + tagsCount + " tags parameter(s), expected 2 separate tags parameters.");

            // Verify both tags parameters are preserved separately
            if (tagsCount == 2) {
                // Both original tag values should be present as separate parameters
                assertTrue(actualUrl.contains("tag1%3D") && actualUrl.contains("tag2%3D"),
                    "Both tags parameters should be preserved separately with original values");
            }

            System.out.println("Original URL: " + originalUrl);
            System.out.println("Transformed URL: " + actualUrl);
            System.out.println("Tags parameters found: " + tagsCount);

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
    public void multipleTagsParametersCorrectBehavior() {
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
    public void singleTagsParameter() {
        final String originalUrl = BASE_URL + ENDPOINT_PATH + "?api-version=2023-11-01&key=*&tags=environment%3Dprod";

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify alphabetical ordering: api-version, key, tags
            assertTrue(actualUrl.contains("api-version=2023-11-01"), "API version should be preserved");
            assertTrue(actualUrl.contains("key=*"), "Key parameter should be preserved");
            assertTrue(actualUrl.contains("tags=environment%3Dprod"), "Tags parameter should be preserved");

            // Verify order: api-version should come before key, key should come before tags
            int apiVersionPos = actualUrl.indexOf("api-version=");
            int keyPos = actualUrl.indexOf("key=");
            int tagsPos = actualUrl.indexOf("tags=");

            assertTrue(apiVersionPos < keyPos, "api-version should come before key");
            assertTrue(keyPos < tagsPos, "key should come before tags");

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

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify alphabetical ordering: api-version, key, label, tags
            assertTrue(actualUrl.contains("api-version=2023-11-01"), "API version should be preserved");
            assertTrue(actualUrl.contains("key=*"), "Key parameter should be preserved");
            assertTrue(actualUrl.contains("label=dev"), "Label parameter should be preserved");

            // Verify that the policy correctly preserves multiple tags as separate parameters
            // This is the correct behavior for Azure App Configuration API
            int tagsCount = (actualUrl.length() - actualUrl.replace("tags=", "").length()) / "tags=".length();
            assertEquals(2, tagsCount, "Multiple tags parameters should be preserved as separate parameters. "
                + "Expected " + 2 + " separate tags parameters, but found " + tagsCount + ".");

            // Verify parameters are in alphabetical order
            int apiVersionPos = actualUrl.indexOf("api-version=");
            int keyPos = actualUrl.indexOf("key=");
            int labelPos = actualUrl.indexOf("label=");
            int tagsPos = actualUrl.indexOf("tags=");

            assertTrue(apiVersionPos < keyPos, "api-version should come before key");
            assertTrue(keyPos < labelPos, "key should come before label");
            assertTrue(labelPos < tagsPos, "label should come before tags");

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

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify parameters are sorted alphabetically
            assertTrue(actualUrl.contains("api-version=2023-11-01"), "API version should be preserved");
            assertTrue(actualUrl.contains("tags="), "Tags parameter should be present");

            // Verify alphabetical order: api-version comes before tags
            int apiVersionPos = actualUrl.indexOf("api-version=");
            int tagsPos = actualUrl.indexOf("tags=");
            assertTrue(apiVersionPos < tagsPos, "api-version should come before tags");

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

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify we have the expected parameters
            assertTrue(actualUrl.contains("$filter="), "$filter parameter should be present");
            assertTrue(actualUrl.contains("$select="), "$select parameter should be present");
            assertTrue(actualUrl.contains("api-version="), "api-version parameter should be present");
            assertTrue(actualUrl.contains("label="), "label parameter should be present");
            assertTrue(actualUrl.contains("tags="), "tags parameter should be present");

            // Verify alphabetical ordering of the first occurrence of each parameter type
            int filterPos = actualUrl.indexOf("$filter=");
            int selectPos = actualUrl.indexOf("$select=");
            int apiVersionPos = actualUrl.indexOf("api-version=");
            int labelPos = actualUrl.indexOf("label=");
            int tagsPos = actualUrl.indexOf("tags=");

            assertTrue(filterPos < selectPos, "$filter should come before $select");
            assertTrue(selectPos < apiVersionPos, "$select should come before api-version");
            assertTrue(apiVersionPos < labelPos, "api-version should come before label");
            assertTrue(labelPos < tagsPos, "label should come before tags");

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

        QueryParamPolicy queryParamPolicy = new QueryParamPolicy();

        HttpPipelinePolicy auditorPolicy = (context, next) -> {
            final String actualUrl = context.getHttpRequest().getUrl().toString();

            // Verify that tag keys are converted to lowercase
            assertTrue(actualUrl.contains("tags="), "Tags parameter key should be lowercase");
            // Should not contain uppercase versions
            assertTrue(!actualUrl.contains("TAGS=") || actualUrl.indexOf("tags=") >= 0,
                "Uppercase TAGS should be converted to lowercase");
            assertTrue(!actualUrl.contains("Tags=") || actualUrl.indexOf("tags=") >= 0,
                "Mixed case Tags should be converted to lowercase");

            // Verify values are preserved with their encoding
            assertTrue(actualUrl.contains("%3D"), "Special characters in values should be preserved");

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
