// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.generated;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIServiceVersion;
import com.azure.ai.openai.models.ChatChoice;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.azure.ai.openai.generated.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIClientTest extends OpenAIClientTestBase {
    private OpenAIClient client;

    private OpenAIClient getOpenAIClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, serviceVersion)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Completions resultCompletions = client.getCompletions(deploymentId, new CompletionsOptions(prompt));
            assertNotNull(resultCompletions.getUsage());
            assertCompletions(new int[]{0}, null, null, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Response<BinaryData> response = client.getCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new CompletionsOptions(prompt)), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            Completions resultCompletions = response.getValue().toObject(Completions.class);
            assertNotNull(resultCompletions.getUsage());
            assertCompletions(new int[]{0}, null, null, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            ChatCompletions resultChatCompletions = client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages));
            assertNotNull(resultChatCompletions.getUsage());
            // TODO: assert chat completion and make it generic
            List<ChatChoice> choices = resultChatCompletions.getChoices();
            assertNotNull(choices);
            assertTrue(choices.size() > 0);
            ChatChoice chatChoice = resultChatCompletions.getChoices().get(0);
            assertEquals(0, chatChoice.getIndex());
            assertEquals(ChatRole.ASSISTANT, chatChoice.getMessage().getRole());
            assertNotNull(chatChoice.getMessage().getContent());        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            Response<BinaryData> response = client.getChatCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            ChatCompletions resultChatCompletions = response.getValue().toObject(ChatCompletions.class);
            // TODO: assert chat completion and make it generic
            List<ChatChoice> choices = resultChatCompletions.getChoices();
            assertNotNull(choices);
            assertTrue(choices.size() > 0);
            ChatChoice chatChoice = resultChatCompletions.getChoices().get(0);
            assertEquals(0, chatChoice.getIndex());
            assertEquals(ChatRole.ASSISTANT, chatChoice.getMessage().getRole());
            assertNotNull(chatChoice.getMessage().getContent());        });
    }

    @Disabled("Status code 401, Access denied due to invalid subscription key or wrong API endpoint")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Embeddings embeddings = client.getEmbeddings(deploymentId, embeddingsOptions);
            assertNotNull(embeddings.getUsage());
            // TODO: assert embedding
        });
    }

    @Disabled("Status code 401, Access denied due to invalid subscription key or wrong API endpoint")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.generated.TestUtils#getTestParameters")
    public void getEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Response<BinaryData> response = client.getEmbeddingsWithResponse(deploymentId,
                BinaryData.fromObject(embeddingsOptions), new RequestOptions());
            assertEquals(200, response.getStatusCode());
            Embeddings resultEmbeddings = response.getValue().toObject(Embeddings.class);
            // TODO: assert embeddings
        });
    }

}
