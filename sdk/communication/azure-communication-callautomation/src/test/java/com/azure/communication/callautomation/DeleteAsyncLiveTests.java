// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;


public class DeleteAsyncLiveTests extends CallAutomationLiveTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithConnectionStringAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient callAutomationAsyncClient = setupAsyncClient(builder, "deleteRecordingWithConnectionStringAsyncClient");
        deleteRecording(callAutomationAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithTokenCredentialAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingTokenCredential(httpClient);
        CallAutomationAsyncClient callAutomationAsyncClient = setupAsyncClient(builder, "deleteRecordingWithTokenCredentialAsyncClient");
        deleteRecording(callAutomationAsyncClient);
    }

    private void deleteRecording(CallAutomationAsyncClient callAutomationAsyncClient) {
        StepVerifier.create(callAutomationAsyncClient
            .getCallRecordingAsync()
            .deleteRecordingWithResponse(RECORDING_DELETE_URL))
            .consumeNextWith(response -> assertThat(response.getStatusCode(), is(equalTo(200))))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording401Async(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingInvalidTokenCredential(httpClient);
        CallAutomationAsyncClient callAutomationAsyncClient = setupAsyncClient(builder, "deleteRecording404Async");
        StepVerifier.create(callAutomationAsyncClient
                .getCallRecordingAsync()
                .deleteRecordingWithResponse(RECORDING_DELETE_URL))
            .consumeNextWith(response -> assertThat(response.getStatusCode(), is(equalTo(401))))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording404Async(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient callAutomationAsyncClient = setupAsyncClient(builder, "deleteRecording404Async");
        StepVerifier.create(callAutomationAsyncClient
                .getCallRecordingAsync()
                .deleteRecordingWithResponse(RECORDING_DELETE_URL_404))
            .consumeNextWith(response -> assertThat(response.getStatusCode(), is(equalTo(404))))
            .verifyComplete();
    }

    private CallAutomationAsyncClient setupAsyncClient(CallAutomationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallAutomationClientBuilder addLoggingPolicy(CallAutomationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
