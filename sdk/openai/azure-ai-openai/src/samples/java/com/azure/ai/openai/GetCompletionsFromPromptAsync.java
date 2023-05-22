// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.core.credential.AzureKeyCredential;

import java.util.concurrent.TimeUnit;

/**
 * Sample demonstrates the minimal async use we can have of the SDK where the user, aside from providing authentication
 * details, only needs to pass the list of prompts and the deploymentId
 */
public class GetCompletionsFromPromptAsync {
    /**
     * The sample will return the text choices that are generated based on the prompt provided by the user
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenAIKey = "{azure-open-ai-key}";
        String endpoint = "{azure-open-ai-endpoint}";
        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenAIKey))
            .buildAsyncClient();

        String prompt = "Tell me 3 facts about pineapples";

        client.getCompletions(deploymentOrModelId, prompt)
            .subscribe(
                completions -> {
                    for (Choice choice : completions.getChoices()) {
                        System.out.printf("%s.%n", choice.getText());
                    }
                },
                error -> System.err.println("There was an error getting completions." + error),
                () -> System.out.println("Completed called getCompletions.")
            );

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        TimeUnit.SECONDS.sleep(10);
    }
}

