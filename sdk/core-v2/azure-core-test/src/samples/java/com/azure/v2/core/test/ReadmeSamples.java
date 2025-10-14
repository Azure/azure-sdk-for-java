// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.test;

import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    // BEGIN: readme-sample-createATestClass

    /**
     * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
     * record. By default, tests are run in playback mode.
     */
    public static class ClientTests extends TestBase {

        /**
         * Use JUnit annotation here for your testcase
         */
        public void testMethodName() {
            HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder();
            if (interceptorManager.isRecordMode()) {
                // Add a policy to record network calls.
                pipelineBuilder.addPolicy(interceptorManager.getRecordPolicy());
            }
            if (interceptorManager.isPlaybackMode()) {
                // Use a playback client when running in playback mode
                pipelineBuilder.httpClient(interceptorManager.getPlaybackClient());
            }

            try (Response<?> response = pipelineBuilder.build().send(new HttpRequest().setMethod(HttpMethod.GET)
                .setUri("http://bing.com"))) {
                // Validate test results.
                assertEquals(200, response.getStatusCode());
            }
        }
        // END: readme-sample-createATestClass


        /**
         * Sample code for adding sanitizer and matcher to the interceptor manager.
         */
        public void testAddSanitizersAndMatchers() throws IOException {
            HttpPipelineBuilder pipelineBuilder = new HttpPipelineBuilder();
            // BEGIN: readme-sample-add-sanitizer-matcher

            List<TestProxySanitizer> customSanitizer = new ArrayList<>();
            // sanitize value for key: "modelId" in response json body
            customSanitizer.add(
                new TestProxySanitizer("$..modelId", "REPLACEMENT_TEXT", TestProxySanitizerType.BODY_KEY));

            if (interceptorManager.isRecordMode()) {
                // Add a policy to record network calls.
                pipelineBuilder.addPolicy(interceptorManager.getRecordPolicy());
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

            try (Response<?> response = pipelineBuilder.build().send(new HttpRequest().setMethod(HttpMethod.GET)
                .setUri("http://bing.com"))) {
                // Validate test results.
                assertEquals(200, response.getStatusCode());
            }
        }
    }
}
