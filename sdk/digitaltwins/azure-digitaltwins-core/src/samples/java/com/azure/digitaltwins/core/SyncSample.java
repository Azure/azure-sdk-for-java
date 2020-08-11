package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.digitaltwins.core.models.DigitalTwinsGetByIdResponse;
import com.azure.identity.ClientSecretCredentialBuilder;

public class SyncSample
{
    public static void main(String[] args)
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

        DigitalTwinsClient client = new DigitalTwinsClientBuilder()
            .tokenCredential(tokenCredential)
            .endpoint(endpoint)
            .buildClient();

        DigitalTwinsGetByIdResponse syncResponse = client.getDigitalTwin(digitalTwinId);
        Object digitalTwin = syncResponse.getValue();
        System.out.println(digitalTwin);
    }
}
