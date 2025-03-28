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
 *  This sample demonstrates how to use the Responses Input Content Image  to generate text using the Azure OpenAI service.
 */
public class AzureResponsesContentImageUrlSample {

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
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(
                new ResponsesSystemMessage(Arrays.asList(new ResponsesInputContentText("You are a helpful assistant that describes images"))),
                new ResponsesUserMessage(Arrays.asList(
                    new ResponsesInputContentText("Please describe this image"),
                    new ResponsesInputContentImage().setImageUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Microsoft_logo.svg/512px-Microsoft_logo.svg.png")))));

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);
        ResponsesAssistantMessage responseMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) responseMessage.getContent().get(0);

        // Print the response
        System.out.println("Response: " + response);
        System.out.println("Output: " + outputContent.getText());
    }
}
