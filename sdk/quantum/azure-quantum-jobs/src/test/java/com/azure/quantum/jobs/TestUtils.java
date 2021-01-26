package com.azure.quantum.jobs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.identity.AzureCliCredentialBuilder;

public class TestUtils {

    public static QuantumClientBuilder getBuilder() {
        return new QuantumClientBuilder()
            .subscriptionId("677fc922-91d0-4bf6-9b06-4274d319a0fa")
            .resourceGroupName("sdk-review-rg")
            .workspaceName("workspace-ms")
            .host(String.format("https://%s.quantum.azure.com", "westus"))
            .credential(new AzureCliCredentialBuilder().build());
    }
}
