// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FileSearchToolDefinition;
import com.azure.ai.openai.assistants.models.FileSearchToolDefinitionDetails;
import com.azure.core.http.HttpClient;
import com.azure.core.util.BinaryData;

import java.util.Arrays;
import java.util.function.BiConsumer;

public abstract class FileSearchTestBase extends AssistantsClientTestBase {
    private static final String JAVA_SDK_TESTS_ASSISTANTS_TXT = "java_sdk_tests_assistants.txt";
    private static final String GPT_4_TURBO = "gpt-4-turbo";

    public abstract void basicFileSearch(HttpClient httpClient, AssistantsServiceVersion serviceVersion);
    public abstract void fileSearchWithMaxNumberResult(HttpClient httpClient, AssistantsServiceVersion serviceVersion);

    void fileSearchRunner(BiConsumer<FileDetails, AssistantCreationOptions> testRunner) {
        FileDetails fileDetails = new FileDetails(
            BinaryData.fromFile(openResourceFile(JAVA_SDK_TESTS_ASSISTANTS_TXT)),
            JAVA_SDK_TESTS_ASSISTANTS_TXT);

        AssistantCreationOptions assistantOptions = new AssistantCreationOptions(GPT_4_TURBO)
            .setName("Java SDK Retrieval Sample")
            .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
            .setTools(Arrays.asList(new FileSearchToolDefinition()));

        testRunner.accept(fileDetails, assistantOptions);
    }

    void fileSearchWithMaxNumberResultRunner(BiConsumer<FileDetails, AssistantCreationOptions> testRunner) {
        FileDetails fileDetails = new FileDetails(
            BinaryData.fromFile(openResourceFile(JAVA_SDK_TESTS_ASSISTANTS_TXT)),
            JAVA_SDK_TESTS_ASSISTANTS_TXT);

        AssistantCreationOptions assistantOptions = new AssistantCreationOptions(GPT_4_1106_PREVIEW)
            .setName("Java SDK Retrieval Sample")
            .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
            .setTools(Arrays.asList(
                new FileSearchToolDefinition().setFileSearch(
                    new FileSearchToolDefinitionDetails().setMaxNumResults(0)
                )));

        testRunner.accept(fileDetails, assistantOptions);
    }
}
