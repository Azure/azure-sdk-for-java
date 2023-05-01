// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.core.credential.AzureKeyCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample demonstrates the minimal use we can have of the SDK where the user, aside from providing authentication
 * details, only needs to pass the list of prompts and the deploymentId
 */
public class GetCompletionsFromPrompt {

    /**
     * The sample will return the text choices that are generated based on the prompt provided by the user
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String azureOpenaiKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildClient();

        String prompt = "Tell me 3 jokes about trains";

        Completions completions = client.getCompletions(deploymentOrModelId, prompt);

        for (Choice choice : completions.getChoices()) {
            System.out.printf("%s.%n", choice.getText());
        }
    }
}
