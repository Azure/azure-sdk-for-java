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
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void testGetCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletions(deploymentId, new CompletionsOptions(prompt)))
                .assertNext(resultCompletions -> {
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        // TODO: use the parameterized serviceVersion once we have a service release that responds with valid data
        client = getOpenAIAsyncClient(httpClient, OpenAIServiceVersion.V2023_05_15);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletionsStream(deploymentId, new CompletionsOptions(prompt)))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> {
                    assertCompletionsStream(chatCompletions);
                    return true;
                })
                .consumeRecordedWith(messageList -> assertTrue(messageList.size() > 1))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsFromPrompt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsFromSinglePromptRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletions(deploymentId, prompt))
                .assertNext(resultCompletions -> {
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((deploymentId, prompt) -> {
            StepVerifier.create(client.getCompletionsWithResponse(deploymentId,
                    BinaryData.fromObject(new CompletionsOptions(prompt)),
                    new RequestOptions()))
                .assertNext(response -> {
                    Completions resultCompletions = assertAndGetValueFromResponse(response, Completions.class, 200);
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponseBadDeployment(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((_deploymentId, prompt) -> {
            String deploymentId = "BAD_DEPLOYMENT_ID";
            StepVerifier.create(client.getCompletionsWithResponse(deploymentId,
                    BinaryData.fromObject(new CompletionsOptions(prompt)), new RequestOptions()))
                .verifyErrorSatisfies(throwable -> {
                    assertInstanceOf(ResourceNotFoundException.class, throwable);
                    assertEquals(404, ((ResourceNotFoundException) throwable).getResponse().getStatusCode());
                });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsUsageField(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
            completionsOptions.setMaxTokens(1024);
            completionsOptions.setN(3);
            completionsOptions.setLogprobs(1);
            StepVerifier.create(client.getCompletions(modelId, completionsOptions))
                .assertNext(resultCompletions -> {
                    CompletionsUsage usage = resultCompletions.getUsage();
                    assertCompletions(completionsOptions.getN() * completionsOptions.getPrompt().size(), resultCompletions);
                    assertNotNull(usage);
                    assertTrue(usage.getTotalTokens() > 0);
                    assertEquals(usage.getCompletionTokens() + usage.getPromptTokens(), usage.getTotalTokens());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsTokenCutoff(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
            completionsOptions.setMaxTokens(3);
            StepVerifier.create(client.getCompletions(modelId, completionsOptions))
                .assertNext(resultCompletions ->
                    assertCompletions(1, "length", resultCompletions))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(deploymentId, new ChatCompletionsOptions(chatMessages)))
                .assertNext(resultChatCompletions -> {
                    assertNotNull(resultChatCompletions.getUsage());
                    assertChatCompletions(1, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        // TODO: use the parameterized serviceVersion once we have a service release that responds with valid data
        client = getOpenAIAsyncClient(httpClient, OpenAIServiceVersion.V2023_05_15);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsStream(deploymentId, new ChatCompletionsOptions(chatMessages)))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> {
                    assertChatCompletionsStream(chatCompletions);
                    return true;
                })
                .consumeRecordedWith(messageList -> assertTrue(messageList.size() > 1))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsWithResponse(deploymentId,
                    BinaryData.fromObject(new ChatCompletionsOptions(chatMessages)),
                    new RequestOptions()))
                .assertNext(response -> {
                    ChatCompletions resultChatCompletions = assertAndGetValueFromResponse(response, ChatCompletions.class, 200);
                    assertChatCompletions(1, resultChatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddings(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(deploymentId, embeddingsOptions))
                .assertNext(resultEmbeddings -> assertEmbeddings(resultEmbeddings))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getEmbeddingRunner((deploymentId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddingsWithResponse(deploymentId,
                    BinaryData.fromObject(embeddingsOptions),
                    new RequestOptions()))
                .assertNext(response -> {
                    Embeddings resultEmbeddings = assertAndGetValueFromResponse(response, Embeddings.class, 200);
                    assertEmbeddings(resultEmbeddings);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGenerateImage(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getImageGenerationRunner(options ->
            StepVerifier.create(client.generateImage(options))
                .assertNext(OpenAIClientTestBase::assertImageResponse)
                .verifyComplete());
    }
}
