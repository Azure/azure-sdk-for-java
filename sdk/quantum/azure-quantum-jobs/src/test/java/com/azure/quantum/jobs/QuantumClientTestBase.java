// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .credential(new AzureCliCredentialBuilder().build())
            .subscriptionId(getSubscriptionId())
            .resourceGroupName(getResourceGroup())
            .workspaceName(getWorkspaceName())
            .host(getEndpoint());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : endpoint;
    }

    String getSubscriptionId() {
        return testResourceNamer.recordValueFromConfig(subscriptionId);
    }

    String getResourceGroup() {
        return testResourceNamer.recordValueFromConfig(resourceGroup);
    }

    String getWorkspaceName() {
        return testResourceNamer.recordValueFromConfig(workspaceName);
    }

}
