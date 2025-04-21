package com.azure.ai.openai.responses;

import com.azure.ai.openai.responses.models.CreateResponsesRequest;
import com.azure.ai.openai.responses.models.CreateResponsesRequestModel;
import com.azure.ai.openai.responses.models.ResponsesAssistantMessage;
import com.azure.ai.openai.responses.models.ResponsesInputContentText;
import com.azure.ai.openai.responses.models.ResponsesOutputContentText;
import com.azure.ai.openai.responses.models.ResponsesResponse;
import com.azure.ai.openai.responses.models.ResponsesSystemMessage;
import com.azure.ai.openai.responses.models.ResponsesUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

import java.util.Arrays;

public class ChatbotResponsesSample {
    public static void main(String[] args) throws InterruptedException {
        // Create a client
        ResponsesClient client = new ResponsesClientBuilder()
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT"))
                .credential(new AzureKeyCredential(Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY")))
                .serviceVersion(AzureResponsesServiceVersion.V2025_03_01_PREVIEW)
                .buildClient();

        String prompt = "Tell me 3 jokes about trains";

        // Create a request
        CreateResponsesRequest request = createJokesRequest(prompt);

        // Send the request and get the response
        ResponsesResponse response = client.createResponse(request);
        ResponsesAssistantMessage responseMessage = (ResponsesAssistantMessage) response.getOutput().get(0);
        ResponsesOutputContentText outputContent = (ResponsesOutputContentText) responseMessage.getContent().get(0);

        // Print the response
        System.out.println("Response: " + response);
        System.out.println("Jokes: " + outputContent.getText());
    }

    public static CreateResponsesRequest createJokesRequest(String prompt) {
        return new CreateResponsesRequest(
                CreateResponsesRequestModel.GPT_4O,
                Arrays.asList(
                        new ResponsesSystemMessage(Arrays.asList(
                                new ResponsesInputContentText("You are a humorous assistant who tells jokes")
                        )),
                        new ResponsesUserMessage(Arrays.asList(
                                new ResponsesInputContentText("Please tell me some jokes about trains"),
                                new ResponsesInputContentText(prompt)
                        ))
                )
        );
    }
}
