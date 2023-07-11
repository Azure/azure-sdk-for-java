// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.Embeddings;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.IterableStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAISyncClientTest extends OpenAIClientTestBase {
    private OpenAIClient client;

    private OpenAIClient getOpenAIClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, serviceVersion)
            .buildClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Completions resultCompletions = client.getCompletions(deploymentId, new CompletionsOptions(prompt));
            assertCompletions(1, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            IterableStream<Completions> resultCompletions = client.getCompletionsStream(deploymentId, new CompletionsOptions(prompt));
            assertTrue(resultCompletions.stream().toArray().length > 1);
            resultCompletions.forEach(OpenAIClientTestBase::assertCompletionsStream);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsFromPrompt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsFromSinglePromptRunner((deploymentId, prompts) -> {
            Completions completions = client.getCompletions(deploymentId, prompts);
            assertCompletions(1, completions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            Response<BinaryData> response = client.getCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new CompletionsOptions(prompt)), new RequestOptions());
            Completions resultCompletions = assertAndGetValueFromResponse(response, Completions.class, 200);
            assertCompletions(1, resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponseBadDeployment(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((_deploymentId, prompt) -> {
            String deploymentId = "BAD_DEPLOYMENT_ID";
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> client.getCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new CompletionsOptions(prompt)), new RequestOptions()));
            assertEquals(404, exception.getResponse().getStatusCode());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsUsageField(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
            completionsOptions.setMaxTokens(1024);
            completionsOptions.setN(3);
            completionsOptions.setLogprobs(1);

            Completions resultCompletions = client.getCompletions(modelId, completionsOptions);

            CompletionsUsage usage = resultCompletions.getUsage();
            assertCompletions(completionsOptions.getN() * completionsOptions.getPrompt().size(), resultCompletions);
            assertNotNull(usage);
            assertTrue(usage.getTotalTokens() > 0);
            assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsTokenCutoff(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getCompletionsRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
            completionsOptions.setMaxTokens(3);
            Completions resultCompletions = client.getCompletions(modelId, completionsOptions);
            assertCompletions(1, "length", resultCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            ChatCompletions resultChatCompletions = client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages));
            assertChatCompletions(1, resultChatCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            IterableStream<ChatCompletions> resultChatCompletions = client.getChatCompletionsStream(deploymentId, new ChatCompletionsOptions(chatMessages));
            assertTrue(resultChatCompletions.stream().toArray().length > 1);
            resultChatCompletions.forEach(OpenAIClientTestBase::assertChatCompletionsStream);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            Response<BinaryData> response = client.getChatCompletionsWithResponse(deploymentId,
                BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)), new RequestOptions());
            ChatCompletions resultChatCompletions = assertAndGetValueFromResponse(response, ChatCompletions.class, 200);
            assertChatCompletions(1, resultChatCompletions);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Embeddings resultEmbeddings = client.getEmbeddings(deploymentId, embeddingsOptions);
            assertEmbeddings(resultEmbeddings);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            Response<BinaryData> response = client.getEmbeddingsWithResponse(deploymentId,
                BinaryData.fromObject(embeddingsOptions), new RequestOptions());
            Embeddings resultEmbeddings = assertAndGetValueFromResponse(response, Embeddings.class, 200);
            assertEmbeddings(resultEmbeddings);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGenerateImage(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIClient(httpClient, serviceVersion);
        getImageGenerationRunner(options -> assertImageResponse(client.generateImage(options)));
    }
}
