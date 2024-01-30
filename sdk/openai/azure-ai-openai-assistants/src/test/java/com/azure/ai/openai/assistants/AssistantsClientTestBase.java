// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CodeInterpreterToolDefinition;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.ThreadInitializationMessage;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AssistantsClientTestBase extends TestProxyTestBase {

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false))
                .buildAsyncClient();
    }

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false), serviceVersion)
                .buildAsyncClient();
    }

    AssistantsClient getAssistantsClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true))
                .buildClient();
    }

    AssistantsClient getAssistantsClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                        interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true),
                serviceVersion)
                .buildClient();
    }

    AssistantsClientBuilder getAzureAssistantsClientBuilder(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        AssistantsClientBuilder builder = new AssistantsClientBuilder()
                .httpClient(httpClient)
                .serviceVersion(serviceVersion);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder
                    .endpoint("https://localhost:8080")
                    .credential(new AzureKeyCredential(TestUtils.FAKE_API_KEY));
        } else if (getTestMode() == TestMode.RECORD) {
            builder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                    .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")));
        } else {
            builder
                    .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                    .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")));
        }
        return builder;
    }

    AssistantsClientBuilder getAssistantsClientBuilder(HttpClient httpClient) {
        AssistantsClientBuilder builder = new AssistantsClientBuilder()
                .httpClient(httpClient);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new KeyCredential(TestUtils.FAKE_API_KEY));
        } else if (getTestMode() == TestMode.RECORD) {
            builder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY")));
        } else {
            builder.credential(new KeyCredential(Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY")));
        }
        return builder;
    }

    public static final String GPT_4_1106_PREVIEW = "gpt-4-1106-preview";

    void createAssistantsRunner(Consumer<AssistantCreationOptions> testRunner) {
        testRunner.accept(new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.")
                .setTools(Arrays.asList(new CodeInterpreterToolDefinition())));
    }

    void createAssistantsFileRunner(BiConsumer<AssistantCreationOptions, String> testRunner) {
        String fileId = "file-TYRl7zf7ecXsqYcBUDofznbA";
        testRunner.accept(new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                        .setName("Math Tutor")
                        .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.")
                        .setTools(Arrays.asList(new CodeInterpreterToolDefinition())),
                fileId);
    }

    void createThreadRunner(Consumer<AssistantThreadCreationOptions> testRunner) {
        testRunner.accept(new AssistantThreadCreationOptions()
                .setMessages(Arrays.asList(new ThreadInitializationMessage(MessageRole.USER,
                        "I need to solve the equation `3x + 11 = 14`. Can you help me?"))));
    }

    void createMessageRunner(Consumer<String> testRunner) {
        testRunner.accept("I need to solve the equation `3x + 11 = 14`. Can you help me?");
    }

    void submitMessageAndRunRunner(Consumer<String> testRunner) {
        testRunner.accept("I need to solve the equation `3x + 11 = 14`. Can you help me?");
    }

    void createThreadAndRunRunner(Consumer<CreateAndRunThreadOptions> testRunner, String assistantId) {
        testRunner.accept(
                new CreateAndRunThreadOptions(assistantId)
                        .setThread(new AssistantThreadCreationOptions()
                                .setMessages(Arrays.asList(new ThreadInitializationMessage(MessageRole.USER,
                                        "I need to solve the equation `3x + 11 = 14`. Can you help me?")))));

    }

    public HttpClient buildAssertingClient(HttpClient httpClient, boolean sync) {
        AssertingHttpClientBuilder builder = new AssertingHttpClientBuilder(httpClient)
                .skipRequest((ignored1, ignored2) -> false);
        if (sync) {
            builder.assertSync();
        } else {
            builder.assertAsync();
        }
        return builder.build();
    }

    static <T> T assertAndGetValueFromResponse(Response<BinaryData> actualResponse, Class<T> clazz, int expectedCode) {
        assertNotNull(actualResponse);
        assertEquals(expectedCode, actualResponse.getStatusCode());
        assertInstanceOf(Response.class, actualResponse);
        BinaryData binaryData = actualResponse.getValue();
        assertNotNull(binaryData);
        T object = binaryData.toObject(clazz);
        assertNotNull(object);
        assertInstanceOf(clazz, object);
        return object;
    }

    String createMathTutorAssistant(AssistantsClient client, ClientLogger logger) {
        logger.info("Creating a new Math tutor assistant.");
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // Create an assistant
        assertEquals(assistantCreationOptions.getName(), assistant.getName());
        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
        logger.info("Finished creating a new Math tutor assistant.");
        return assistant.getId();
    }

    String createMathTutorAssistant(AssistantsAsyncClient client, ClientLogger logger) {
        logger.info("Creating a new Math tutor assistant.");
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        AtomicReference<String> assistantIdRef = new AtomicReference<>();
        // create assistant test
        StepVerifier.create(client.createAssistant(assistantCreationOptions))
                .assertNext(assistant -> {
                    assistantIdRef.set(assistant.getId());
                    assertEquals(assistantCreationOptions.getName(), assistant.getName());
                    assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
                    assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
                })
                .verifyComplete();
        logger.info("Finished creating a new Math tutor assistant.");
        return assistantIdRef.get();
    }

    void deleteMathTutorAssistant(AssistantsClient client, String assistantId, ClientLogger logger) {
        logger.info("Cleaning up created Math tutor assistant.");
        if (CoreUtils.isNullOrEmpty(assistantId)) {
            return;
        }
        AssistantDeletionStatus deletionStatus = client.deleteAssistant(assistantId);
        assertEquals(assistantId, deletionStatus.getId());
        assertTrue(deletionStatus.isDeleted());
        logger.info("Finished cleaning up Math tutor assistant.");
    }

    void deleteMathTutorAssistant(AssistantsAsyncClient client, String assistantId, ClientLogger logger) {
        logger.info("Cleaning up created Math tutor  assistant.");
        if (CoreUtils.isNullOrEmpty(assistantId)) {
            return;
        }
        StepVerifier.create(client.deleteAssistant(assistantId))
                .assertNext(deletionStatus -> {
                    assertEquals(assistantId, deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                })
                .verifyComplete();
        logger.info("Finished cleaning up Math tutor assistant.");
    }

    String createThread(AssistantsAsyncClient client, ClientLogger logger) {
        logger.info("Creating a new thread.");
        AtomicReference<String> threadIdRef = new AtomicReference<>();
        // Create a simple thread without a message
        StepVerifier.create(client.createThread(new AssistantThreadCreationOptions()))
                .assertNext(assistantThread -> {
                    assertNotNull(assistantThread.getId());
                    assertNotNull(assistantThread.getCreatedAt());
                    assertEquals("thread", assistantThread.getObject());
                    threadIdRef.set(assistantThread.getId());
                })
                .verifyComplete();
        logger.info("Finished creating a new thread.");
        return threadIdRef.get();
    }

    String createThread(AssistantsClient client, ClientLogger logger) {
        logger.info("Creating a new thread.");
        // Create a simple thread without a message
        AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
        assertNotNull(assistantThread.getId());
        assertNotNull(assistantThread.getCreatedAt());
        assertEquals("thread", assistantThread.getObject());
        logger.info("Finished creating a new thread.");
        return assistantThread.getId();
    }

    void deleteThread(AssistantsAsyncClient client, String threadId, ClientLogger logger) {
        logger.info("Cleaning up created thread.");
        if (CoreUtils.isNullOrEmpty(threadId)) {
            return;
        }
        // Delete the created thread
        StepVerifier.create(client.deleteThread(threadId))
                .assertNext(deletionStatus -> {
                    assertEquals(threadId, deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                })
                .verifyComplete();
        logger.info("Finished cleaning up thread.");
    }

    void deleteThread(AssistantsClient client, String threadId, ClientLogger logger) {
        logger.info("Cleaning up created thread.");
        if (CoreUtils.isNullOrEmpty(threadId)) {
            return;
        }
        // Delete the created thread
        ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
        assertEquals(threadId, threadDeletionStatus.getId());
        assertTrue(threadDeletionStatus.isDeleted());
        logger.info("Finished cleaning up thread.");
    }


    void validateThreadMessage(ThreadMessage threadMessage, String threadId) {
        String threadMessageId = threadMessage.getId();
        assertNotNull(threadMessageId);
        assertEquals(threadId, threadMessage.getThreadId());
        assertNotNull(threadMessage.getCreatedAt());
        assertEquals("thread.message", threadMessage.getObject());
        assertEquals(MessageRole.USER, threadMessage.getRole());
        assertFalse(threadMessage.getContent().isEmpty());
    }
}
