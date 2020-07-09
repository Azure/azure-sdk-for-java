// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.PlaybackClient;
import com.azure.core.test.models.NetworkCallRecord;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import reactor.core.publisher.Mono;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    /**
     * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
     * live. By default, tests are run in playback mode.
     */
    public class SessionTests extends TestBase {

        /**
         * Use JUnit or TestNG annotation here for your testcase
         */
        public void fooTest() {
            // Do some network calls.
        }
    }

    /**
     * Sample code for recording network calls.
     */
    public class Foo {
        public void recordNetworkCalls() {
            // All network calls are kept in the recordedData variable.
            RecordedData recordedData = new RecordedData();
            HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new RecordNetworkCallPolicy(recordedData))
                .build();

            // Send requests through the HttpPipeline.
            pipeline.send(new HttpRequest(HttpMethod.GET, "http://bing.com"));

            // Get a record that was sent through the pipeline.
            NetworkCallRecord networkCall = recordedData.findFirstAndRemoveNetworkCall(record -> {
                return record.getUri().equals("http://bing.com");
            });
        }
    }

    /**
     * Sample code for using playback to test.
     */
    public class FooBar {
        public void playbackNetworkCalls() {
            RecordedData recordedData = new RecordedData();

            // Add some network calls to be replayed by playbackClient

            // Creates a HTTP client that plays back responses in recordedData.
            HttpClient playbackClient = new PlaybackClient(recordedData, null);

            // Send an HTTP GET request to http://bing.com. If recordedData contains a NetworkCallRecord with a matching HTTP
            // method and matching URL, it is returned as a response.
            Mono<HttpResponse> response = playbackClient.send(new HttpRequest(HttpMethod.GET, "http://bing.com"));
        }
    }
}
