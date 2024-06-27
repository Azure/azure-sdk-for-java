// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MixedRealityStsClientTestBase extends TestBase {
    private final String accountDomain = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_DOMAIN");
    private final String accountId = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_ID");
    private final String accountKey = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_KEY");

    // NOT REAL ACCOUNT DETAILS
    private final String playbackAccountDomain = "mixedreality.azure.com";
    private final String playbackAccountId = "68321d5a-7978-4ceb-b880-0f49751daae9";
    private final String playbackAccountKey = "NjgzMjFkNWEtNzk3OC00Y2ViLWI4ODAtMGY0OTc1MWRhYWU5";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        String accountId = getAccountId();
        String accountDomain = getAccountDomain();
        AzureKeyCredential keyCredential = getAccountKey();

        TokenCredential credential = constructAccountKeyCredential(accountId, keyCredential);
        String endpoint = AuthenticationEndpoint.constructFromDomain(accountDomain);
        String authenticationScope = AuthenticationEndpoint.constructScope(endpoint);

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new BearerTokenAuthenticationPolicy(credential, authenticationScope));

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    String getAccountDomain() {
        return interceptorManager.isPlaybackMode()
            ? this.playbackAccountDomain
            : this.accountDomain;
    }

    String getAccountId() {
        String accountIdValue = interceptorManager.isPlaybackMode()
            ? this.playbackAccountId
            : this.accountId;

        return accountIdValue;
    }

    AzureKeyCredential getAccountKey() {
        String accountKeyValue = interceptorManager.isPlaybackMode()
            ? this.playbackAccountKey
            : this.accountKey;

        return new AzureKeyCredential(accountKeyValue);
    }

    static TokenCredential constructAccountKeyCredential(String accountId, AzureKeyCredential keyCredential) {
        return new MixedRealityAccountKeyCredential(UUID.fromString(accountId), keyCredential);
    }
}
