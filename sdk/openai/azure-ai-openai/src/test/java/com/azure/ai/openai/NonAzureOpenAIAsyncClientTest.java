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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NonAzureOpenAIAsyncClientTest extends OpenAIClientTestBase {
    private OpenAIAsyncClient client;

    private OpenAIAsyncClient getNonAzureOpenAIAsyncClient(HttpClient httpClient) {
        return getNonAzureOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletions(modelId, new CompletionsOptions(prompt)))
                .assertNext(resultCompletions -> {
                    assertCompletions(new int[]{0}, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @Disabled("onError(com.fasterxml.jackson.databind.exc.MismatchedInputException: Missing required creator property 'usage'")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletionsStream(modelId, new CompletionsOptions(prompt)).last())
                .assertNext(completions -> {
                    assertNotNull(completions.getId());
                    assertNotNull(completions.getChoices());
                    assertFalse(completions.getChoices().isEmpty());
                    assertNotNull(completions.getChoices().get(0).getText());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsFromPrompt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsFromSinglePromptRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletions(modelId, prompt))
                .assertNext(resultCompletions -> {
                    assertCompletions(new int[]{0}, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletionsWithResponse(modelId,
                    BinaryData.fromObject(new CompletionsOptions(prompt)),
                    new RequestOptions()))
                .assertNext(response -> {
                    Completions resultCompletions = assertResponseAndGetValue(response, Completions.class, 200);
                    assertCompletions(new int[]{0}, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(modelId, new ChatCompletionsOptions(chatMessages)))
                .assertNext(resultChatCompletions -> {
                    assertNotNull(resultChatCompletions.getUsage());
                    assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @Disabled("onError(com.fasterxml.jackson.databind.exc.MismatchedInputException: Missing required creator property 'usage'")
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getChatCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsStream(modelId, new ChatCompletionsOptions(chatMessages)).last())
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsWithResponse(modelId,
                    BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)),
                    new RequestOptions()))
                .assertNext(response -> {
                    ChatCompletions resultChatCompletions = assertResponseAndGetValue(response, ChatCompletions.class, 200);
                    assertChatCompletions(new int[]{0}, new ChatRole[]{ChatRole.ASSISTANT}, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingNonAzureRunner((modelId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(modelId, embeddingsOptions))
                .assertNext(resultEmbeddings -> assertEmbeddings(resultEmbeddings))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingNonAzureRunner((modelId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddingsWithResponse(modelId,
                    BinaryData.fromObject(embeddingsOptions),
                    new RequestOptions()))
                .assertNext(response -> {
                    Embeddings resultEmbeddings = assertResponseAndGetValue(response, Embeddings.class, 200);
                    assertEmbeddings(resultEmbeddings);
                })
                .verifyComplete();
        });
    }
}
