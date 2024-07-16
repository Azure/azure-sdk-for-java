// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AudioTaskLabel;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionTimestampGranularity;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.ai.openai.models.SpeechGenerationResponseFormat;
import com.azure.core.credential.KeyCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        getCompletionsRunnerForNonAzure((modelId, prompt) -> {
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
        getCompletionsRunnerForNonAzure((deploymentId, prompt) -> {
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
        getCompletionsFromSinglePromptRunnerForNonAzure((modelId, prompt) -> {
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
        getCompletionsRunnerForNonAzure((modelId, prompt) -> {
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

        getCompletionsRunnerForNonAzure((modelId, prompt) -> {
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
        getCompletionsRunnerForNonAzure((modelId, prompt) -> {
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
        getCompletionsRunnerForNonAzure((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(prompt);
            completionsOptions.setMaxTokens(3);
            StepVerifier.create(client.getCompletions(modelId, completionsOptions))
                .assertNext(resultCompletions ->
                    assertCompletions(1, resultCompletions))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsRunnerForNonAzure((modelId, chatMessages) -> {
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
        getChatCompletionsRunnerForNonAzure((deploymentId, chatMessages) -> {
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
    public void testGetChatCompletionsStreamWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsWithResponseRunnerForNonAzure(deploymentId -> chatMessages -> requestOptions -> {
            StepVerifier.create(client.getChatCompletionsStreamWithResponse(deploymentId,
                            new ChatCompletionsOptions(chatMessages), requestOptions))
                    .recordWith(ArrayList::new)
                    .thenConsumeWhile(response -> {
                        assertResponseRequestHeader(response.getRequest());
                        assertChatCompletionsStream(response.getValue());
                        return true;
                    })
                    .consumeRecordedWith(messageList -> assertTrue(messageList.size() > 1))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatCompletionsRunnerForNonAzure((modelId, chatMessages) -> {
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
        getEmbeddingRunnerForNonAzure((modelId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(modelId, embeddingsOptions))
                .assertNext(resultEmbeddings -> assertEmbeddings(resultEmbeddings))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void getEmbeddingsWithSmallerDimensions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingWithSmallerDimensionsRunner((deploymentId, embeddingsOptions) -> {
            StepVerifier.create(client.getEmbeddings(deploymentId, embeddingsOptions))
                    .assertNext(resultEmbeddings -> {
                        assertEmbeddings(resultEmbeddings);
                        assertEquals(embeddingsOptions.getDimensions(),
                                resultEmbeddings.getData().get(0).getEmbedding().size());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetEmbeddingsWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getEmbeddingRunnerForNonAzure((modelId, embeddingsOptions) -> {
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
        getImageGenerationRunner((deploymentOrModelName, options) ->
            StepVerifier.create(client.getImageGenerations(deploymentOrModelName, options))
                .assertNext(OpenAIClientTestBase::assertImageGenerations)
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGenerateImageWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getImageGenerationWithResponseRunner(deploymentId -> options -> requestOptions -> {
            StepVerifier.create(client.getImageGenerationsWithResponse(deploymentId, options, requestOptions))
                    .assertNext(response -> {
                        assertResponseRequestHeader(response.getRequest());
                        assertImageGenerations(response.getValue());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionAutoPreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatFunctionRunnerForNonAzure((modelId, chatCompletionsOptions) -> {
            chatCompletionsOptions.setFunctionCall(FunctionCallConfig.AUTO);
            StepVerifier.create(client.getChatCompletions(modelId, chatCompletionsOptions))
                .assertNext(chatCompletions -> {
                    assertEquals(1, chatCompletions.getChoices().size());
                    ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                    MyFunctionCallArguments arguments = assertFunctionCall(
                        chatChoice,
                        MyFunctionCallArguments.class);
                    assertTrue(arguments.getLocation().contains("San Francisco"));
                    assertEquals(arguments.getUnit(), "CELSIUS");
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionNonePreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatFunctionRunnerForNonAzure((modelId, chatCompletionsOptions) -> {
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
        getChatFunctionRunnerForNonAzure((modelId, chatCompletionsOptions) -> {
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
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.JSON);
            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription ->
                            assertAudioTranscriptionSimpleJson(transcription, BATMAN_TRANSCRIPTION))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionVerboseJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON);

            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription ->
                            assertAudioTranscriptionVerboseJson(transcription, BATMAN_TRANSCRIPTION, AudioTaskLabel.TRANSCRIBE))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testAudioTranscriptionTimestampGranularityInWord(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON)
                    .setTimestampGranularities(Arrays.asList(AudioTranscriptionTimestampGranularity.WORD));

            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription -> {
                        assertNull(transcription.getSegments());
                        assertAudioTranscriptionWords(transcription.getWords());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testAudioTranscriptionTimestampGranularityInSegment(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON)
                    .setTimestampGranularities(Arrays.asList(AudioTranscriptionTimestampGranularity.SEGMENT));

            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription -> {
                        assertAudioTranscriptionSegments(transcription.getSegments());
                        assertNull(transcription.getWords());
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testAudioTranscriptionTimestampGranularityInBothSegmentAndWord(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON)
                    .setTimestampGranularities(Arrays.asList(
                            AudioTranscriptionTimestampGranularity.SEGMENT,
                            AudioTranscriptionTimestampGranularity.WORD));

            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription -> {
                        assertAudioTranscriptionSegments(transcription.getSegments());
                        assertAudioTranscriptionWords(transcription.getWords());
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testAudioTranscriptionDuplicateTimestampGranularity(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON)
                    .setTimestampGranularities(Arrays.asList(
                            AudioTranscriptionTimestampGranularity.WORD,
                            AudioTranscriptionTimestampGranularity.WORD));

            StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription -> {
                        assertNull(transcription.getSegments());
                        assertAudioTranscriptionWords(transcription.getWords());
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testAudioTranscriptionTimestampGranularityInWrongResponseFormat(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getAudioTranscriptionRunnerForNonAzure((modelId, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.JSON)
                    .setTimestampGranularities(Arrays.asList(AudioTranscriptionTimestampGranularity.WORD));

            StepVerifier.create(client.getAudioTranscription(modelId, transcriptionOptions.getFilename(),
                    transcriptionOptions))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof HttpResponseException));
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionTextPlain(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((deploymentName, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.TEXT);

            StepVerifier.create(client.getAudioTranscriptionText(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(transcription ->
                            // A plain/text request adds a line break as an artifact. Also observed for translations
                            assertEquals(BATMAN_TRANSCRIPTION + "\n", transcription))
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionSrt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((modelId, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.SRT);

            StepVerifier.create(client.getAudioTranscriptionText(modelId, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(translation -> {
                        // Sequence number
                        assertTrue(translation.contains("1\n"));
                        // First sequence starts at timestamp 0
                        assertTrue(translation.contains("00:00:00,000 --> "));
                        // Contains at least one expected word
                        assertTrue(translation.contains("Batman"));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionVtt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranscriptionRunnerForNonAzure((modelId, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.VTT);

            StepVerifier.create(client.getAudioTranscriptionText(modelId, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(translation -> {
                        // Start value according to spec
                        assertTrue(translation.startsWith("WEBVTT\n"));
                        // First sequence starts at timestamp 0. Note: unlike SRT, the millisecond separator is a "."
                        assertTrue(translation.contains("00:00:00.000 --> "));
                        // Contains at least one expected word
                        assertTrue(translation.contains("Batman"));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranscriptionTextWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        List<AudioTranscriptionFormat> wrongFormats = Arrays.asList(
                AudioTranscriptionFormat.JSON,
                AudioTranscriptionFormat.VERBOSE_JSON
        );

        getAudioTranscriptionRunnerForNonAzure((modelId, transcriptionOptions) -> {
            for (AudioTranscriptionFormat format: wrongFormats) {
                transcriptionOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranscriptionText(modelId, transcriptionOptions.getFilename(), transcriptionOptions))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranscriptionJsonWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        List<AudioTranscriptionFormat> wrongFormats = Arrays.asList(
                AudioTranscriptionFormat.TEXT,
                AudioTranscriptionFormat.SRT,
                AudioTranscriptionFormat.VTT
        );

        getAudioTranscriptionRunnerForNonAzure((modelId, transcriptionOptions) -> {
            for (AudioTranscriptionFormat format: wrongFormats) {
                transcriptionOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranscription(modelId, transcriptionOptions.getFilename(), transcriptionOptions))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.JSON);

            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions.getFilename(), translationOptions))
                .assertNext(translation ->
                    assertAudioTranslationSimpleJson(translation, "It's raining today."))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationVerboseJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.VERBOSE_JSON);

            StepVerifier.create(client.getAudioTranslation(modelId, translationOptions.getFilename(), translationOptions))
                .assertNext(translation ->
                    assertAudioTranslationVerboseJson(translation, "It's raining today.", AudioTaskLabel.TRANSLATE))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationTextPlain(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.TEXT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions.getFilename(), translationOptions))
                .assertNext(translation -> {
                    assertEquals("It's raining today.\n", translation);
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationSrt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.SRT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions.getFilename(), translationOptions))
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
    @RecordWithoutRequestBody
    public void testGetAudioTranslationVtt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.VTT);

            StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions.getFilename(), translationOptions))
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
        List<AudioTranslationFormat> wrongFormats = Arrays.asList(
            AudioTranslationFormat.JSON,
            AudioTranslationFormat.VERBOSE_JSON
        );

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            for (AudioTranslationFormat format: wrongFormats) {
                translationOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranslationText(modelId, translationOptions.getFilename(), translationOptions))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationJsonWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        List<AudioTranslationFormat> wrongFormats = Arrays.asList(
            AudioTranslationFormat.TEXT,
            AudioTranslationFormat.SRT,
            AudioTranslationFormat.VTT
        );

        getAudioTranslationRunnerForNonAzure((modelId, translationOptions) -> {
            for (AudioTranslationFormat format: wrongFormats) {
                translationOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranslation(modelId, translationOptions.getFilename(), translationOptions))
                    .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsVision(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatWithVisionRunnerForNonAzure(((modelId, chatRequestMessages) -> {
            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatRequestMessages);
            chatCompletionsOptions.setMaxTokens(2048);
            StepVerifier.create(client.getChatCompletions(modelId, chatCompletionsOptions))
                    .assertNext(OpenAIClientTestBase::assertVisionChatCompletions)
                    .verifyComplete();
        }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsToolCall(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatWithToolCallRunnerForNonAzure((modelId, chatCompletionsOptions) -> StepVerifier.create(
                client.getChatCompletionsWithResponse(modelId, chatCompletionsOptions, new RequestOptions())
                    .flatMap(response -> {
                        assertNotNull(response);
                        assertTrue(response.getStatusCode() >= 200 && response.getStatusCode() < 300);
                        ChatCompletions chatCompletions = response.getValue();
                        assertNotNull(chatCompletions);

                        assertTrue(chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty());
                        ChatChoice chatChoice = chatCompletions.getChoices().get(0);
                        assertEquals(chatChoice.getFinishReason(), CompletionsFinishReason.TOOL_CALLS);

                        ChatResponseMessage responseMessage = chatChoice.getMessage();
                        assertNotNull(responseMessage);
                        assertTrue(responseMessage.getContent() == null || responseMessage.getContent().isEmpty());
                        assertFalse(responseMessage.getToolCalls() == null || responseMessage.getToolCalls().isEmpty());

                        ChatCompletionsFunctionToolCall functionToolCall = (ChatCompletionsFunctionToolCall) responseMessage.getToolCalls().get(0);
                        assertNotNull(functionToolCall);
                        assertFalse(functionToolCall.getFunction().getArguments() == null
                                || functionToolCall.getFunction().getArguments().isEmpty());
                        return client.getChatCompletions(modelId, getChatCompletionsOptionWithToolCallFollowUp(
                                functionToolCall, responseMessage.getContent()));
                    })).assertNext(followUpChatCompletions -> {
                        assertNotNull(followUpChatCompletions);
                        assertNotNull(followUpChatCompletions.getChoices());
                        ChatChoice followUpChatChoice = followUpChatCompletions.getChoices().get(0);
                        assertNotNull(followUpChatChoice);
                        assertNotNull(followUpChatChoice.getMessage());
                        String content = followUpChatChoice.getMessage().getContent();
                        assertFalse(content == null || content.isEmpty());
                        assertEquals(followUpChatChoice.getMessage().getRole(), ChatRole.ASSISTANT);
                        assertEquals(followUpChatChoice.getFinishReason(), CompletionsFinishReason.STOPPED);
                    }).verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsToolCallStreaming(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        getChatWithToolCallRunnerForNonAzure((modelId, chatCompletionsOptions) -> {
            StepVerifier.create(client.getChatCompletionsStream(modelId, chatCompletionsOptions)
                    .collectList()
                    .flatMapMany(chatCompletionsStream -> {
                        StringBuilder argumentsBuilder = new StringBuilder();
                        long totalStreamMessages = chatCompletionsStream.size();
                        String functionName = null;
                        String toolCallId = null;
                        String content = null;
                        assertTrue(totalStreamMessages > 0);

                        int i = 0;
                        for (ChatCompletions chatCompletions : chatCompletionsStream) {
                            List<ChatChoice> chatChoices = chatCompletions.getChoices();
                            if (!chatChoices.isEmpty() && chatChoices.get(0) != null) {
                                assertEquals(1, chatChoices.size());
                                ChatChoice chatChoice = chatChoices.get(0);
                                List<ChatCompletionsToolCall> toolCalls = chatChoice.getDelta().getToolCalls();
                                if (toolCalls != null && !toolCalls.isEmpty()) {
                                    assertEquals(1, toolCalls.size());
                                    ChatCompletionsFunctionToolCall toolCall = (ChatCompletionsFunctionToolCall) toolCalls.get(0);
                                    FunctionCall functionCall = toolCall.getFunction();
                                    // TODO: It used to be first stream event but now second event, in NonAzure
                                    // this data is only available in the first stream message, if at all
                                    if (i == 1) {
                                        content = chatChoice.getDelta().getContent();
                                        functionName = functionCall.getName();
                                        toolCallId = toolCall.getId();
                                    }
                                    argumentsBuilder.append(functionCall.getArguments());
                                }
                                if (i < totalStreamMessages - 1) {
                                    assertNull(chatChoice.getFinishReason());
                                } else {
                                    assertEquals(CompletionsFinishReason.TOOL_CALLS, chatChoice.getFinishReason());
                                }
                            }
                            i++;
                        }
                        assertFunctionToolCallArgs(argumentsBuilder.toString());
                        FunctionCall functionCall = new FunctionCall(functionName, argumentsBuilder.toString());
                        ChatCompletionsFunctionToolCall functionToolCall = new ChatCompletionsFunctionToolCall(toolCallId, functionCall);

                        ChatCompletionsOptions followUpChatCompletionsOptions = getChatCompletionsOptionWithToolCallFollowUp(
                                functionToolCall, content);

                        return client.getChatCompletionsStream(modelId, followUpChatCompletionsOptions);
                    })
                    .collectList()
            ).assertNext(followupChatCompletionsStream -> {
                StringBuilder contentBuilder = new StringBuilder();
                long totalStreamFollowUpMessages = followupChatCompletionsStream.size();
                int j = 0;

                for (ChatCompletions chatCompletions: followupChatCompletionsStream) {
                    List<ChatChoice> chatChoices = chatCompletions.getChoices();
                    if (!chatChoices.isEmpty() && chatChoices.get(0) != null) {
                        assertEquals(1, chatChoices.size());
                        ChatChoice chatChoice = chatChoices.get(0);
                        contentBuilder.append(chatChoice.getDelta().getContent());
                        if (j < totalStreamFollowUpMessages - 1) {
                            assertNull(chatChoice.getFinishReason());
                        } else {
                            assertEquals(CompletionsFinishReason.STOPPED, chatChoice.getFinishReason());
                        }
                    }
                    j++;
                }
                assertFalse(CoreUtils.isNullOrEmpty(contentBuilder.toString()));
            }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testTextToSpeech(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        textToSpeechRunnerForNonAzure(((modelId, speechGenerationOptions) -> {
            StepVerifier.create(client.generateSpeechFromText(modelId, speechGenerationOptions))
                    .assertNext(speech -> {
                        assertNotNull(speech);
                        byte[] bytes = speech.toBytes();
                        assertNotNull(bytes);
                        assertTrue(bytes.length > 0);
                    }).verifyComplete();
        }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testTextToSpeechWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        textToSpeechRunnerForNonAzure(((modelId, speechGenerationOptions) -> {
            StepVerifier.create(client.generateSpeechFromTextWithResponse(modelId,
                            BinaryData.fromObject(speechGenerationOptions), new RequestOptions()))
                    .assertNext(response -> {
                        assertTrue(response.getStatusCode() > 0);
                        assertNotNull(response.getHeaders());
                        BinaryData speech = response.getValue();
                        assertNotNull(speech);
                        byte[] bytes = speech.toBytes();
                        assertNotNull(bytes);
                        assertTrue(bytes.length > 0);
                    }).verifyComplete();
        }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void generateSpeechInMp3(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        textToSpeechRunnerForNonAzure(((modelId, speechGenerationOptions) -> {
            speechGenerationOptions.setResponseFormat(SpeechGenerationResponseFormat.MP3);
            StepVerifier.create(client.generateSpeechFromText(modelId, speechGenerationOptions))
                    .assertNext(speech -> {
                        assertNotNull(speech);
                        byte[] bytes = speech.toBytes();
                        assertNotNull(bytes);
                        assertTrue(bytes.length > 0);
                    }).verifyComplete();
        }));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void generateSpeechInWav(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getNonAzureOpenAIAsyncClient(httpClient);
        textToSpeechRunnerForNonAzure(((modelId, speechGenerationOptions) -> {
            speechGenerationOptions.setResponseFormat(SpeechGenerationResponseFormat.WAV);
            StepVerifier.create(client.generateSpeechFromText(modelId, speechGenerationOptions))
                    .assertNext(speech -> {
                        assertNotNull(speech);
                        byte[] bytes = speech.toBytes();
                        assertNotNull(bytes);
                        assertTrue(bytes.length > 0);
                    }).verifyComplete();
        }));
    }
}
