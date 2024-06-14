// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.implementation.FunctionsToolCallHelper;
import com.azure.ai.openai.assistants.implementation.accesshelpers.PageableListAccessHelper;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantDeletionStatus;
import com.azure.ai.openai.assistants.models.AssistantStreamEvent;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CodeInterpreterToolDefinition;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.FileDeletionStatus;
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.FileSearchToolDefinition;
import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RunStep;
import com.azure.ai.openai.assistants.models.StreamUpdate;
import com.azure.ai.openai.assistants.models.ThreadDeletionStatus;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.ToolDefinition;
import com.azure.ai.openai.assistants.models.VectorStoreDeletionStatus;
import com.azure.ai.openai.assistants.models.VectorStoreUpdateOptions;
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
import com.azure.json.JsonReader;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AssistantsClientTestBase extends TestProxyTestBase {
    // Remove the `id`, `name`, `Set-Cookie` sanitizers from the list of common sanitizers.
    // See list of sanitizers: https://github.com/Azure/azure-sdk-tools/blob/main/tools/test-proxy/Azure.Sdk.Tools.TestProxy/Common/SanitizerDictionary.cs
    private static final String[] REMOVE_SANITIZER_ID = {"AZSDK3430", "AZSDK3493", "AZSDK2015"};

    private static final String JAVA_SDK_TESTS_ASSISTANTS_TXT =  "java_sdk_tests_assistants.txt";
    private static final String JAVA_SDK_TESTS_FINE_TUNING_JSON = "java_sdk_tests_fine_tuning.json";
    private static final String MS_LOGO_PNG = "ms_logo.png";

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false))
            .buildAsyncClient();
    }

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
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

    AssistantsClient getAssistantsClient(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                        interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true),
                serviceVersion)
                .buildClient();
    }

    AssistantsClientBuilder getAzureAssistantsClientBuilder(HttpClient httpClient, AssistantsServiceVersion serviceVersion) {
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
            removeDefaultSanitizers();
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
            removeDefaultSanitizers();
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

    private void removeDefaultSanitizers() {
        interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
    }

    public static final String GPT_4_1106_PREVIEW = "gpt-4-1106-preview";

    void createAssistantsRunner(Consumer<AssistantCreationOptions> testRunner) {
        testRunner.accept(new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.")
                .setTools(Arrays.asList(new CodeInterpreterToolDefinition())));
    }

    void createRunRunner(Consumer<AssistantThreadCreationOptions> testRunner) {
        testRunner.accept(new AssistantThreadCreationOptions()
                .setMessages(Arrays.asList(new ThreadMessageOptions(MessageRole.USER,
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
                                .setMessages(Arrays.asList(new ThreadMessageOptions(MessageRole.USER,
                                        "I need to solve the equation `3x + 11 = 14`. Can you help me?")))));

    }

    void createThreadRunWithFunctionCallRunner(Consumer<CreateAndRunThreadOptions> testRunner, String assistantId) {
        testRunner.accept(
            new CreateAndRunThreadOptions(assistantId)
                .setThread(new AssistantThreadCreationOptions()
                    .setMessages(Arrays.asList(new ThreadMessageOptions(MessageRole.USER,
                        "Please make a graph for my boilerplate equation")))));

    }

    void createRunRunner(Consumer<CreateRunOptions> testRunner, String assistantId) {
        testRunner.accept(new CreateRunOptions(assistantId));
    }

    void createRetrievalRunner(BiConsumer<FileDetails, AssistantCreationOptions> testRunner) {
        FileDetails fileDetails = new FileDetails(
            BinaryData.fromFile(openResourceFile("java_sdk_tests_assistants.txt")), "java_sdk_tests_assistants.txt");

        AssistantCreationOptions assistantOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
            .setName("Java SDK Retrieval Sample")
            .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
            .setTools(Arrays.asList(new FileSearchToolDefinition()));

        testRunner.accept(fileDetails, assistantOptions);
    }

    void createFunctionToolCallRunner(BiConsumer<AssistantCreationOptions, AssistantThreadCreationOptions> testRunner) {
        FunctionsToolCallHelper functionsToolCallHelper = new FunctionsToolCallHelper();
        List<ToolDefinition> toolDefinition = Arrays.asList(
            functionsToolCallHelper.getAirlinePriceToDestinationForSeasonDefinition(),
            functionsToolCallHelper.getFavoriteVacationDestinationDefinition(),
            functionsToolCallHelper.getPreferredAirlineForSeasonDefinition()
        );
        AssistantCreationOptions assistantOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
            .setName("Java SDK Function Tool Call Test")
            .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
            .setTools(toolDefinition);

        AssistantThreadCreationOptions threadCreationOptions = new AssistantThreadCreationOptions();

        testRunner.accept(assistantOptions, threadCreationOptions);
    }

    void uploadAssistantTextFileRunner(BiConsumer<FileDetails, FilePurpose> testRunner) {
        String fileName = JAVA_SDK_TESTS_ASSISTANTS_TXT;
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        testRunner.accept(fileDetails, FilePurpose.ASSISTANTS);
    }

    void uploadAssistantImageFileRunner(BiConsumer<FileDetails, FilePurpose> testRunner) {
        String fileName = MS_LOGO_PNG;
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        testRunner.accept(fileDetails, FilePurpose.ASSISTANTS);
    }

    void uploadFineTuningJsonFileRunner(BiConsumer<FileDetails, FilePurpose> testRunner) {
        String fileName = JAVA_SDK_TESTS_FINE_TUNING_JSON;
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);
        testRunner.accept(fileDetails, FilePurpose.FINE_TUNE);
    }

    void modifyVectorStoreRunner(Consumer<VectorStoreUpdateOptions> testRunner) {
        VectorStoreUpdateOptions updateVectorStoreOptions = new VectorStoreUpdateOptions()
                .setName("updatedName");
        testRunner.accept(updateVectorStoreOptions);
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

    static <T> PageableList<T> asserAndGetPageableListFromResponse(Response<BinaryData> actualResponse, int expectedCode,
                                                     CheckedFunction<JsonReader, List<T>> readListFunction) {
        assertNotNull(actualResponse);
        assertEquals(expectedCode, actualResponse.getStatusCode());
        assertInstanceOf(Response.class, actualResponse);
        BinaryData binaryData = actualResponse.getValue();
        assertNotNull(binaryData);
        PageableList<T> object = null;
        try {
            object = PageableListAccessHelper.create(binaryData, readListFunction);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(object);
        return object;
    }

    protected interface CheckedFunction<T, R> extends Function<T, R> {

        @Override
        default R apply(T t) {
            try {
                return applyThrows(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        R applyThrows(T t) throws Exception;
    }

    protected static void assertFileEquals(OpenAIFile expected, OpenAIFile actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFilename(), actual.getFilename());
        assertEquals(expected.getBytes(), actual.getBytes());
        assertEquals(expected.getPurpose(), actual.getPurpose());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    public static void assertStreamUpdate(StreamUpdate event) {
        assertNotNull(event);
        assertNotNull(event.getKind());
        assertTrue(AssistantStreamEvent.values().contains(event.getKind()));
    }

    public static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }

    String createMathTutorAssistant(AssistantsClient client) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.")
                .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
        return createAssistant(client, assistantCreationOptions);
    }

    String createMathTutorAssistantWithFunctionTool(AssistantsClient client) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a helpful math assistant that helps with visualizing equations. Use the code "
                    + "interpreter tool when asked to generate images. Use provided functions to resolve appropriate unknown values")
                .setTools(Arrays.asList(
                    new CodeInterpreterToolDefinition(),
                    new FunctionToolDefinition(
                        new FunctionDefinition("get_boilerplate_equation", BinaryData.fromString("{\"type\":\"object\",\"properties\":{}}"))
                            .setDescription("Retrieves a predefined 'boilerplate equation' from the caller")
                )));
        return createAssistant(client, assistantCreationOptions);
    }

    String createMathTutorAssistantWithFunctionTool(AssistantsAsyncClient client) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
            .setName("Math Tutor")
            .setInstructions("You are a helpful math assistant that helps with visualizing equations. Use the code "
                + "interpreter tool when asked to generate images. Use provided functions to resolve appropriate unknown values")
            .setTools(Arrays.asList(
                new CodeInterpreterToolDefinition(),
                new FunctionToolDefinition(
                    new FunctionDefinition("get_boilerplate_equation", BinaryData.fromString("{\"type\":\"object\",\"properties\":{}}"))
                        .setDescription("Retrieves a predefined 'boilerplate equation' from the caller")
                )));
        return createAssistant(client, assistantCreationOptions);
    }

    String createMathTutorAssistant(AssistantsAsyncClient client) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.")
                .setTools(Arrays.asList(new CodeInterpreterToolDefinition()));
        return createAssistant(client, assistantCreationOptions);
    }

    String uploadFile(AssistantsClient client, String fileName, FilePurpose filePurpose) {
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);

        OpenAIFile openAIFile = client.uploadFile(
            fileDetails,
            filePurpose);
        assertNotNull(openAIFile.getId());
        assertNotNull(openAIFile.getCreatedAt());
        return openAIFile.getId();
    }

    String uploadFileAsync(AssistantsAsyncClient client, String fileName, FilePurpose filePurpose) {
        FileDetails fileDetails = new FileDetails(BinaryData.fromFile(openResourceFile(fileName)), fileName);

        OpenAIFile openAIFile = client.uploadFile(
                fileDetails,
                filePurpose).block();
        assertNotNull(openAIFile.getId());
        assertNotNull(openAIFile.getCreatedAt());
        return openAIFile.getId();
    }

    void deleteFiles(AssistantsClient client, String... fileIds) {
        if (CoreUtils.isNullOrEmpty(fileIds)) {
            return;
        }
        for (String fileId : fileIds) {
            FileDeletionStatus deletionStatus = client.deleteFile(fileId);
            assertEquals(fileId, deletionStatus.getId());
            assertTrue(deletionStatus.isDeleted());
        }
    }

    void deleteFilesAsync(AssistantsAsyncClient client, String... fileIds) {
        if (CoreUtils.isNullOrEmpty(fileIds)) {
            return;
        }
        for (String fileId : fileIds) {
            StepVerifier.create(client.deleteFile(fileId))
                    .assertNext(deletionStatus -> {
                        assertEquals(fileId, deletionStatus.getId());
                        assertTrue(deletionStatus.isDeleted());
                    })
                    .verifyComplete();
        }
    }

    void deleteVectorStores(AssistantsClient client, String... vectorStoreIds) {
        if (!CoreUtils.isNullOrEmpty(vectorStoreIds)) {
            for (String vectorStoreId : vectorStoreIds) {
                VectorStoreDeletionStatus vectorStoreDeletionStatus = client.deleteVectorStore(vectorStoreId);
                assertTrue(vectorStoreDeletionStatus.isDeleted());
            }
        }
    }

    void deleteVectorStoresAsync(AssistantsAsyncClient client, String... vectorStoreIds) {
        if (!CoreUtils.isNullOrEmpty(vectorStoreIds)) {
            for (String vectorStoreId : vectorStoreIds) {
                StepVerifier.create(client.deleteVectorStore(vectorStoreId))
                        .assertNext(vectorStoreDeletionStatus -> {
                            assertTrue(vectorStoreDeletionStatus.isDeleted());
                        })
                        .verifyComplete();
            }
        }
    }

    String createAssistant(AssistantsClient client, AssistantCreationOptions assistantCreationOptions) {
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // Create an assistant
        assertEquals(assistantCreationOptions.getName(), assistant.getName());
        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
        return assistant.getId();
    }

    String createAssistant(AssistantsAsyncClient client, AssistantCreationOptions assistantCreationOptions) {
        // create assistant test
        Assistant assistant = client.createAssistant(assistantCreationOptions).block();
        assertNotNull(assistant);
        assertEquals(assistantCreationOptions.getName(), assistant.getName());
        assertEquals(assistantCreationOptions.getDescription(), assistant.getDescription());
        assertEquals(assistantCreationOptions.getInstructions(), assistant.getInstructions());
        return assistant.getId();
    }

    void deleteAssistant(AssistantsClient client, String assistantId) {
        if (CoreUtils.isNullOrEmpty(assistantId)) {
            return;
        }
        AssistantDeletionStatus deletionStatus = client.deleteAssistant(assistantId);
        assertEquals(assistantId, deletionStatus.getId());
        assertTrue(deletionStatus.isDeleted());
    }

    void deleteAssistant(AssistantsAsyncClient client, String assistantId) {
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
