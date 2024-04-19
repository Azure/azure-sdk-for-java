// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * A sample demonstrating a prompt to summarize text using async method
 */
public class SummarizeTextSample {
    /**
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) throws InterruptedException {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");

        OpenAIAsyncClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(azureOpenaiKey))
            .buildAsyncClient();

        String textToSummarize = "On December 5, 192 giant lasers at the laboratory’s National Ignition Facility blasted a small cylinder about the size of a pencil eraser that contained a frozen nubbin of hydrogen encased in diamond. "
            + "The laser beams entered at the top and bottom of the cylinder, vaporizing it. That generated an inward onslaught of X-rays that compresses a BB-size fuel pellet of deuterium and tritium, the heavier forms of hydrogen. "
            + "In a brief moment lasting less than 100 trillionths of a second, 2.05 megajoules of energy — roughly the equivalent of a pound of TNT — bombarded the hydrogen pellet. Out flowed a flood of neutron particles — the product of fusion — which carried about 3 megajoules of energy, a factor of 1.5 in energy gain.";

        String summarizationPrompt = "Summarize the following text.%n" + "Text:%n" + textToSummarize + "%n Summary:%n";

        String deploymentOrModelId = "{azure-open-ai-deployment-model-id}";
        System.out.printf("Input prompt: %s%n", summarizationPrompt);

        client.getCompletions(deploymentOrModelId, summarizationPrompt)
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
