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
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.CustomMatcher;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.models.TestProxyRequestMatcher;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MixedRealityStsClientTestBase extends TestProxyTestBase {
    public static final String INVALID_DUMMY_TOKEN = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b"
        + "3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZm"
        + "F1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYwNzk3ODY4MyIsIm5iZiI6IjE2MDc5Nzg2ODMiLCJleHAiOiIxNjA3OTc4OTgzIn0.";
    private final String accountDomain = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_DOMAIN");
    private final String accountId = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_ID");
    private final String accountKey = Configuration.getGlobalConfiguration().get("MIXEDREALITY_ACCOUNT_KEY");

    // NOT REAL ACCOUNT DETAILS
    private final String playbackAccountDomain = "mixedreality.azure.com";
    private final String playbackAccountId = "f5b3e69f-1e1b-46a5-a718-aea58a7a0f8e";
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

        if (interceptorManager.isRecordMode() || interceptorManager.isPlaybackMode()) {
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();
            customSanitizers.add(new TestProxySanitizer("$..AccessToken", null, INVALID_DUMMY_TOKEN,
                TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        }

        if (interceptorManager.isRecordMode()) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        if (interceptorManager.isPlaybackMode()) {
            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();
            customMatchers.add(new BodilessMatcher());
            customMatchers.add(new CustomMatcher().setExcludedHeaders(Collections.singletonList("X-MRC-CV")));
            interceptorManager.addMatchers(customMatchers);
        }



        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
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
