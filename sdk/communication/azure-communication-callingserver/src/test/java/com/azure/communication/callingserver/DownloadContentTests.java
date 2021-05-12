// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 * These tests will not run in LIVE or RECORD as they cannot get the content url.
 */
public class DownloadContentTests extends CallingServerTestBase {
    private static final String METADATA_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/acsmetadata";
    private static final String VIDEO_URL = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141c/content/video";
    private static final String CONTENT_URL_404 = "https://storage.asm.skype.com/v1/objects/0-eus-d2-3cca2175891f21c6c9a5975a12c0141d/content/acsmetadata";

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadMetadata(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadMetadata");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            conversationClient.downloadTo(METADATA_URL, baos, null);
            String metadata = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadVideo(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadVideo");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Response<Void> response = conversationClient
                .downloadToWithResponse(VIDEO_URL, baos, null, null);
            assertThat(response, is(notNullValue()));
            assertThat(response.getHeaders().getValue("Content-Type"), is(equalTo("application/octet-stream")));
            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")), is(equalTo(baos.size())));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void downloadContent404(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerClient conversationClient = setupClient(builder, "downloadContent404");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CallingServerResponseException ex = assertThrows(CallingServerResponseException.class,
            () -> conversationClient
                .downloadTo(CONTENT_URL_404, baos, null));
        assertThat(ex.getResponse().getStatusCode(), is(equalTo(404)));
    }

    private CallingServerClient setupClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
