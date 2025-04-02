package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.List;

public class SummarizeTextResponsesSample {
    public static void main(String[] args) throws InterruptedException {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .serviceVersion(AzureResponsesServiceVersion.V2025_03_01_PREVIEW)
                .buildClient();

        String summarizationPrompt = getSummarizationPrompt();

        // Create a request
        CreateResponsesRequest request = createSummarizationRequest(summarizationPrompt);

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);
        ResponsesAssistantMessage responseMessage = (ResponsesAssistantMessage) response.getOutput().getFirst();
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) responseMessage.getContent().getFirst();

        // Print the response
        System.out.println("Response: " + response);
        System.out.println("Summary: " + outputContent.getText());
    }

    public static String getSummarizationPrompt() {
        String textToSummarize = "On December 5, 192 giant lasers at the laboratory’s National Ignition Facility blasted a small cylinder about the size of a pencil eraser that contained a frozen nubbin of hydrogen encased in diamond. "
                + "The laser beams entered at the top and bottom of the cylinder, vaporizing it. That generated an inward onslaught of X-rays that compresses a BB-size fuel pellet of deuterium and tritium, the heavier forms of hydrogen. "
                + "In a brief moment lasting less than 100 trillionths of a second, 2.05 megajoules of energy — roughly the equivalent of a pound of TNT — bombarded the hydrogen pellet. Out flowed a flood of neutron particles — the product of fusion — which carried about 3 megajoules of energy, a factor of 1.5 in energy gain.";

        return "Summarize the following text.%n" + "Text:%n" + textToSummarize + "%n Summary:%n";
    }

    public static CreateResponsesRequest createSummarizationRequest(String summarizationPrompt) {
        return new CreateResponsesRequest(
                CreateResponsesRequestModel.GPT_4O,
                Arrays.asList(
                        new ResponsesSystemMessage(List.of(
                                new ResponsesInputContentText("You are a helpful assistant that summarizes texts")
                        )),
                        new ResponsesUserMessage(Arrays.asList(
                                new ResponsesInputContentText("Please summarize the following text"),
                                new ResponsesInputContentText(summarizationPrompt)
                        ))
                )
        );
    }
}
