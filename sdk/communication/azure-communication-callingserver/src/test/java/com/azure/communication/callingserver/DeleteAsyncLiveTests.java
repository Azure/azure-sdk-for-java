// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DeleteAsyncLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithConnectionStringAsyncClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "deleteRecordingWithConnectionStringAsyncClient");
        deleteRecording(conversationAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithTokenCredentialAsyncClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "deleteRecordingWithTokenCredentialAsyncClient");
        deleteRecording(conversationAsyncClient);
    }

    private void deleteRecording(CallingServerAsyncClient callingServerAsyncClient) {
        try {
            callingServerAsyncClient.deleteRecording(RECORDING_DELETE_URL).block();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    /*
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording404Async(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "deleteRecording404Async");
        StepVerifier.create(conversationAsyncClient.deleteRecordingWithResponse(RECORDING_DELETE_URL_404, null))
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode(), is(equalTo(404)));
                StepVerifier.create(response.getValue()).verifyError(CallingServerErrorException.class);
            })
            .verifyComplete();
    }*/

    private CallingServerAsyncClient setupAsyncClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
