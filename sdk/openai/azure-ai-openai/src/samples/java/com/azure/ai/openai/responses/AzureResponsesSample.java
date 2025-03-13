// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesAssistantMessage;
import com.azure.ai.openai.responses.models.ResponsesContent;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesOutputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates how to use the Responses client to generate text using the Azure OpenAI service.
 */
public class AzureResponsesSample {

    /**
     * Main method to run the sample.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .serviceVersion(AzureResponsesServiceVersion.V2024_12_01_PREVIEW)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        // Create a request
        List<ResponsesContent> messages = Arrays.asList(new ResponsesInputContentText("Hello, world!"));
        CreateResponsesRequest request = new CreateResponsesRequest(
            CreateResponsesRequestModel.fromString("computer-use-preview"),
                Arrays.asList(new ResponsesUserMessage(messages))
        );

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);
        ResponsesAssistantMessage responseMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) responseMessage.getContent().get(0);

        // Print the response
        System.out.println("Response: " + response);
        System.out.println("Output: " + outputContent.getText());
    }
}
