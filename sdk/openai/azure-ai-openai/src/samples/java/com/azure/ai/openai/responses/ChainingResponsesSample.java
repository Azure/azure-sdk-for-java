// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import java.util.Arrays;

/**
 *  This sample demonstrates how to chain responses together by passing the response.id from the previous response to the previous_response_id parameter to generate text using the Azure OpenAI service.
 */
public class ChainingResponsesSample {

    /**
     * Main method to run the sample.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {

        // Azure OpenAI Responses API is enabled only for api-version 2025-03-01-preview and later
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
            .serviceVersion(AzureResponsesServiceVersion.V2025_03_01_PREVIEW)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        // First response
        CreateResponsesRequest firstRequest = new CreateResponsesRequest(
            CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(
                    new ResponsesInputContentText(    "Define and explain the concept of catastrophic forgetting?")))));
        ResponsesResponse firstResponse = client.createResponse(firstRequest);

        // Second response
        CreateResponsesRequest secondRequest = new CreateResponsesRequest(
            CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(
                new ResponsesUserMessage(Arrays.asList(
                    new ResponsesInputContentText(    "Explain this at a level that could be understood by a college freshman")))));
        secondRequest.setPreviousResponseId(firstResponse.getId());
        ResponsesResponse secondResponse = client.createResponse(secondRequest);

        // Print the second response
        ResponsesAssistantMessage assistantMessage = (ResponsesAssistantMessage) secondResponse.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) assistantMessage.getContent().get(0);
        System.out.println(outputContent.getText());

    }
}
