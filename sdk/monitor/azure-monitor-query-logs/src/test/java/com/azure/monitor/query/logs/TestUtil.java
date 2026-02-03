// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.logs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.InterceptorManager;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;

/**
 * Utility class for tests.
 */
public class TestUtil {

    /**
     * Gets a token credential for use in tests.
     * @param interceptorManager the interceptor manager
     * @return the TokenCredential
     */
    public static TokenCredential getTestTokenCredential(InterceptorManager interceptorManager) {
        if (interceptorManager.isLiveMode()) {
            Configuration config = Configuration.getGlobalConfiguration();

            String serviceConnectionId = config.get("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID");
            String clientId = config.get("AZURESUBSCRIPTION_CLIENT_ID");
            String tenantId = config.get("AZURESUBSCRIPTION_TENANT_ID");
            String systemAccessToken = config.get("SYSTEM_ACCESSTOKEN");

            return new AzurePipelinesCredentialBuilder().systemAccessToken(systemAccessToken)
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .build();
        } else if (interceptorManager.isRecordMode()) {
            return new DefaultAzureCredentialBuilder().build();
        } else {
            return new MockTokenCredential();
        }
    }

    public static void addTestProxySanitizersAndMatchers(InterceptorManager interceptorManager) {
        interceptorManager.addSanitizers(
            new TestProxySanitizer("resourceGroups\\/.*?\\/", "resourceGroups/REDACTED/", TestProxySanitizerType.URL),
            new TestProxySanitizer("Namespaces\\/.*\\/providers", "Namespaces/REDACTED/providers",
                TestProxySanitizerType.URL),
            // For resource based queries, the resource id is url encoded string (/ is encoded to %2F).
            // The regex checks for anything starting with subscriptions and ending with /query ignoring the actual resource id in playback mode
            new TestProxySanitizer("subscriptions.*?\\/query", "subscriptions/IGNORED/query",
                TestProxySanitizerType.URL),
            new TestProxySanitizer("workspaces\\/.*?\\/", "workspaces/REDACTED/", TestProxySanitizerType.URL));
        interceptorManager.addMatchers(
            new CustomMatcher().setIgnoredQueryParameters(Arrays.asList("starttime", "endtime", "api-version"))
                .setComparingBodies(false)
                .setExcludedHeaders(Arrays.asList("x-ms-content-sha256")));
    }
}
