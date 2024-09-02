package com.azure.ai.samples;

import com.azure.ai.assistants.AssistantsClient;
import com.azure.ai.assistants.AssistantsClientBuilder;
import com.azure.ai.assistants.models.Assistant;
import com.azure.ai.assistants.models.CreateAssistantOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;

/**
 * Sample demonstrates how to create a client with Azure API Key.
 */
public class CreateAzureAIAssistantClient {
    /**
     * Runs the sample algorithm and demonstrates how to create a client with Azure API Key.
     *
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {
        String apiKey = Configuration.getGlobalConfiguration().get("AZUREAI_ENDPOINT_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZUREAI_ENDPOINT_URL");
        String deploymentOrModelId = "gpt";

        // Create an HttpLogOptions object to specify what details you want to log
        HttpLogOptions logOptions = new HttpLogOptions()
                .setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS); // This can be BODY_AND_HEADERS, HEADERS, BODY, or BASIC

        // Build the client with HTTP logging policy
        AssistantsClient client = new AssistantsClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .httpLogOptions(logOptions)  // Set the HTTP logging options
                .buildClient();

        /*AssistantsClient client = new AssistantsClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .buildClient(); */

        CreateAssistantOptions assistantCreationOptions = new CreateAssistantOptions(deploymentOrModelId)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");

        Assistant assistant = client.createAssistant(assistantCreationOptions);
        System.out.printf("Assistant ID = \"%s\" is created at %s.%n", assistant.getId(), assistant.getCreatedAt());
    }
}