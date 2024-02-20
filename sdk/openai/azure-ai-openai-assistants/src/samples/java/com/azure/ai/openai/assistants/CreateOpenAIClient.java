// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.Configuration;

/**
 * Sample demonstrates how to create a client with public Non-Azure API Key.
 */
public class CreateOpenAIClient {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with public Non-Azure API Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";

        AssistantsClient client = new AssistantsClientBuilder()
                .credential(new KeyCredential(apiKey))
                .buildClient();

        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");

        Assistant assistant = client.createAssistant(assistantCreationOptions);
        System.out.printf("Assistant ID = \"%s\" is created at %s.%n", assistant.getId(), assistant.getCreatedAt());
    }
}
