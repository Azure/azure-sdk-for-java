// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.CompletionsUsage;
import com.azure.ai.inference.models.StreamingChatCompletionsUpdate;
import com.azure.core.http.HttpClient;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class ChatCompletionsSyncClientTest extends ChatCompletionsClientTestBase {
    private ChatCompletionsClient client;

    private ChatCompletionsClient getChatCompletionsClient(HttpClient httpClient) {
        return getChatCompletionsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsRunner((prompt) -> {
            ChatCompletions resultCompletions = client.complete(prompt);
            assertCompletions(1, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsRunner((prompt) -> {
            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestUserMessage(prompt));
            IterableStream<StreamingChatCompletionsUpdate> resultCompletions = client.completeStream(new ChatCompletionsOptions(chatMessages));
            assertTrue(resultCompletions.stream().toArray().length > 1);
            resultCompletions.forEach(ChatCompletionsClientTestBase::assertCompletionsStream);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsFromOptions(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            ChatCompletions completions = client.complete(options);
            assertCompletions(1, completions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponse(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            Response<BinaryData> binaryDataResponse = client.completeWithResponse(
                BinaryData.fromObject(options), new RequestOptions());
            ChatCompletions response = binaryDataResponse.getValue().toObject(ChatCompletions.class);
            assertCompletions(1, response);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsUsageField(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            options.setMaxTokens(1024);

            ChatCompletions resultCompletions = client.complete(options);

            CompletionsUsage usage = resultCompletions.getUsage();
            assertCompletions(1, resultCompletions);
            assertNotNull(usage);
            assertTrue(usage.getTotalTokens() > 0);
            assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsTokenCutoff(HttpClient httpClient) {
        client = getChatCompletionsClient(httpClient);
        getChatCompletionsFromOptionsRunner((options) -> {
            options.setMaxTokens(3);
            ChatCompletions resultCompletions = client.complete(options);
            assertCompletions(1, resultCompletions);
        });
    }
}

