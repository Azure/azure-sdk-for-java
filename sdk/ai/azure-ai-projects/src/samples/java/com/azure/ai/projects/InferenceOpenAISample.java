// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class InferenceOpenAISample {

    private static InferenceClient inferenceClient
        = new AIProjectClientBuilder().endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint"))
        .credential(new DefaultAzureCredentialBuilder().build())
        .openAIConnectionName(Configuration.getGlobalConfiguration().get("OPENAI_CONNECTION_NAME", ""))
        .buildInferenceClient();

    public static void main(String[] args) {
        openAIConnectedSample();
    }

    public static void openAIConnectedSample() {
        // BEGIN: com.azure.ai.projects.InferenceOpenAISample.openAIConnectedSample

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .addUserMessage("Say this is a test")
            .model("gpt-4o-mini")
            .build();
        ChatCompletion chatCompletion = inferenceClient.getOpenAIClient().chat().completions().create(params);
        for (ChatCompletion.Choice choice : chatCompletion.choices()) {
            System.out.println("Choice index: " + choice.index());
            System.out.println("Message content: " + choice.message().content().get());
        }

        // END: com.azure.ai.projects.InferenceOpenAISample.openAIConnectedSample
    }
}
