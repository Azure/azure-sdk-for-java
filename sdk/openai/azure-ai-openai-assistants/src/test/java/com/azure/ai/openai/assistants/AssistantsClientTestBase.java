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
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.RetrievalToolDefinition;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.ThreadInitializationMessage;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.UploadFileRequest;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.nio.file.Paths;
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

        if (getTestMode() != TestMode.LIVE) {
            addTestRecordCustomSanitizers();
            addCustomMatchers();
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

        if (getTestMode() != TestMode.LIVE) {
            addTestRecordCustomSanitizers();
            addCustomMatchers();
        }

        return builder;
    }

    private void addTestRecordCustomSanitizers() {
        interceptorManager.addSanitizers(Arrays.asList(
            new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("$..endpoint", null, "https://REDACTED", TestProxySanitizerType.BODY_KEY),
            new TestProxySanitizer("Content-Type", "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
                "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER)
        ));
    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setHeadersKeyOnlyMatch(Arrays.asList("Cookie", "Set-Cookie")));
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

    void createRetrievalRunner(BiConsumer<UploadFileRequest, AssistantCreationOptions> testRunner) {
        UploadFileRequest uploadRequest = new UploadFileRequest(
            new FileDetails(
                BinaryData.fromFile(openResourceFile("java_sdk_tests_assistants.txt")))
                    .setFilename("java_sdk_tests_assistants.txt"),
                FilePurpose.ASSISTANTS);

        AssistantCreationOptions assistantOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
            .setName("Java SDK Retrieval Sample")
            .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
            .setTools(Arrays.asList(new RetrievalToolDefinition()));

        testRunner.accept(uploadRequest, assistantOptions);
    }

    void uploadAssistantTextFileRunner(Consumer<UploadFileRequest> testRunner) {
        UploadFileRequest uploadFileRequest = new UploadFileRequest(
            new FileDetails(BinaryData.fromFile(openResourceFile("java_sdk_tests_assistants.txt"))),
            FilePurpose.ASSISTANTS);
        testRunner.accept(uploadFileRequest);
    }

    void uploadAssistantImageFileRunner(Consumer<UploadFileRequest> testRunner) {
        UploadFileRequest uploadFileRequest = new UploadFileRequest(
            new FileDetails(BinaryData.fromFile(openResourceFile("ms_logo.png"))),
            FilePurpose.ASSISTANTS);
        testRunner.accept(uploadFileRequest);
    }

    void uploadFineTuningJsonFileRunner(Consumer<UploadFileRequest> testRunner) {
        UploadFileRequest uploadFileRequest = new UploadFileRequest(
            new FileDetails(BinaryData.fromFile(openResourceFile("java_sdk_tests_fine_tuning.json"))),
            FilePurpose.FINE_TUNE);
        testRunner.accept(uploadFileRequest);
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

    protected static void assertFileEquals(OpenAIFile expected, OpenAIFile actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFilename(), actual.getFilename());
        assertEquals(expected.getBytes(), actual.getBytes());
        assertEquals(expected.getPurpose(), actual.getPurpose());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    protected static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }

    String createMathTutorAssistant(AssistantsClient client) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // Create an assistant
        assertEquals(assistantCreationOptions.getName(), assistant.getName());
        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
        return assistant.getId();
    }

    String createMathTutorAssistant(AssistantsAsyncClient client) {
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
        return assistantIdRef.get();
    }

    void deleteMathTutorAssistant(AssistantsClient client, String assistantId) {
        if (CoreUtils.isNullOrEmpty(assistantId)) {
            return;
        }
        AssistantDeletionStatus deletionStatus = client.deleteAssistant(assistantId);
        assertEquals(assistantId, deletionStatus.getId());
        assertTrue(deletionStatus.isDeleted());
    }

    void deleteMathTutorAssistant(AssistantsAsyncClient client, String assistantId) {
        if (CoreUtils.isNullOrEmpty(assistantId)) {
            return;
        }
        StepVerifier.create(client.deleteAssistant(assistantId))
                .assertNext(deletionStatus -> {
                    assertEquals(assistantId, deletionStatus.getId());
                    assertTrue(deletionStatus.isDeleted());
                })
                .verifyComplete();
    }

    String createThread(AssistantsAsyncClient client) {
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
        return threadIdRef.get();
    }

    String createThread(AssistantsClient client) {
        // Create a simple thread without a message
        AssistantThread assistantThread = client.createThread(new AssistantThreadCreationOptions());
        assertNotNull(assistantThread.getId());
        assertNotNull(assistantThread.getCreatedAt());
        assertEquals("thread", assistantThread.getObject());
        return assistantThread.getId();
    }

    void deleteThread(AssistantsAsyncClient client, String threadId) {
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
    }

    void deleteThread(AssistantsClient client, String threadId) {
        if (CoreUtils.isNullOrEmpty(threadId)) {
            return;
        }
        // Delete the created thread
        ThreadDeletionStatus threadDeletionStatus = client.deleteThread(threadId);
        assertEquals(threadId, threadDeletionStatus.getId());
        assertTrue(threadDeletionStatus.isDeleted());
    }

    ThreadRun createThreadAndRun(AssistantsAsyncClient client, CreateAndRunThreadOptions options) {
        AtomicReference<ThreadRun> threadRunRef = new AtomicReference<>();
        StepVerifier.create(client.createThreadAndRun(options))
                .assertNext(run -> {
                    assertNotNull(run.getId());
                    assertNotNull(run.getCreatedAt());
                    assertEquals("thread.run", run.getObject());
                    assertNotNull(run.getInstructions());
                    threadRunRef.set(run);
                })
                .verifyComplete();
        return threadRunRef.get();
    }

    ThreadRun createThreadAndRun(AssistantsClient client, CreateAndRunThreadOptions options) {
        ThreadRun run = client.createThreadAndRun(options);
        assertNotNull(run.getId());
        assertNotNull(run.getCreatedAt());
        assertEquals("thread.run", run.getObject());
        assertNotNull(run.getInstructions());
        return run;
    }

    void validateThreadRun(ThreadRun expect, ThreadRun actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getThreadId(), actual.getThreadId());
        assertEquals(expect.getAssistantId(), actual.getAssistantId());
        assertEquals(expect.getCreatedAt(), actual.getCreatedAt());
        assertEquals(expect.getCompletedAt(), actual.getCompletedAt());
        assertEquals(expect.getInstructions(), actual.getInstructions());
        assertEquals(expect.getObject(), actual.getObject());
        assertEquals(expect.getModel(), actual.getModel());
    }

    void validateRunStep(RunStep expect, RunStep actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getRunId(), actual.getRunId());
        assertEquals(expect.getThreadId(), actual.getThreadId());
        assertEquals(expect.getAssistantId(), actual.getAssistantId());
        assertEquals(expect.getObject(), actual.getObject());
        assertEquals(expect.getType(), actual.getType());
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
