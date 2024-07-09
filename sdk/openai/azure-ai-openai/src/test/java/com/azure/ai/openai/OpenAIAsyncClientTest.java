// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.AudioTaskLabel;
import com.azure.ai.openai.models.AudioTranscriptionFormat;
import com.azure.ai.openai.models.AudioTranscriptionTimestampGranularity;
import com.azure.ai.openai.models.AudioTranslationFormat;
import com.azure.ai.openai.models.AzureChatExtensionsMessageContext;
import com.azure.ai.openai.models.AzureSearchChatExtensionConfiguration;
import com.azure.ai.openai.models.AzureSearchChatExtensionParameters;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsFunctionToolCall;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolCall;
import com.azure.ai.openai.models.ChatResponseMessage;
import com.azure.ai.openai.models.ChatRole;
import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.CompletionsOptions;
import com.azure.ai.openai.models.CompletionsUsage;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.FunctionCall;
import com.azure.ai.openai.models.FunctionCallConfig;
import com.azure.ai.openai.models.OnYourDataApiKeyAuthenticationOptions;
import com.azure.ai.openai.models.OnYourDataContextProperty;
import com.azure.ai.openai.models.SpeechGenerationResponseFormat;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
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
import java.util.Iterator;
import java.util.List;

import static com.azure.ai.openai.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
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
        getCompletionsRunner((deploymentId, prompt) -> {
            deploymentId = "BAD_DEPLOYMENT_ID";
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
                    assertCompletions(1, resultCompletions))
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
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
    public void testGetChatCompletionsStreamWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsWithResponseRunner(deploymentId -> chatMessages -> requestOptions -> {
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
    public void getEmbeddingsWithSmallerDimensions(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
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
        getImageGenerationRunner((deploymentId, options) ->
            StepVerifier.create(client.getImageGenerations(deploymentId, options))
                .assertNext(OpenAIClientTestBase::assertImageGenerationsForAzure)
                .verifyComplete());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGenerateImageWithResponse(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getImageGenerationWithResponseRunner(deploymentId -> options -> requestOptions -> {
            StepVerifier.create(client.getImageGenerationsWithResponse(deploymentId, options, requestOptions))
                .assertNext(response -> {
                    assertResponseRequestHeader(response.getRequest());
                    assertImageGenerationsForAzure(response.getValue());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testContentFilterInputExceptionInImageGeneration(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        contentFilterInputExceptionRunner((deploymentId, options) -> {
            StepVerifier.create(client.getImageGenerations(deploymentId, options))
                    .expectErrorSatisfies(
                            ex -> validateImageGenerationContentFilteringException((HttpResponseException) ex))
                    .verify();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatFunctionAutoPreset(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatFunctionForRunner((modelId, chatCompletionsOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatFunctionForRunner((modelId, chatCompletionsOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatFunctionForRunner((modelId, chatCompletionsOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsContentFilterRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletions(modelId, new ChatCompletionsOptions(chatMessages)))
                .assertNext(chatCompletions -> {
                    assertSafePromptContentFilterResults(chatCompletions.getPromptFilterResults().get(0));
                    assertEquals(1, chatCompletions.getChoices().size());
                    assertSafeChoiceContentFilterResults(chatCompletions.getChoices().get(0).getContentFilterResults());
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatCompletionStreamContentFiltering(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatCompletionsContentFilterRunner((modelId, chatMessages) -> {
            StepVerifier.create(client.getChatCompletionsStream(modelId, new ChatCompletionsOptions(chatMessages)))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> {
                    assertChatCompletionsStream(chatCompletions);
                    return true;
                })
                .consumeRecordedWith(messageList -> {
                    assertTrue(messageList.size() > 1);

                    int i = 0;
                    for (Iterator<ChatCompletions> it = messageList.iterator(); it.hasNext();) {
                        ChatCompletions chatCompletions = it.next();
                        if (i == 0) {
                            // The first stream message has the prompt filter result
                            assertEquals(1, chatCompletions.getPromptFilterResults().size());
                            assertSafePromptContentFilterResults(chatCompletions.getPromptFilterResults().get(0));
                        } else if (i == 1) {
                            // The second message no longer has the prompt filter result, but contains a ChatChoice
                            // filter result with all the filter set to null. The roll is also ASSISTANT
                            assertEquals(ChatRole.ASSISTANT, chatCompletions.getChoices().get(0).getDelta().getRole());
                            assertNull(chatCompletions.getPromptFilterResults());
                            // TODO (team): change in behaviour, this used to be uncommented
//                            assertSafeChoiceContentFilterResults(chatCompletions.getChoices().get(0).getContentFilterResults());
                        } else if (i == messageList.size() - 1) {
                            // The last stream message is empty with all the filters set to null
                            assertEquals(1, chatCompletions.getChoices().size());
                            ChatChoice chatChoice = chatCompletions.getChoices().get(0);

                            assertEquals(CompletionsFinishReason.fromString("stop"), chatChoice.getFinishReason());
                            assertNotNull(chatChoice.getDelta());
                            assertNull(chatChoice.getDelta().getContent());
                            // TODO (team): change in behaviour, this used to be uncommented
//                            assertSafeChoiceContentFilterResults(chatChoice.getContentFilterResults());
                        } else {
                            // The rest of the intermediary messages have the text generation content filter set
                            assertNull(chatCompletions.getPromptFilterResults());
                            assertNotNull(chatCompletions.getChoices().get(0).getDelta());
                            assertSafeChoiceContentFilterResults(chatCompletions.getChoices().get(0).getContentFilterResults());
                        }
                        i++;
                    }
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testCompletionContentFiltering(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsContentFilterRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(Arrays.asList(prompt));
            // work around for this model, there seem to be some issues with Completions in gpt-turbo models
            completionsOptions.setMaxTokens(2000);
            StepVerifier.create(client.getCompletions(modelId, completionsOptions))
                .assertNext(completions -> {
                    assertCompletions(1, completions);
                    assertSafePromptContentFilterResults(completions.getPromptFilterResults().get(0));
                    assertSafeChoiceContentFilterResults(completions.getChoices().get(0).getContentFilterResults());
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testCompletionStreamContentFiltering(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getCompletionsContentFilterRunner((modelId, prompt) -> {
            CompletionsOptions completionsOptions = new CompletionsOptions(Arrays.asList(prompt));
            StepVerifier.create(client.getCompletionsStream(modelId, completionsOptions))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(chatCompletions -> {
                    assertCompletionsStream(chatCompletions);
                    return true;
                })
                .consumeRecordedWith(messageList -> {
                    assertTrue(messageList.size() > 1);

                    int i = 0;
                    for (Iterator<Completions> it = messageList.iterator(); it.hasNext();) {
                        Completions completions = it.next();
                        if (i == 0) {
                            assertEquals(1, completions.getPromptFilterResults().size());
                            assertSafePromptContentFilterResults(completions.getPromptFilterResults().get(0));
                        } else if (i == messageList.size() - 1) {
                            // The last stream message is empty with all the filters set to null
                            assertEquals(1, completions.getChoices().size());
                            Choice choice = completions.getChoices().get(0);
                            assertEquals(CompletionsFinishReason.fromString("stop"), choice.getFinishReason());
                            assertNotNull(choice.getText());
                            // TODO (team): change in behaviour, this used to be uncommented
//                            assertSafeChoiceContentFilterResults(choice.getContentFilterResults());
                        } else {
                        // The rest of the intermediary messages have the text generation content filter set
                            assertNull(completions.getPromptFilterResults());
                            assertNotNull(completions.getChoices().get(0));
                            assertSafeChoiceContentFilterResults(completions.getChoices().get(0).getContentFilterResults());
                        }
                        i++;
                    }
                })
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatCompletionsBasicSearchExtension(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getChatCompletionsAzureChatSearchRunner((deploymentName, chatCompletionsOptions) -> {
            AzureSearchChatExtensionParameters searchParameters = new AzureSearchChatExtensionParameters(
                    "https://openaisdktestsearch.search.windows.net",
                    "openai-test-index-carbon-wiki"
            );
            searchParameters.setAuthentication(new OnYourDataApiKeyAuthenticationOptions(getAzureCognitiveSearchKey()));
            AzureSearchChatExtensionConfiguration cognitiveSearchConfiguration =
                    new AzureSearchChatExtensionConfiguration(searchParameters);

            chatCompletionsOptions.setDataSources(Arrays.asList(cognitiveSearchConfiguration));

            StepVerifier.create(client.getChatCompletions(deploymentName, chatCompletionsOptions))
                .assertNext(OpenAIClientTestBase::assertChatCompletionsCognitiveSearch)
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatCompletionSearchExtensionContextPropertyFilter(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getChatCompletionsAzureChatSearchRunner((deploymentName, chatCompletionsOptions) -> {
            AzureSearchChatExtensionParameters searchParameters = new AzureSearchChatExtensionParameters(
                    "https://openaisdktestsearch.search.windows.net",
                    "openai-test-index-carbon-wiki"
            );

            // Only have citations in the search results, default are 'citations' and 'intent'.
            List<OnYourDataContextProperty> contextProperties = new ArrayList<>();
            contextProperties.add(OnYourDataContextProperty.CITATIONS);

            searchParameters.setAuthentication(new OnYourDataApiKeyAuthenticationOptions(getAzureCognitiveSearchKey()))
                    .setIncludeContexts(contextProperties);

            AzureSearchChatExtensionConfiguration cognitiveSearchConfiguration =
                    new AzureSearchChatExtensionConfiguration(searchParameters);

            chatCompletionsOptions.setDataSources(Arrays.asList(cognitiveSearchConfiguration));

            StepVerifier.create(client.getChatCompletions(deploymentName, chatCompletionsOptions))
                    .assertNext(chatCompletions -> {
                        AzureChatExtensionsMessageContext context = chatCompletions.getChoices().get(0)
                                .getMessage().getContext();
                        assertNotNull(context);
                        assertNull(context.getIntent());
                        assertFalse(CoreUtils.isNullOrEmpty(context.getCitations()));
                    })
                    .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testChatCompletionsStreamingBasicSearchExtension(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getChatCompletionsAzureChatSearchRunner((deploymentName, chatCompletionsOptions) -> {
            AzureSearchChatExtensionParameters searchParameters = new AzureSearchChatExtensionParameters(
                    "https://openaisdktestsearch.search.windows.net",
                    "openai-test-index-carbon-wiki"
            );
            searchParameters.setAuthentication(new OnYourDataApiKeyAuthenticationOptions(getAzureCognitiveSearchKey()));
            AzureSearchChatExtensionConfiguration cognitiveSearchConfiguration =
                    new AzureSearchChatExtensionConfiguration(searchParameters);

            chatCompletionsOptions.setDataSources(Arrays.asList(cognitiveSearchConfiguration));

            StepVerifier.create(client.getChatCompletionsStream(deploymentName, chatCompletionsOptions))
                .recordWith(ArrayList::new)
                .thenConsumeWhile(_chatCompletion -> true)
                .consumeRecordedWith(chatCompletions -> assertChatCompletionsStreamingCognitiveSearch(chatCompletions.stream()))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
            transcriptionOptions
                    .setResponseFormat(AudioTranscriptionFormat.VERBOSE_JSON)
                    .setTimestampGranularities(Arrays.asList(AudioTranscriptionTimestampGranularity.WORD,
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getAudioTranscriptionRunner((modelId, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.SRT);

            StepVerifier.create(client.getAudioTranscriptionText(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(translation -> {
                        // 1st Sequence number
                        assertTrue(translation.contains("1\n"));
                        // First sequence starts at timestamp 0
                        assertTrue(translation.contains("00:00:00,000 --> "));
                        // Transcription contains at least one expected word
                        assertTrue(translation.contains("Batman"));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranscriptionVtt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
            transcriptionOptions.setResponseFormat(AudioTranscriptionFormat.VTT);

            StepVerifier.create(client.getAudioTranscriptionText(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                    .assertNext(translation -> {
                        // Start value according to spec
                        assertTrue(translation.startsWith("WEBVTT\n"));
                        // First sequence starts at timestamp 0. Note: unlike SRT, the millisecond separator is a "."
                        assertTrue(translation.contains("00:00:00.000 --> "));
                        // Transcription contains at least one expected word
                        assertTrue(translation.contains("Batman"));
                    }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranscriptionTextWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        List<AudioTranscriptionFormat> wrongFormats = Arrays.asList(
                AudioTranscriptionFormat.JSON,
                AudioTranscriptionFormat.VERBOSE_JSON
        );

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
            for (AudioTranscriptionFormat format: wrongFormats) {
                transcriptionOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranscriptionText(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                        .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranscriptionJsonWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        List<AudioTranscriptionFormat> wrongFormats = Arrays.asList(
                AudioTranscriptionFormat.TEXT,
                AudioTranscriptionFormat.SRT,
                AudioTranscriptionFormat.VTT
        );

        getAudioTranscriptionRunner((deploymentName, transcriptionOptions) -> {
            for (AudioTranscriptionFormat format: wrongFormats) {
                transcriptionOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranscription(deploymentName, transcriptionOptions.getFilename(), transcriptionOptions))
                        .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.JSON);

            StepVerifier.create(client.getAudioTranslation(deploymentName, translationOptions.getFilename(), translationOptions))
                .assertNext(translation ->
                    assertAudioTranslationSimpleJson(translation, "It's raining today."))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationVerboseJson(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.VERBOSE_JSON);

            StepVerifier.create(client.getAudioTranslation(deploymentName, translationOptions.getFilename(), translationOptions))
                .assertNext(translation ->
                    assertAudioTranslationVerboseJson(translation, "It's raining today.", AudioTaskLabel.TRANSLATE))
                .verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationTextPlain(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.TEXT);

            StepVerifier.create(client.getAudioTranslationText(deploymentName, translationOptions.getFilename(), translationOptions))
                .assertNext(translation -> {
                    assertEquals("It's raining today.\n", translation);
                }).verifyComplete();
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    @RecordWithoutRequestBody
    public void testGetAudioTranslationSrt(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.SRT);

            StepVerifier.create(client.getAudioTranslationText(deploymentName, translationOptions.getFilename(), translationOptions))
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            translationOptions.setResponseFormat(AudioTranslationFormat.VTT);

            StepVerifier.create(client.getAudioTranslationText(deploymentName, translationOptions.getFilename(), translationOptions))
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        List<AudioTranslationFormat> wrongFormats = Arrays.asList(
            AudioTranslationFormat.JSON,
            AudioTranslationFormat.VERBOSE_JSON
        );

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            for (AudioTranslationFormat format: wrongFormats) {
                translationOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranslationText(deploymentName, translationOptions.getFilename(), translationOptions))
                                .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetAudioTranslationJsonWrongFormats(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        List<AudioTranslationFormat> wrongFormats = Arrays.asList(
            AudioTranslationFormat.TEXT,
            AudioTranslationFormat.SRT,
            AudioTranslationFormat.VTT
        );

        getAudioTranslationRunner((deploymentName, translationOptions) -> {
            for (AudioTranslationFormat format: wrongFormats) {
                translationOptions.setResponseFormat(format);
                StepVerifier.create(client.getAudioTranslation(deploymentName, translationOptions.getFilename(), translationOptions))
                        .verifyErrorSatisfies(error -> assertTrue(error instanceof IllegalArgumentException));
            }
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.openai.TestUtils#getTestParameters")
    public void testGetChatCompletionsToolCall(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatWithToolCallRunner((modelId, chatCompletionsOptions) ->
            StepVerifier.create(
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

                        // we should be passing responseMessage.getContent()) instead of ""; but it's null and Azure does not accept that
                        return client.getChatCompletions(modelId, getChatCompletionsOptionWithToolCallFollowUp(
                                functionToolCall, ""));
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        getChatWithToolCallRunner((modelId, chatCompletionsOptions) -> {
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
                                if (!CoreUtils.isNullOrEmpty(toolCalls)) {
                                    assertEquals(1, toolCalls.size());
                                    // TODO: can't not cast to ChatCompletionsFunctionToolCall but has to be ChatCompletionsToolCall
                                    ChatCompletionsFunctionToolCall toolCall = (ChatCompletionsFunctionToolCall) toolCalls.get(0);
                                    FunctionCall functionCall = toolCall.getFunction();

                                    // TODO: It used to be second stream event but now third event.
                                    // this data is only available in the on second stream message, if at all
                                    // The first contains filter results mostly
                                    if (i == 2) {
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

                        // we should be passing responseMessage.getContent()) instead of ""; but it's null and Azure does not accept that
                        ChatCompletionsOptions followUpChatCompletionsOptions = getChatCompletionsOptionWithToolCallFollowUp(
                                functionToolCall, "");

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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        textToSpeechRunner(((modelId, speechGenerationOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        textToSpeechRunner(((modelId, speechGenerationOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        textToSpeechRunner(((modelId, speechGenerationOptions) -> {
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
        client = getOpenAIAsyncClient(httpClient, serviceVersion);
        textToSpeechRunner(((modelId, speechGenerationOptions) -> {
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
