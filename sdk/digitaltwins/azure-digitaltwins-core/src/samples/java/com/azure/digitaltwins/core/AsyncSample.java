package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.digitaltwins.core.models.DigitalTwinsGetByIdResponse;
import com.azure.identity.ClientSecretCredentialBuilder;
import reactor.core.publisher.Mono;

public class AsyncSample
{
    public static void main(String[] args) throws InterruptedException
    {
        String tenantId = System.getenv("TENANT_ID");
        String clientId = System.getenv("CLIENT_ID");
        String clientSecret = System.getenv("CLIENT_SECRET");
        String endpoint = System.getenv("DIGITAL_TWINS_ENDPOINT");
        String digitalTwinId = System.getenv("DIGITAL_TWIN_ID");

        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .tenantId(tenantId)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build();

        DigitalTwinsAsyncClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .buildAsyncClient();

        Mono<DigitalTwinsGetByIdResponse> asyncResponse = client.getDigitalTwin(digitalTwinId);

        // once the async thread completes, the digital twin will be printed, or an error will be printed
        asyncResponse.subscribe(
            result -> System.out.println(result.getValue()),
            error -> System.err.println("Failed to get digital twin with Id " + digitalTwinId + " due to error message " + error.getMessage()));

        //Wait for async thread to finish before ending this thread.
        Thread.sleep(3000);
    }
}
