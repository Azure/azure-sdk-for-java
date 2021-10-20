// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;


public class DeleteAsyncLiveTests extends CallingServerTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithConnectionStringAsyncClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "deleteRecordingWithConnectionStringAsyncClient");
        deleteRecording(callingServerAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithTokenCredentialAsyncClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "deleteRecordingWithTokenCredentialAsyncClient");
        deleteRecording(callingServerAsyncClient);
    }


    private void deleteRecording(CallingServerAsyncClient callingServerAsyncClient) {
        try {
            Response<HttpResponse> response = callingServerAsyncClient.deleteRecordingWithResponse(RECORDING_DELETE_URL, null).block();
            assertThat(response.getStatusCode(), is(equalTo(200)));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording401Async(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingInvalidTokenCredential(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "deleteRecording404Async");
        StepVerifier.create(callingServerAsyncClient.deleteRecordingWithResponse(RECORDING_DELETE_URL, null))
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode(), is(equalTo(401)));
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording404Async(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient callingServerAsyncClient = setupAsyncClient(builder, "deleteRecording404Async");
        StepVerifier.create(callingServerAsyncClient.deleteRecordingWithResponse(RECORDING_DELETE_URL_404, null))
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode(), is(equalTo(404)));
            })
            .verifyComplete();
    }

    private CallingServerAsyncClient setupAsyncClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
