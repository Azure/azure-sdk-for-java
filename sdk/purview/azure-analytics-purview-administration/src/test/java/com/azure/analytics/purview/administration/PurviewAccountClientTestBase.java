// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurviewAccountClientTestBase extends TestProxyTestBase {
    private static final String ZERO_UUID = "00000000-0000-0000-0000-000000000000";
    private static final String[] REMOVE_SANITIZER_ID = {"AZSDK3430", "AZSDK3493"};

    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("ENDPOINT");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    AccountsClientBuilder purviewAccountClientBuilderSetUp() {
        AccountsClientBuilder builder = new AccountsClientBuilder();
        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        List<TestProxySanitizer> customSanitizer = new ArrayList<>();
        if (!interceptorManager.isLiveMode()) {
            // sanitize response body keys
            customSanitizer.add(new TestProxySanitizer("$..clientId", null, "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..principalId", null, "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..tenantId", null, "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..createdBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..friendlyName", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..managedResourceGroupName", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..lastModifiedBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..catalog", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..scan", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..guardian", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..resourceGroup", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..storageAccount", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..id", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("name", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            // sanitize subscription id
            customSanitizer.add(new TestProxySanitizer("(?<=/subscriptions/)([^/?]+)", ZERO_UUID,
                TestProxySanitizerType.BODY_REGEX));

            interceptorManager.addSanitizers(customSanitizer);
            // Remove `id` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }

        builder.endpoint(getEndpoint());
        return Objects.requireNonNull(builder);
    }

    CollectionsClientBuilder purviewCollectionClientBuilderSetUp() {
        CollectionsClientBuilder builder = new CollectionsClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        builder.endpoint(getEndpoint());

        List<TestProxySanitizer> customSanitizer = new ArrayList<>();
        if (!interceptorManager.isLiveMode()) {
            // sanitize response body keys
            customSanitizer.add(new TestProxySanitizer("$..createdBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..friendlyName", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..lastModifiedBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..name", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizer);
            // Remove `id` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }

        return Objects.requireNonNull(builder);
    }

    MetadataPolicyClientBuilder purviewMetadataClientBuilderSetUp() {
        MetadataPolicyClientBuilder builder = new MetadataPolicyClientBuilder();
        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        builder.endpoint(getEndpoint());

        List<TestProxySanitizer> customSanitizer = new ArrayList<>();
        if (!interceptorManager.isLiveMode()) {
            // sanitize response body keys
            customSanitizer.add(new TestProxySanitizer("$..attributeValueIncludedIn", null, "00000000-0000-0000-0000-000000000000", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..referenceName", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..parentCollectionName", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..attributeValueIncludes", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..fromRule", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..attributeValueIncludes", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..name", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$...attributeRules..id", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizer);
            // Remove `id` sanitizer from the list of common sanitizers.
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
        }

        return Objects.requireNonNull(builder);
    }
}
