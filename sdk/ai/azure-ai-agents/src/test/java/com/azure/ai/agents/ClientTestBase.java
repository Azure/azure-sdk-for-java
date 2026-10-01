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
import com.openai.services.async.ConversationServiceAsync;
import com.openai.services.async.ResponseServiceAsync;
import com.openai.services.blocking.ConversationService;
import com.openai.services.blocking.ResponseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ClientTestBase extends TestProxyTestBase {

    private boolean sanitizersRemoved = false;

    protected AgentsClientBuilder getClientBuilder(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion, false);
    }

    protected AgentsClientBuilder getClientBuilder(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion,
        boolean allowPreview) {
        AgentsClientBuilder builder = new AgentsClientBuilder()
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);
        TestMode testMode = getTestMode();
        if (testMode != TestMode.LIVE) {
            addCustomMatchers();
            addTestRecordCustomSanitizers();
            // Disable "$..id"=AZSDK3430 and "Set-Cookie"=AZSDK2015 for both Azure and non-Azure clients from the
            // list of common sanitizers.
            if (!sanitizersRemoved) {
                List<String> sanitizersToRemove = new ArrayList<>(Arrays.asList("AZSDK3430", "AZSDK3493", "AZSDK2015"));
                sanitizersToRemove.addAll(getAdditionalTestProxySanitizersToRemove());
                interceptorManager.removeSanitizers(sanitizersToRemove.toArray(new String[0]));
                sanitizersRemoved = true;
            }
        }

        if (testMode == TestMode.PLAYBACK) {
            builder.endpoint("https://localhost:8080").credential(new MockTokenCredential());
        } else if (testMode == TestMode.RECORD) {
            builder.addPolicy(interceptorManager.getRecordPolicy())
                .endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        } else {
            builder.endpoint(Configuration.getGlobalConfiguration().get("FOUNDRY_PROJECT_ENDPOINT"))
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        String version = Configuration.getGlobalConfiguration().get("AGENTS_SERVICE_VERSION");
        AgentsServiceVersion serviceVersion
            = version == null ? agentsServiceVersion : AgentsServiceVersion.valueOf(version);
        builder.serviceVersion(serviceVersion);
        if (allowPreview) {
            builder.allowPreview(true);
        }
        return builder;
    }

    protected AgentsClient getAgentsSyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildAgentsClient();
    }

    protected AgentsClient getPreviewAgentsSyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion, true).buildAgentsClient();
    }

    protected AgentsAsyncClient getAgentsAsyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildAgentsAsyncClient();
    }

    protected ConversationService getConversationsSyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildOpenAIClient().conversations();
    }

    protected ConversationServiceAsync getConversationsAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildOpenAIAsyncClient().conversations();
    }

    protected ResponsesClient getResponsesSyncClient(HttpClient httpClient, AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildResponsesClient();
    }

    protected ResponsesAsyncClient getResponsesAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildResponsesAsyncClient();
    }

    protected ResponseService getResponseServiceSyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildOpenAIClient().responses();
    }

    protected ResponseServiceAsync getResponseServiceAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).buildOpenAIAsyncClient().responses();
    }

    protected BetaMemoryStoresClient getMemoryStoresSyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).beta().buildBetaMemoryStoresClient();
    }

    protected BetaMemoryStoresAsyncClient getMemoryStoresAsyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion).beta().buildBetaMemoryStoresAsyncClient();
    }

    protected BetaVoiceAgentsTelephonyClient getVoiceAgentsTelephonySyncClient(HttpClient httpClient,
        AgentsServiceVersion agentsServiceVersion) {
        return getClientBuilder(httpClient, agentsServiceVersion, true).beta().buildBetaVoiceAgentsTelephonyClient();
    }

    private void addTestRecordCustomSanitizers() {

        ArrayList<TestProxySanitizer> sanitizers = new ArrayList<>();
        sanitizers.add(new TestProxySanitizer("$..key", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("$..image", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
        sanitizers.add(new TestProxySanitizer("(?<=./)([^?]+)", "/REDACTED/", TestProxySanitizerType.URL));
        sanitizers.add(new TestProxySanitizer("Content-Type", "^multipart\\/form-data(; charset=[^;]+)?; boundary=.*",
            "multipart\\/form-data; boundary=BOUNDARY", TestProxySanitizerType.HEADER));

        interceptorManager.addSanitizers(sanitizers);

    }

    private void addCustomMatchers() {
        List<String> excludedHeaders
            = new ArrayList<>(Arrays.asList("Cookie", "Set-Cookie", "Accept", "X-Stainless-Arch", "X-Stainless-Lang",
                "X-Stainless-OS", "X-Stainless-OS-Version", "X-Stainless-Package-Version", "X-Stainless-Runtime",
                "X-Stainless-Runtime-Version", "X-Stainless-Kotlin-Version", "X-Stainless-Retry-Count"));
        excludedHeaders.addAll(getAdditionalTestProxyExcludedHeaders());
        interceptorManager.addMatchers(new CustomMatcher().setExcludedHeaders(excludedHeaders));
    }

    /**
     * Returns request headers that a derived test class needs the test proxy to exclude from playback matching.
     *
     * @return Additional request header names to exclude.
     */
    protected List<String> getAdditionalTestProxyExcludedHeaders() {
        return Collections.emptyList();
    }

    /**
     * Returns common sanitizer IDs that a derived test class needs to replace with test-specific sanitizers.
     *
     * @return Additional common sanitizer IDs to remove.
     */
    protected List<String> getAdditionalTestProxySanitizersToRemove() {
        return Collections.emptyList();
    }

    protected void sleep(long millis) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
