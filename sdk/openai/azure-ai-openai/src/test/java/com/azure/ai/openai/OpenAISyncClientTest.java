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
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OpenAISyncClientTest extends OpenAIClientTestBase {
    private OpenAIClient client;

    private OpenAIClient getOpenAIClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, serviceVersion)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Completions resultCompletions = client.getCompletions(deploymentId, new CompletionsOptions(prompt));
            assertCompletions(new int[]{0}, null, null, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            IterableStream<Completions> resultCompletions = client.getCompletionsStream(deploymentId, new CompletionsOptions(prompt));
            resultCompletions.forEach(completions -> {
                assertNotNull(completions.getId());
                assertNotNull(completions.getChoices());
                assertFalse(completions.getChoices().isEmpty());
                assertNotNull(completions.getChoices().get(0).getText());
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsFromPrompt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsFromSinglePromptRunner((deploymentId, prompts) -> {
            Completions completions = client.getCompletions(deploymentId, prompts);
            assertCompletions(new int[]{0}, null, null, completions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Response<BinaryData> response = client.getCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new CompletionsOptions(prompt)), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            Completions resultCompletions = response.getValue().toObject(Completions.class);
            assertCompletions(new int[]{0}, null, null, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            ChatCompletions resultChatCompletions = client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages));
            assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            IterableStream<ChatCompletions> resultChatCompletions = client.getChatCompletionsStream(deploymentId, new ChatCompletionsOptions(chatMessages));
            resultChatCompletions.forEach(chatCompletions -> {
                assertNotNull(chatCompletions.getId());
                assertNotNull(chatCompletions.getChoices());
                assertFalse(chatCompletions.getChoices().isEmpty());
                assertNotNull(chatCompletions.getChoices().get(0).getDelta());
            });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            Response<BinaryData> response = client.getChatCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            ChatCompletions resultChatCompletions = response.getValue().toObject(ChatCompletions.class);
            assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Embeddings resultEmbeddings = client.getEmbeddings(deploymentId, embeddingsOptions);
            assertEmbeddings(resultEmbeddings);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Response<BinaryData> response = client.getEmbeddingsWithResponse(deploymentId,
                BinaryData.fromObject(embeddingsOptions), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            Embeddings resultEmbeddings = response.getValue().toObject(Embeddings.class);
            assertEmbeddings(resultEmbeddings);
        });
    }
}
