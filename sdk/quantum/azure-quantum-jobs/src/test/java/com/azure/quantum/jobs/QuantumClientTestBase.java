// Copyright (c) Microsoft Corporation. All rights reserved.
package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;

public class QuantumClientTestBase extends TestBase {
    private final String endpoint = Configuration.getGlobalConfiguration().get("QUANTUM_ENDPOINT");
    private final String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
    private final String resourceGroup = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_RESOURCE_GROUP);
    private final String workspaceName = Configuration.getGlobalConfiguration().get("QUANTUM_WORKSPACE");

    QuantumClientBuilder getClientBuilder(HttpClient httpClient) {

        QuantumClientBuilder builder = new QuantumClientBuilder();

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        return builder
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .credential(new AzureCliCredentialBuilder().build())
            .subscriptionId(subscriptionId)
            .resourceGroupName(resourceGroup)
            .workspaceName(workspaceName)
            .host(getEndpoint());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
    }

}
