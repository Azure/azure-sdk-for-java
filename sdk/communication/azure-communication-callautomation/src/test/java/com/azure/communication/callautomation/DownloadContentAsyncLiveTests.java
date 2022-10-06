// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.DownloadToFileOptions;
import com.azure.communication.callautomation.models.ParallelDownloadOptions;
import com.azure.core.http.HttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

public class DownloadContentAsyncLiveTests extends CallAutomationLiveTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadataWithConnectionStringAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataWithConnectionStringAsyncClient");
        downloadMetadata(conversationAsyncClient);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)",
        disabledReason = "Requires human intervention")
    public void downloadMetadataWithTokenCredentialAsyncClient(HttpClient httpClient) {
        CallAutomationClientBuilder builder = getCallAutomationClientUsingTokenCredential(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataWithTokenCredentialAsyncClient");
        downloadMetadata(conversationAsyncClient);
    }

    private void downloadMetadata(CallAutomationAsyncClient conversationAsyncClient) {

        try {
            validateMetadata(conversationAsyncClient.getCallRecordingAsync().downloadStream(METADATA_URL));
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
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadMetadataRetryingAsync");

        try {
            validateMetadata(conversationAsyncClient.getCallRecordingAsync().downloadStream(METADATA_URL));
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
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadVideoAsync");

        try {
            StepVerifier.create(conversationAsyncClient.getCallRecordingAsync()
                    .downloadStreamWithResponse(VIDEO_URL, null))
                    .consumeNextWith(response -> StepVerifier.create(response.getValue())
                        .consumeNextWith(byteBuffer ->
                            assertThat(Integer.parseInt(response.getHeaders().getValue("Content-Length")),
                            is(equalTo(byteBuffer.array().length))))
                        .verifyComplete())
                    .verifyComplete();
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
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadToFileAsync");
        AsynchronousFileChannel channel = Mockito.mock(AsynchronousFileChannel.class);

        doAnswer(invocation -> {
            ByteBuffer stream = invocation.getArgument(0);
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(439, stream.position(stream.limit()));
            return null;
        }).doAnswer(invocation -> {
            ByteBuffer stream = invocation.getArgument(0);
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(438, stream.position(stream.limit()));
            return null;
        }).when(channel).write(any(ByteBuffer.class),
            anyLong(),
            any(),
            any());

        ParallelDownloadOptions parallelOptions = new ParallelDownloadOptions().setBlockSize(479L);
        DownloadToFileOptions options = new DownloadToFileOptions().setParallelDownloadOptions(parallelOptions);
        conversationAsyncClient
            .getCallRecordingAsync()
            .downloadToWithResponse(METADATA_URL,
                Paths.get("dummyPath"),
                channel,
                options,
                Context.NONE).block();

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
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadToFileRetryingAsync");
        AsynchronousFileChannel channel = Mockito.mock(AsynchronousFileChannel.class);

        doAnswer(invocation -> {
            ByteBuffer stream = invocation.getArgument(0);
            String metadata = new String(stream.array(), StandardCharsets.UTF_8);
            assertTrue(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"));
            CompletionHandler<Integer, Object> completionHandler = invocation.getArgument(3);
            completionHandler.completed(957, stream.position(stream.limit()));
            return null;
        }).when(channel).write(any(ByteBuffer.class),
            anyLong(),
            any(),
            any());


        conversationAsyncClient
            .getCallRecordingAsync()
            .downloadToWithResponse(METADATA_URL,
                Paths.get("dummyPath"),
                channel,
                new DownloadToFileOptions(),
                Context.NONE).block();

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
        CallAutomationClientBuilder builder = getCallAutomationClientUsingConnectionString(httpClient);
        CallAutomationAsyncClient conversationAsyncClient = setupAsyncClient(builder, "downloadContent404Async");
        StepVerifier.create(conversationAsyncClient
                .getCallRecordingAsync()
                .downloadStreamWithResponse(CONTENT_URL_404, null))
            .consumeNextWith(response -> {
                assertThat(response.getStatusCode(), is(equalTo(404)));
                StepVerifier.create(response.getValue()).verifyError(CallingServerErrorException.class);
            })
            .verifyComplete();
    }

    private CallAutomationAsyncClient setupAsyncClient(CallAutomationClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }

    protected CallAutomationClientBuilder addLoggingPolicy(CallAutomationClientBuilder builder, String testName) {
        return builder.addPolicy((context, next) -> logHeaders(testName, next));
    }

    private void validateMetadata(Flux<ByteBuffer> metadataByteBuffer) {
        StepVerifier.create(metadataByteBuffer)
            .consumeNextWith(byteBuffer -> {
                String metadata = new String(byteBuffer.array(), StandardCharsets.UTF_8);
                assertThat(metadata.contains("0-eus-d2-3cca2175891f21c6c9a5975a12c0141c"), is(true));
            })
            .verifyComplete();
    }
}
