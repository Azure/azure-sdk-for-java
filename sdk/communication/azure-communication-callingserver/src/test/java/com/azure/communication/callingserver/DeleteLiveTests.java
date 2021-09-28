// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class DeleteLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithConnectionStringClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "deleteRecordingWithConnectionStringClient");
        deleteRecording(callingServerClient);
    }
/*
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecordingWithTokenCredentialClient(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingTokenCredential(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "deleteRecordingWithTokenCredentialClient");
        deleteRecording(callingServerClient);
    }*/


    private void deleteRecording(CallingServerClient callingServerAsyncClient) {
        try {
            Response<HttpResponse> response = callingServerAsyncClient.deleteRecordingWithResponse(RECORDING_DELETE_URL, null);
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
        CallingServerClient callingServerClient = setupClient(builder, "deleteRecording404Async");
        Response<HttpResponse> response = callingServerClient.deleteRecordingWithResponse(RECORDING_DELETE_URL, null);
        assertThat(response.getStatusCode(), is(equalTo(401)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void deleteRecording404Async(HttpClient httpClient) {
        CallingServerClientBuilder builder = getCallingServerClientUsingConnectionString(httpClient);
        CallingServerClient callingServerClient = setupClient(builder, "deleteRecording404Async");
        Response<HttpResponse> response = callingServerClient.deleteRecordingWithResponse(RECORDING_DELETE_URL_404, null);
        assertThat(response.getStatusCode(), is(equalTo(404)));
    }

    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
