// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.inference;

import com.azure.ai.inference.models.*;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.azure.ai.inference.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChatCompletionsAsyncClientTest extends ChatCompletionsClientTestBase {
    private ChatCompletionsAsyncClient client;

    private ChatCompletionsAsyncClient getChatCompletionsAsyncClient (HttpClient httpClient) {
        return getChatCompletionsClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient) {
        client = getChatCompletionsAsyncClient(httpClient);
        getChatCompletionsRunner((prompt) -> {
            StepVerifier.create(client.complete(prompt))
                .assertNext(resultCompletions -> {
                    assertNotNull(resultCompletions.getUsage());
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.inference.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient) {
        client = getChatCompletionsAsyncClient(httpClient);
        getStreamingChatCompletionsRunner((chatMessages) -> {
            StepVerifier.create(client.completeStreaming(new ChatCompletionsOptions(chatMessages)))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> {
                    assertCompletionsStream(chatCompletions);
                    return true;
                })
                .consumeRecordedWith(messageList -> assertTrue(messageList.size() > 1))
                .verifyComplete();
        });
    }
}
