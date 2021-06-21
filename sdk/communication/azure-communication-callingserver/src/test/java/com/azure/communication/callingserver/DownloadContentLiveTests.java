// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

public class DownloadContentLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadata(HttpClient httpClient) throws UnsupportedEncodingException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadMetadata");

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            conversationClient.downloadTo(METADATA_URL, byteArrayOutputStream, null);
            String metadata = byteArrayOutputStream.toString(StandardCharsets.UTF_8.name());
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
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
    public void downloadVideo(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadVideo");

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Response<Void> response = conversationClient
                .downloadToWithResponse(VIDEO_URL, byteArrayOutputStream, null, null);
            assertThat(response, is(notNullValue()));
            assertThat(
                response.getHeaders().getValue("Content-Type"),
                is(equalTo("application/octet-stream")));
            assertThat(
                Integer.parseInt(response.getHeaders().getValue("Content-Length")),
                is(equalTo(byteArrayOutputStream.size())));
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
    public void downloadContent404(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadContent404");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        CallingServerErrorException ex = assertThrows(CallingServerErrorException.class,
            () -> conversationClient
                .downloadTo(CONTENT_URL_404, byteArrayOutputStream, null));
        assertThat(ex.getResponse().getStatusCode(), is(equalTo(404)));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContentWrongUrl(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadContentWrongUrl");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IllegalArgumentException ex =
            assertThrows(
                IllegalArgumentException.class,
                () -> conversationClient
                    .downloadTo("wrongurl", byteArrayOutputStream, null));
        assertThat(ex, is(notNullValue()));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadContentStreamFailure(HttpClient httpClient) throws IOException {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadContent404");

        ByteArrayOutputStream byteArrayOutputStream = Mockito.mock(ByteArrayOutputStream.class);
        doThrow(IOException.class).when(byteArrayOutputStream).write(Mockito.any());
        assertThrows(
            UncheckedIOException.class,
            () -> conversationClient
                .downloadTo(METADATA_URL, byteArrayOutputStream, null));
    }

    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
