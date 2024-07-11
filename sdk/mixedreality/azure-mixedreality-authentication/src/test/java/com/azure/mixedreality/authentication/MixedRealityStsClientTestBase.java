// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.TokenCredential;
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
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzureDeveloperCliCredentialBuilder;
import com.azure.identity.AzurePipelinesCredentialBuilder;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MixedRealityStsClientTestBase extends TestProxyTestBase {
    // NOT REAL ACCOUNT DETAILS
    private static final String PLAYBACK_ACCOUNT_DOMAIN = "mixedreality.azure.com";
    private static final String PLAYBACK_ACCOUNT_ID = "f5b3e69f-1e1b-46a5-a718-aea58a7a0f8e";

    public static final String INVALID_DUMMY_TOKEN = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b"
        + "3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZm"
        + "F1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYwNzk3ODY4MyIsIm5iZiI6IjE2MDc5Nzg2ODMiLCJleHAiOiIxNjA3OTc4OTgzIn0.";

    TokenCredential getTokenCredential() {
        switch (getTestMode()) {
            case RECORD:
                return new DefaultAzureCredentialBuilder().build();
            case LIVE:
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

                return chainedTokenCredentialBuilder.build();
            default:
                // On PLAYBACK mode
                return new MockTokenCredential();
        }
    }

    MixedRealityStsClientBuilder getClientBuilder(HttpClient httpClient) {
        MixedRealityStsClientBuilder clientBuilder = new MixedRealityStsClientBuilder()
            .accountId(Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_ID", PLAYBACK_ACCOUNT_ID))
            .accountDomain(
                Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_DOMAIN", PLAYBACK_ACCOUNT_DOMAIN))
            .credential(getTokenCredential())
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("X-MRC-CV")));

            interceptorManager.addMatchers(customMatchers);
        } else {
            clientBuilder.endpoint(Configuration.getGlobalConfiguration().get("MIXEDREALITY_SERVICE_ENDPOINT"));

            if (interceptorManager.isRecordMode()) {
                clientBuilder.addPolicy(interceptorManager.getRecordPolicy());
            }
        }

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(
                new TestProxySanitizer("$..AccessToken", null, INVALID_DUMMY_TOKEN, TestProxySanitizerType.BODY_KEY));

            interceptorManager.addSanitizers(customSanitizers);
        }

        return clientBuilder;
    }
}
