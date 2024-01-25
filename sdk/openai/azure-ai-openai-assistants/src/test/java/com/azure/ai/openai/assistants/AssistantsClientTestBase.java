// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.ThreadInitializationMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AssistantsClientTestBase extends TestProxyTestBase {

    private static final String RESOURCE_FOLDER_ROOT = "src/test/resources";

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();
    }

    AssistantsAsyncClient getAssistantsAsyncClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient,
                false), serviceVersion)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildAsyncClient();
    }

    AssistantsClient getAssistantsClient(HttpClient httpClient) {
        return getAssistantsClientBuilder(buildAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .buildClient();
    }

    AssistantsClient getAssistantsClient(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        return getAzureAssistantsClientBuilder(buildAssertingClient(
                        interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient, true),
                serviceVersion)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less."));
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

    void uploadFileRunner(Runnable testRunner) {
        testRunner.run();
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

    protected static Path openResourceFile(String fileName) {
        return Paths.get("src", "test", "resources", fileName);
    }
}
