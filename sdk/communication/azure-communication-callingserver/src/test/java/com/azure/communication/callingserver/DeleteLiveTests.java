// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Context;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

// Package marked to be deprecated
@LiveOnly()
public class DeleteLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecordingWithConnectionStringClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecordingWithConnectionStringClient");
        deleteRecording(callAutomationClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecordingWithTokenCredentialClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecordingWithTokenCredentialClient");
        deleteRecording(callAutomationClient);
    }

    private void deleteRecording(CallAutomationClient callingServerAsyncClient) {
        try {
            Response<Void> response = callingServerAsyncClient.getCallRecording()
                .deleteRecordingWithResponse(RECORDING_DELETE_URL, Context.NONE);
            assertEquals(200, response.getStatusCode());
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
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecording401(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingInvalidTokenCredential(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecording404Async");
        Response<Void> response
            = callAutomationClient.getCallRecording().deleteRecordingWithResponse(RECORDING_DELETE_URL, Context.NONE);
        assertEquals(401, response.getStatusCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void deleteRecording404(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallAutomationClient callAutomationClient = setupClient(builder, "deleteRecording404Async");
        Response<Void> response = callAutomationClient.getCallRecording()
            .deleteRecordingWithResponse(RECORDING_DELETE_URL_404, Context.NONE);
        assertEquals(404, response.getStatusCode());
    }

    private CallAutomationClient setupClient(CallAutomationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallAutomationClientBuilder addLoggingPolicy(CallAutomationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
