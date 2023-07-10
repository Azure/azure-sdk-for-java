// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredentialBuilder;

import java.nio.file.Files;

public class QuantumClientTestBase extends TestProxyTestBase {
//    private final String endpoint = Configuration.getGlobalConfiguration().get("QUANTUM_ENDPOINT");
    private final String endpoint = "https://westus2.quantum.azure.com";
//    private final String subscriptionId = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
    private final String subscriptionId = "faa080af-c1d8-40ad-9cce-e1a450ca5b57";
    private final String resourceGroup = "vigera-group";
    private final String workspaceName = "vigeraQuantumWorkspace";

    QuantumClientBuilder getClientBuilder(HttpClient httpClient) {

        QuantumClientBuilder builder = new QuantumClientBuilder();

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(httpClient)
                .credential(new AzureCliCredentialBuilder().build());
        }

        return builder.subscriptionId(getSubscriptionId())
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
