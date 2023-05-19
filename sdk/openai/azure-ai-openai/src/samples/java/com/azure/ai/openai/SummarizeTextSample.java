// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.core.credential.AzureKeyCredential;

public class SummarizeTextSample {
    /**
     * Sample demonstrating the minimal usage with default credentials
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String endpoint = "{azure-open-ai-endpoint}";
        TokenCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        OpenAIClient client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(defaultCredential)
            .buildClient();

        String textToSummarize = """
            On December 5, 192 giant lasers at the laboratory’s National Ignition Facility blasted a small cylinder about the size of a pencil eraser that contained a frozen nubbin of hydrogen encased in diamond.
            The laser beams entered at the top and bottom of the cylinder, vaporizing it. That generated an inward onslaught of X-rays that compresses a BB-size fuel pellet of deuterium and tritium, the heavier forms of hydrogen.
            In a brief moment lasting less than 100 trillionths of a second, 2.05 megajoules of energy — roughly the equivalent of a pound of TNT — bombarded the hydrogen pellet. Out flowed a flood of neutron particles — the product of fusion — which carried about 3 megajoules of energy, a factor of 1.5 in energy gain.""";

        String summarizationPrompt = """
            Summarize the following text.
            
            Text:
            "textToSummarize"

            Summary:
            """;

        String deploymentOrModelId = "text-davinci-003";
        System.out.println("Input prompt: %d%n", summarizationPrompt);
        Completions completions = client.getCompletions(deploymentOrModelId, summarizationPrompt);

        for (Choice choice : completions.getChoices()) {
            System.out.printf("%s.%n", choice.getText());
        }
    }
}
