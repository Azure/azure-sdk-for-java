package com.azure.ai.openai.realtime;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.RealtimeAsyncClient;
import com.azure.ai.openai.RealtimeClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Configuration;
import org.apache.tools.ant.taskdefs.Sleep;

public class LowLevelClient {
    public static void main(String[] args) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        System.out.println("Azure OpenAI Key: " + deploymentOrModelId);
        RealtimeAsyncClient client = new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .credential(new AzureKeyCredential(azureOpenaiKey))
                .buildAsyncClient();

        client.start().block();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            client.close();
        } catch (Exception e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }
}
