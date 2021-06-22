// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

public class DownloadContentAsyncLiveTests extends CallingServerTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadataAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataAsync");

        try {
            Flux<ByteBuffer> content = conversationAsyncClient.downloadStream(METADATA_URL);
            byte[] contentBytes = FluxUtil.collectBytesInByteBufferStream(content).block();
            assertThat(contentBytes, is(notNullValue()));
            String metadata = new String(contentBytes, StandardCharsets.UTF_8);
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
    public void downloadMetadataRetryingAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataRetryingAsync");

        try {
            Flux<ByteBuffer> content = conversationAsyncClient.downloadStream(METADATA_URL);
            byte[] contentBytes = FluxUtil.collectBytesInByteBufferStream(content).block();
            assertThat(contentBytes, is(notNullValue()));
            String metadata = new String(contentBytes, StandardCharsets.UTF_8);
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
    public void downloadVideoAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadVideoAsync");

        try {
            Response<Flux<ByteBuffer>> response = conversationAsyncClient.downloadStreamWithResponse(VIDEO_URL, null).block();
            assertThat(response, is(notNullValue()));
            byte[] contentBytes = FluxUtil.collectBytesInByteBufferStream(response.getValue()).block();
            assertThat(contentBytes, is(notNullValue()));
            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")), is(equalTo(contentBytes.length)));
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
    public void downloadToFileAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadToFileAsync");
        AsynchronousFileChannel channel = Mockito.mock(AsynchronousFileChannel.class);

        doAnswer(invocation -> {
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(439, null);
            return null;
        }).doAnswer(invocation -> {
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(438, null);
            return null;
        }).when(channel).write(any(ByteBuffer.class),
            anyLong(),
            any(),
            any());

        conversationAsyncClient
            .downloadToWithResponse(METADATA_URL,
                Paths.get("dummyPath"),
                channel,
                new ParallelDownloadOptions().setBlockSize(479L),
                null).block();

        Mockito.verify(channel, times(2)).write(any(ByteBuffer.class), anyLong(),
            any(), any());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadToFileRetryingAsync(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadToFileRetryingAsync");
        AsynchronousFileChannel channel = Mockito.mock(AsynchronousFileChannel.class);

        doAnswer(invocation -> {
            ByteBuffer stream = invocation.getArgument(0);
            String metadata = new String(stream.array(), StandardCharsets.UTF_8);
            assertTrue(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"));
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(957, null);
            return null;
        }).when(channel).write(any(ByteBuffer.class),
            anyLong(),
            any(),
            any());


        conversationAsyncClient
            .downloadToWithResponse(METADATA_URL,
                Paths.get("dummyPath"),
                channel,
                null,
                null).block();

        Mockito.verify(channel).write(any(ByteBuffer.class), anyLong(),
            any(), any());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadContent404Async(HttpClient httpClient) {
        CallingServerClientBuilder builder = getConversationClientUsingConnectionString(httpClient);
        CallingServerAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadContent404Async");
        Response<Flux<ByteBuffer>> response = conversationAsyncClient
            .downloadStreamWithResponse(CONTENT_URL_404, null).block();
        assertThat(response, is(notNullValue()));
        assertThat(response.getStatusCode(), is(equalTo(404)));
        assertThrows(CallingServerErrorException.class,
            () -> FluxUtil.collectBytesInByteBufferStream(response.getValue()).block());
    }


    private CallingServerAsyncClient setupAsyncClient(CallingServerClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallingServerClientBuilder addLoggingPolicy(CallingServerClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }
}
