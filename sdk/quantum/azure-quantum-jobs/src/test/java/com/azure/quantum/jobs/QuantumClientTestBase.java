// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class QuantumClientTestBase extends TestProxyTestBase {
    QuantumClientBuilder getClientBuilder(HttpClient httpClient) {

        System.out.println(String.format("Subscription id: %s", getSubscriptionId()));
        System.out.println(String.format("Resource group: %s", getResourceGroup()));
        System.out.println(String.format("Workspace: %s", getWorkspaceName()));
        System.out.println(String.format("Location: %s", getLocation()));
        System.out.println(String.format("Endpoint: %s", getEndpoint()));
        System.out.println(String.format("Test mode: %s", getTestMode()));

        QuantumClientBuilder builder = new QuantumClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("Authorization")));
            interceptorManager.addMatchers(customMatchers);
            builder.httpClient(interceptorManager.getPlaybackClient());
        } else {
            builder.httpClient(httpClient)
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();

            customSanitizers.add(new TestProxySanitizer("(?:\\?(sv|sig|se|srt|ss|sp)=)(?<secret>.*)", "REDACTED", TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
            customSanitizers.add(new TestProxySanitizer("$..sasUri", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..containerUri", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..inputDataUri", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..outputDataUri", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        }

        return builder
            .subscriptionId(getSubscriptionId())
            .resourceGroupName(getResourceGroup())
            .workspaceName(getWorkspaceName())
            .host(getEndpoint());
    }

    String getEndpoint() {
        return String.format("https://%s.quantum.azure.com", getLocation());
    }

    String getLocation() {
        return Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_LOCATION", "eastus");
    }

    String getSubscriptionId() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID, "faa080af-c1d8-40ad-9cce-e1a450ca5b57");
    }

    String getResourceGroup() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_RESOURCE_GROUP, "vigera-group");
    }

    String getWorkspaceName() {
        return Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_NAME", "vigeraQuantumWorkspace");
    }
}
