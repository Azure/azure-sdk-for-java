// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.quantum.jobs;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuantumClientTestBase extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(QuantumClientTestBase.class);

    private final String SANITIZED = "Sanitized";
    private final String SUBSCRIPTION_ID = "00000000-0000-0000-0000-000000000000";
    private final String RESOURCE_GROUP = "myresourcegroup";
    private final String WORKSPACE = "myworkspace";
    private final String LOCATION = "eastus";

    QuantumClientBuilder getClientBuilder(HttpClient httpClient) {

        LOGGER.log(LogLevel.VERBOSE, () -> "Subscription id: " + getSubscriptionId());
        LOGGER.log(LogLevel.VERBOSE, () -> "Resource group: " + getResourceGroup());
        LOGGER.log(LogLevel.VERBOSE, () -> "Workspace: " + getWorkspaceName());
        LOGGER.log(LogLevel.VERBOSE, () -> "Location: " + getLocation());
        LOGGER.log(LogLevel.VERBOSE, () -> "Endpoint: " + getEndpoint());
        LOGGER.log(LogLevel.VERBOSE, () -> "Test mode: " + getTestMode());

        QuantumClientBuilder builder = new QuantumClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Arrays.asList("Authorization", "Cookie")));
            interceptorManager.addMatchers(customMatchers);

            builder.httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder.httpClient(httpClient);

            if (interceptorManager.isRecordMode()) {
                builder.credential(new DefaultAzureCredentialBuilder().build())
                    .addPolicy(interceptorManager.getRecordPolicy());
            } else if (interceptorManager.isLiveMode()) {
                Configuration config = Configuration.getGlobalConfiguration();

                ChainedTokenCredentialBuilder chainedTokenCredentialBuilder = new ChainedTokenCredentialBuilder()
                    .addLast(new EnvironmentCredentialBuilder().build())
                    .addLast(new AzureCliCredentialBuilder().build())
                    .addLast(new AzureDeveloperCliCredentialBuilder().build())
                    .addLast(new AzurePowerShellCredentialBuilder().build());

                String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
                String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
                String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
                String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

                if (!CoreUtils.isNullOrEmpty(serviceConnectionId)
                    && !CoreUtils.isNullOrEmpty(clientId)
                    && !CoreUtils.isNullOrEmpty(tenantId)
                    && !CoreUtils.isNullOrEmpty(systemAccessToken)) {

                    chainedTokenCredentialBuilder.addLast(new AzurePipelinesCredentialBuilder()
                        .systemAccessToken(systemAccessToken)
                        .clientId(clientId)
                        .tenantId(tenantId)
                        .serviceConnectionId(serviceConnectionId)
                        .build());
                }

                builder.credential(chainedTokenCredentialBuilder.build());
            }
        }

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();

            customSanitizers.add(
                new TestProxySanitizer(getSubscriptionId(), SUBSCRIPTION_ID, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(
                new TestProxySanitizer(getResourceGroup(), RESOURCE_GROUP, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(
                new TestProxySanitizer(getWorkspaceName(), WORKSPACE, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(new TestProxySanitizer(getLocation(), LOCATION, TestProxySanitizerType.BODY_REGEX));
            customSanitizers.add(
                new TestProxySanitizer(getSubscriptionId(), SUBSCRIPTION_ID, TestProxySanitizerType.URL));
            customSanitizers.add(
                new TestProxySanitizer(getResourceGroup(), RESOURCE_GROUP, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer(getWorkspaceName(), WORKSPACE, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer(getLocation(), LOCATION, TestProxySanitizerType.URL));
            customSanitizers.add(new TestProxySanitizer("(?:\\\\?(sv|sig|se|srt|ss|sp)=)(?<secret>.*)", SANITIZED,
                TestProxySanitizerType.BODY_REGEX).setGroupForReplace("secret"));
            customSanitizers.add(new TestProxySanitizer("$..sasUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(
                new TestProxySanitizer("$..containerUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(
                new TestProxySanitizer("$..inputDataUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            customSanitizers.add(
                new TestProxySanitizer("$..outputDataUri", null, SANITIZED, TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        }

        return builder.subscriptionId(getSubscriptionId())
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
