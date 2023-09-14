// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.functions.MyFunctionCallArguments;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class NonAzureOpenAIAsyncClientTest extends OpenAIClientTestBase {
    private OpenAIAsyncClient client;

    private OpenAIAsyncClient getNonAzureOpenAIAsyncClient(HttpClient httpClient) {
        return getNonAzureOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .buildAsyncClient();
    }

    private OpenAIAsyncClient getNonAzureOpenAIAsyncClient(HttpClient httpClient, KeyCredential keyCredential) {
        return getNonAzureOpenAIClientBuilder(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .credential(keyCredential)
            .buildAsyncClient();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletions(modelId, new CompletionsOptions(prompt)))
                .assertNext(resultCompletions -> {
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsStream(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsFromSinglePromptRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletions(modelId, prompt))
                .assertNext(resultCompletions -> {
                    assertCompletions(1, resultCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletionsWithResponse(modelId,
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
    public void testGetCompletionsBadSecretKey(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(
            httpClient,
            new KeyCredential("not_token_looking_string"));

        getCompletionsRunner((modelId, prompt) -> {
            StepVerifier.create(client.getCompletionsWithResponse(modelId,
                    BinaryData.fromObject(new CompletionsOptions(prompt)),
                    new RequestOptions()))
                .verifyErrorSatisfies(throwable -> {
                    assertInstanceOf(ClientAuthenticationException.class, throwable);
                    assertEquals(401, ((ClientAuthenticationException) throwable).getResponse().getStatusCode());
                });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetCompletionsUsageField(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(modelId, new ChatCompletionsOptions(chatMessages)))
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((deploymentId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsStream(deploymentId, new ChatCompletionsOptions(chatMessages)))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> true)
                .consumeRecordedWith(messageList -> {
                    assertTrue(messageList.size() > 1);
                    messageList.forEach(OpenAIClientTestBase::assertChatCompletionsStream);
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsForNonAzureRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsWithResponse(modelId,
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingNonAzureRunner((modelId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(modelId, embeddingsOptions))
                .assertNext(resultEmbeddings -> assertEmbeddings(resultEmbeddings))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingNonAzureRunner((modelId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddingsWithResponse(modelId,
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
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getImageGenerationRunner(options ->
            StepVerifier.create(client.getImages(options))
                .assertNext(OpenAIClientTestBase::assertImageResponse)
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionAutoPreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatFunctionForNonAzureRunner((modelId, chatCompletionsOptions) -> {
            chatCompletionsOptions.setFunctionCall(FunctionCallConfig.AUTO);
            StepVerifier.create(client.getChatCompletions(modelId, chatCompletionsOptions))
                .assertNext(chatCompletions -> {
                    assertEquals(1, chatCompletions.getChoices().size());
                    ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                    MyFunctionCallArguments arguments = assertFunctionCall(
                        chatChoice,
                        "MyFunction",
                        MyFunctionCallArguments.class);
                    assertEquals(arguments.getLocation(), "San Francisco, CA");
                    assertEquals(arguments.getUnit(), "CELSIUS");
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionNonePreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatFunctionForNonAzureRunner((modelId, chatCompletionsOptions) -> {
            chatCompletionsOptions.setFunctionCall(FunctionCallConfig.NONE);
            StepVerifier.create(client.getChatCompletions(modelId, chatCompletionsOptions))
                .assertNext(chatCompletions -> {
                    assertChatCompletions(1, "stop", ChatRole.ASSISTANT, chatCompletions);
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionNotSuppliedByNamePreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatFunctionForNonAzureRunner((modelId, chatCompletionsOptions) -> {
            chatCompletionsOptions.setFunctionCall(new FunctionCallConfig("NotMyFunction"));
            StepVerifier.create(client.getChatCompletions(modelId, chatCompletionsOptions))
                .verifyErrorSatisfies(throwable -> {
                    assertInstanceOf(HttpResponseException.class, throwable);
                    HttpResponseException httpResponseException = (HttpResponseException) throwable;
                    assertEquals(400, httpResponseException.getResponse().getStatusCode());
                    assertTrue(httpResponseException.getMessage().contains("Invalid value for 'function_call'"));
                });
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatCompletionContentFiltering(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsContentFilterRunnerForNonAzure((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(modelId, new ChatCompletionsOptions(chatMessages)))
                .assertNext(chatCompletions -> {
                    assertNull(chatCompletions.getPromptFilterResults());
                    assertEquals(1, chatCompletions.getChoices().size());
                    assertNull(chatCompletions.getChoices().get(0).getContentFilterResults());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testCompletionContentFiltering(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getCompletionsContentFilterRunnerForNonAzure((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(Arrays.asList(prompt));
            StepVerifier.create(client.getCompletions(modelId, completionsOptions))
                .assertNext(completions -> {
                    assertCompletions(1, completions);
                    assertNull(completions.getPromptFilterResults());
                    assertNull(completions.getChoices().get(0).getContentFilterResults());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranscription(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunner((deploymentName, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranscriptionOptions transcriptionOptions = new AudioTranscriptionOptions(file);

//            AudioTranscription transcription = client.getAudioTranscription(deploymentName, transcriptionOptions);
//            assertNotNull(transcription);
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
            translationOptions.setResponseFormat(AudioTranscriptionFormat.JSON);

            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions, fileName))
                    .assertNext(translation ->
                        assertAudioTranscriptionSimpleJson(translation,"It's raining today."))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationVerboseJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
            translationOptions.setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON);

            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions, fileName))
                    .assertNext(translation ->
                        assertAudioTranscriptionVerboseJson(translation,"It's raining today."))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationTextPlain(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
            translationOptions.setResponseFormat(AudioTranscriptionFormat.TEXT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions, fileName))
                    .assertNext(translation -> {
                        assertEquals("It's raining today.\n", translation);
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationSrt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
            translationOptions.setResponseFormat(AudioTranscriptionFormat.SRT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions, fileName))
                    .assertNext(translation -> {
                        // Sequence number
                        assertTrue(translation.contains("1\n"));
                        // First sequence starts at timestamp 0
                        assertTrue(translation.contains("00:00:00,000 --> "));
                        // Actual translation value
                        assertTrue(translation.contains("It's raining today."));
                    }).verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationVtt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);
            translationOptions.setResponseFormat(AudioTranscriptionFormat.VTT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions, fileName))
                    .assertNext(translation -> {
                        // Start value according to spec
                        assertTrue(translation.startsWith("WEBVTT\n"));
                        // First sequence starts at timestamp 0. Note: unlike SRT, the millisecond separator is a "."
                        assertTrue(translation.contains("00:00:00.000 --> "));
                        // Actual translation value
                        assertTrue(translation.contains("It's raining today."));
                    }).verifyComplete();
        });
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationTextWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);

            translationOptions.setResponseFormat(AudioTranscriptionFormat.JSON);
            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions, fileName))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));

            translationOptions.setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON);
            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions, fileName))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationJsonWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, fileName) -> {
            byte[] file = BinaryData.fromFile(openTestResourceFile(fileName)).toBytes();
            AudioTranslationOptions translationOptions = new AudioTranslationOptions(file);

            translationOptions.setResponseFormat(AudioTranscriptionFormat.TEXT);
            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions, fileName))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));

            translationOptions.setResponseFormat(AudioTranscriptionFormat.SRT);
            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions, fileName))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));

            translationOptions.setResponseFormat(AudioTranscriptionFormat.VTT);
            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions, fileName))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
        });
    }
}
