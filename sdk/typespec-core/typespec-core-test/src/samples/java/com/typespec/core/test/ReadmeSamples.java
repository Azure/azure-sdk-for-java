// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.test.models.CustomMatcher;
import com.typespec.core.test.models.TestProxySanitizer;
import com.typespec.core.test.models.TestProxySanitizerType;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    // BEGIN: readme-sample-createATestClass

    /**
     * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
     * record. By default, tests are run in playback mode.
     */
    public static class ClientTests extends TestProxyTestBase {

        /**
         * Use JUnit annotation here for your testcase
         */
        public void testMethodName() {
            HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder();
            if (interceptorManager.isRecordMode()) {
                // Add a policy to record network calls.
                pipelineBuilder.policies(interceptorManager.getRecordPolicy());
            }
            if (interceptorManager.isPlaybackMode()) {
                // Use a playback client when running in playback mode
                pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
            }

            Mono<HttpResponse> response =
                pipelineBuilder.build().send(new HttpRequest(HttpMethod.GET, "http://bing.com"));

            // Validate test results.
            assertEquals(200, response.block().getStatusCode());
        }
        // END: readme-sample-createATestClass


        /**
         * Sample code for adding sanitizer and matcher to the interceptor manager.
         */
        public void testAddSanitizersAndMatchers() {
            HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder();
            // BEGIN: readme-sample-add-sanitizer-matcher

            List<TestProxySanitizer> customSanitizer = new ArrayList<>();
            // sanitize value for key: "modelId" in response json body
            customSanitizer.add(
                new TestProxySanitizer("$..modelId", "REPLACEMENT_TEXT", TestProxySanitizerType.BODY_KEY));

            if (interceptorManager.isRecordMode()) {
                // Add a policy to record network calls.
                pipelineBuilder.policies(interceptorManager.getRecordPolicy());
            }
            if (interceptorManager.isPlaybackMode()) {
                // Use a playback client when running in playback mode
                pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
                // Add matchers only in playback mode
                interceptorManager.addMatchers(Arrays.asList(new CustomMatcher()
                    .setHeadersKeyOnlyMatch(Arrays.asList("x-ms-client-request-id"))));
            }
            if (!interceptorManager.isLiveMode()) {
                // Add sanitizers when running in playback or record mode
                interceptorManager.addSanitizers(customSanitizer);
            }
            // END: readme-sample-add-sanitizer-matcher

            Mono<HttpResponse> response =
                pipelineBuilder.build().send(new HttpRequest(HttpMethod.GET, "http://bing.com"));

            // Validate test results.
            assertEquals(200, response.block().getStatusCode());
        }
    }
}
