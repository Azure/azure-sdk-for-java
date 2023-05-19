// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.core.credential.AzureKeyCredential;

/**
 * A sample demonstrating the minimal usage with default credentials 
 * where we only need to pass a prompt and the deploymentId
 */
public class ChatbotSample {
    /**
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = "{azure-open-ai-endpoint}";
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();
        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();

        String deploymentOrModelId = "text-davinci-003";
        String prompt = "Tell me 3 jokes about trains";

        Completions completions = client.getCompletions(deploymentOrModelId, prompt);

        for (Choice choice : completions.getChoices()) {
            System.out.printf("%s.%n", choice.getText());
        }
    }
}
