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
    private final String SANITIZED = "Sanitized";
    private final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private final String RESOURCE_GROUP = "myresourcegroup";
    private final String WORKSPACE = "myworkspace";
    private final String LOCATION = "eastus";


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

            customSanitizers.add(new TestProxySanitizer(getSubscriptionId(), SUBSCRIPTION_ID, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(getResourceGroup(), RESOURCE_GROUP, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(getWorkspaceName(), WORKSPACE, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(getLocation(), LOCATION, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(getSubscriptionId(), SUBSCRIPTION_ID, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer(getResourceGroup(), RESOURCE_GROUP, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer(getWorkspaceName(), WORKSPACE, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer(getLocation(), LOCATION, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer("(?:\\?(sv|sig|se|srt|ss|sp)=)(?<secret>.*)", SANITIZED, TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
            customSanitizers.add(new TestProxySanitizer("$..sasUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..containerUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..inputDataUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(new TestProxySanitizer("$..outputDataUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
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
        return Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_LOCATION", LOCATION);
    }

    String getSubscriptionId() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID, SUBSCRIPTION_ID);
    }

    String getResourceGroup() {
        return Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_RESOURCE_GROUP, RESOURCE_GROUP);
    }

    String getWorkspaceName() {
        return Configuration.getGlobalConfiguration().get("AZURE_QUANTUM_WORKSPACE_NAME", WORKSPACE);
    }
}
