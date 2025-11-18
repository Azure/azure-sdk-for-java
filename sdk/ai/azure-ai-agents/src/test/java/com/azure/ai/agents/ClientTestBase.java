// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientTestBase extends TestProxyTestBase {

    private boolean sanitizersRemoved = false;

    protected AgentsClientBuilder getClientBuilder(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        TestMode testMode = getTestMode();
        if (testMode != TestMode.LIVE) {
            addCustomMatchers();
            addTestRecordCustomSanitizers();
            // Disable "$..id"=AZSDK3430, "Set-Cookie"=AZSDK2015 for both azure and non-azure clients from the list of common sanitizers.
            if (!sanitizersRemoved) {
                interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493", "AZSDK2015");
                sanitizersRemoved = true;
            }
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new MockTokenCredential());
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .endpoint(Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.endpoint(Configuration.getGlobalConfiguration().get("AZURE_AGENTS_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        String version = Configuration.getGlobalConfiguration().get("AGENTS_SERVICE_VERSION");
        AgentsServiceVersion serviceVersion
            = version == null ? agentsServiceVersion : AgentsServiceVersion.valueOf(version);
        builder.serviceVersion(serviceVersion);
        return builder;
    }

    protected AgentsClient getAgentsSyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildAgentsClient();
    }

    protected AgentsAsyncClient getAgentsAsyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildAgentsAsyncClient();
    }

    protected ConversationsClient getConversationsSyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildConversationsClient();
    }

    protected ConversationsAsyncClient getConversationsAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildConversationsAsyncClient();
    }

    protected ResponsesClient getResponsesSyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildResponsesClient();
    }

    protected ResponsesAsyncClient getResponsesAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildResponsesAsyncClient();
    }

    private void addTestRecordCustomSanitizers() {

        ArrayList<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..endpoint", "https://.+?/api/projects/.+?/", "https://REDACTED/",
            TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer("Content-Type",
            "(^multipart\\/form-data; boundary=[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{2})",
            "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER));

        interceptorManager.addSanitizers(sanitizers);

    }

    private void addCustomMatchers() {
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(Arrays.asList("Cookie", "Set-Cookie")));
    }

}
