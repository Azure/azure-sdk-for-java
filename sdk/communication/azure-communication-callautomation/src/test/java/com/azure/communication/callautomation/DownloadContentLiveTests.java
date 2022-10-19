// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;

public class DownloadContentLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadataWithConnectionStringClient(HttpClient httpClient) throws UnsupportedEncodingException {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadMetadataWithConnectionStringClient");
        downloadMetadata(conversationClient.getCallRecording());
    }
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadataWithTokenCredentialClient(HttpClient httpClient) throws UnsupportedEncodingException {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingTokenCredential(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadMetadataWithTokenCredentialClient");
        downloadMetadata(conversationClient.getCallRecording());
    }

    private void downloadMetadata(CallRecording callRecording) throws UnsupportedEncodingException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            callRecording.downloadTo(METADATA_URL, byteArrayOutputStream);
            String metadata = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
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
    public void downloadVideo(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadVideo");

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Response<Void> response = conversationClient
                .getCallRecording()
                .downloadToWithResponse(VIDEO_URL, byteArrayOutputStream, null, Context.NONE);
            assertThat(response, is(notNullValue()));
            assertThat(
                response.getHeaders().getValue("Content-Type"),
                is(equalTo("application/octet-stream")));
            assertThat(
                Integer.parseInt(response.getHeaders().getValue("Content-Length")),
                is(equalTo(byteArrayOutputStream.size())));
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
    public void downloadContent404(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadContent404");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CallingServerErrorException ex = assertThrows(CallingServerErrorException.class,
            () -> conversationClient
                .getCallRecording()
                .downloadTo(CONTENT_URL_404, byteArrayOutputStream));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContentWrongUrl(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadContentWrongUrl");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IllegalArgumentException ex =
            assertThrows(
                IllegalArgumentException.class,
                () -> conversationClient
                    .getCallRecording()
                    .downloadTo("wrongurl", byteArrayOutputStream));
        assertThat(ex, is(notNullValue()));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadContentStreamFailure(HttpClient httpClient) throws IOException {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationClient conversationClient = setupClient(builder, "downloadContent404");

        OutputStream outputStream = Mockito.mock(OutputStream.class);
        doThrow(IOException.class).when(outputStream).write(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
        assertThrows(
            UncheckedIOException.class,
            () -> conversationClient
                .getCallRecording()
                .downloadTo(METADATA_URL, outputStream));
    }

    private CallAutomationClient setupClient(CallAutomationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallAutomationClientBuilder addLoggingPolicy(CallAutomationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
