// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.Embeddings;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OpenAIAsyncClientTest extends OpenAIClientTestBase {
    private OpenAIAsyncClient client;

    private OpenAIAsyncClient getOpenAIAsyncClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
            serviceVersion)
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletions(deploymentId, new CompletionsOptions(prompt)))
                .assertNext(resultCompletions -> {
                    assertCompletions(new int[]{0}, null, null, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletionsStream(deploymentId, new CompletionsOptions(prompt)).last())
                .assertNext(completions -> {
                    assertNotNull(completions.getId());
                    assertNotNull(completions.getChoices());
                    assertFalse(completions.getChoices().isEmpty());
                    assertNotNull(completions.getChoices().get(0).getText());
                })
                .verifyComplete();
        });
    }

    public void getCompletionsFromPrompt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsFromSinglePromptRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletions(deploymentId, prompt))
                .assertNext(resultCompletions -> {
                    assertCompletions(new int[]{0}, null, null, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletionsWithResponse(deploymentId,
                    BinaryData.fromObject(new CompletionsOptions(prompt)),
                    new RequestOptions()))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode());
                    Completions resultCompletions = response.getValue().toObject(Completions.class);
                    assertCompletions(new int[]{0}, null, null, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages)))
                .assertNext(resultChatCompletions -> {
                    assertNotNull(resultChatCompletions.getUsage());
                    assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsStream(deploymentId, new ChatCompletionsOptions(chatMessages)).last())
                .assertNext(chatCompletions -> {
                    assertNotNull(chatCompletions.getId());
                    assertNotNull(chatCompletions.getChoices());
                    assertFalse(chatCompletions.getChoices().isEmpty());
                    assertNotNull(chatCompletions.getChoices().get(0).getDelta());
                })
                .verifyComplete();

        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsWithResponse(deploymentId,
                    BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)),
                    new RequestOptions()))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode());
                    ChatCompletions resultChatCompletions = response.getValue().toObject(ChatCompletions.class);
                    assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(deploymentId, embeddingsOptions))
                .assertNext(resultEmbeddings -> assertEmbeddings(resultEmbeddings))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddingsWithResponse(deploymentId,
                    BinaryData.fromObject(embeddingsOptions),
                    new RequestOptions()))
                .assertNext(response -> {
                    assertEquals(200, response.getStatusCode());
                    Embeddings resultEmbeddings = response.getValue().toObject(Embeddings.class);
                    assertEmbeddings(resultEmbeddings);
                })
                .verifyComplete();
        });
    }
}
