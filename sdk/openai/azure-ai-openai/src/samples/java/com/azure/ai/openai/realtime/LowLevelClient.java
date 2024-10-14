package com.azure.ai.openai.realtime;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.RealtimeAsyncClient;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;

public class LowLevelClient {
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        System.out.println("Azure OpenAI Key: " + deploymentOrModelId);
        RealtimeAsyncClient client = new OpenAIClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildRealtimeAsyncClient();

    }
}
