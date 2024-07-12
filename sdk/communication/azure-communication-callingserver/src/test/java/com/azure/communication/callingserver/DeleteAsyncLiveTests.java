// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class DeleteAsyncLiveTests extends CallAutomationLiveTestBase {
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecordingWithConnectionStringAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient callAutomationAsyncClient = setupAsyncClient(builder, "deleteRecordingWithConnectionStringAsyncClient");
        deleteRecording(callAutomationAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecordingWithTokenCredentialAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
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
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecording401Async(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingInvalidTokenCredential(httpClient);
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
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecording404Async(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
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
