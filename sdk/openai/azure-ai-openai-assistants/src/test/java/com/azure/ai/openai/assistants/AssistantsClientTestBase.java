// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public abstract class AssistantsClientTestBase extends TestProxyTestBase {
    AssistantsClientBuilder getAssistantsClientBuilder(HttpClient httpClient, OpenAIServiceVersion serviceVersion) {
        AssistantsClientBuilder builder = new AssistantsClientBuilder()
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
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
            builder.addPolicy(interceptorManager.getRecordPolicy())
                    .credential(new KeyCredential(Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY")));
        } else {
            builder.credential(new KeyCredential(Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY")));
        }
        return builder;
    }

    public static String GPT_4_1106_PREVIEW = "gpt-4-1106-preview";

    @Test
    public abstract void createAndThenDeleteAssistant(HttpClient httpClient, OpenAIServiceVersion serviceVersion);


    void createAssistantsRunner(Consumer<AssistantCreationOptions> testRunner) {
        testRunner.accept(new AssistantCreationOptions(GPT_4_1106_PREVIEW)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less."));
    }
}
