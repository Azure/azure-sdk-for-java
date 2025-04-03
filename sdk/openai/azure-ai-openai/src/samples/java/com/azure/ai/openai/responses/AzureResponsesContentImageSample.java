// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses;


import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesSystemMessage;
import com.azure.ai.openai.responses.models.ResponsesAssistantMessage;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.ai.openai.responses.models.ResponsesInputContentImage;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesOutputContentText;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import static java.lang.System.exit;

/**
 * This sample demonstrates how to use the Responses Input Content Image  to generate text using the Azure OpenAI service.
 */
public class AzureResponsesContentImageSample {

    /**
     * Main method to run the sample.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
            .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
            .serviceVersion(AzureResponsesServiceVersion.V2025_03_01_PREVIEW)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        String base64Image = null;
        try {
            String fileName = "ms_logo.png";
            byte[] imageBytes = Files.readAllBytes(Paths.get("src/samples/java/com/azure/ai/openai/resources/" + fileName));
            base64Image= Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.out.println("Exception: " + e);
            exit(-1);
        }

        // Create a request
        CreateResponsesRequest request = new CreateResponsesRequest(CreateResponsesRequestModel.GPT_4O_MINI,
            Arrays.asList(
                new ResponsesSystemMessage(Arrays.asList(new ResponsesInputContentText("You are a helpful assistant that describes images"))),
                new ResponsesUserMessage(Arrays.asList(
                    new ResponsesInputContentText("Please describe this image"),
                    new ResponsesInputContentImage().setImageUrl("data:image/jpeg;base64," + base64Image)))));

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);
        ResponsesAssistantMessage responseMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) responseMessage.getContent().get(0);

        // Print the response
        System.out.println("Response: " + response);
        System.out.println("Output: " + outputContent.getText());
    }
}
