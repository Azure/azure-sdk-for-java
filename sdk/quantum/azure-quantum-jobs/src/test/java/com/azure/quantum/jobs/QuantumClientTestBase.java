// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class QuantumClientTestBase extends TestProxyTestBase {
    private final String location = Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_LOCATION");
    private final String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
    private final String resourceGroup = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_RESOURCE_GROUP);
    private final String workspaceName = Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_NAME");

    QuantumClientBuilder getClientBuilder(HttpClient httpClient) {

        System.out.println(String.format("Subscription id: %s", getSubscriptionId()));
        System.out.println(String.format("Resource group: %s", getResourceGroup()));
        System.out.println(String.format("Workspace: %s", getWorkspaceName()));
        System.out.println(String.format("Location: %s", location));
        System.out.println(String.format("Endpoint: %s", getEndpoint()));
        System.out.println(String.format("Test mode: %s", getTestMode()));

        QuantumClientBuilder builder = new QuantumClientBuilder();

        if (getTestMode() == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(httpClient)
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        return builder
            .subscriptionId(getSubscriptionId())
            .resourceGroupName(getResourceGroup())
            .workspaceName(getWorkspaceName())
            .host(getEndpoint());
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : String.format("https://%s.quantum.azure.com", location);
    }

    String getSubscriptionId() {
        return interceptorManager.isPlaybackMode()
            ? testResourceNamer.recordValueFromConfig(subscriptionId)
            : subscriptionId;
    }

    String getResourceGroup() {
        return interceptorManager.isPlaybackMode()
            ? testResourceNamer.recordValueFromConfig(resourceGroup)
            : resourceGroup;
    }

    String getWorkspaceName() {
        return interceptorManager.isPlaybackMode()
            ? testResourceNamer.recordValueFromConfig(workspaceName)
            : workspaceName;
    }

}
