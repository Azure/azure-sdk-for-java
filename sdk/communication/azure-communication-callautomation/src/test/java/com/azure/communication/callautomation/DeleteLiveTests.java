// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


public class DeleteLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithConnectionStringClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecordingWithConnectionStringClient");
        deleteRecording(callAutomationClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithTokenCredentialClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingTokenCredential(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecordingWithTokenCredentialClient");
        deleteRecording(callAutomationClient);
    }

    private void deleteRecording(CallAutomationClient callingServerAsyncClient) {
        try {
            Response<Void> response = callingServerAsyncClient
                .getCallRecording()
                .deleteRecordingWithResponse(RECORDING_DELETE_URL, Context.NONE);
            assertThat(response.getStatusCode(), is(equalTo(200)));
        } catch (Exception e) {
            fail("Unexpected exception received", e);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording401(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingInvalidTokenCredential(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecording404Async");
        Response<Void> response = callAutomationClient
            .getCallRecording()
            .deleteRecordingWithResponse(RECORDING_DELETE_URL, Context.NONE);
        assertThat(response.getStatusCode(), is(equalTo(401)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording404(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecording404Async");
        Response<Void> response = callAutomationClient
            .getCallRecording()
            .deleteRecordingWithResponse(RECORDING_DELETE_URL_404, Context.NONE);
        assertThat(response.getStatusCode(), is(equalTo(404)));
    }

    private CallAutomationClient setupClient(CallAutomationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallAutomationClientBuilder addLoggingPolicy(CallAutomationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
