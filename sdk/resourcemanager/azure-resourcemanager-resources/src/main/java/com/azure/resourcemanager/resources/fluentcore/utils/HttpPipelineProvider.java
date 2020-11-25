// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.resources.fluentcore.policy.AuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.ReturnRequestIdHeaderPolicy;
import com.azure.resourcemanager.resources.fluentcore.policy.UserAgentPolicy;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides common patterns on building {@link HttpPipeline}.
 */
public final class HttpPipelineProvider {

    private HttpPipelineProvider() {
    }

    /**
     * Creates http pipeline with token credential and profile
     *
     * @param credential the token credential
     * @param profile the profile
     * @return the http pipeline
     */
    public static HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile) {
        return buildHttpPipeline(credential, profile, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE),
            null, new RetryPolicy("Retry-After", ChronoUnit.SECONDS), null, null);
    }

    /**
     * Creates http pipeline with token credential and profile
     *
     * @param credential the token credential
     * @param profile the profile
     * @param scopes the credential scopes
     * @param httpLogOptions the http log options
     * @param configuration the configuration value
     * @param retryPolicy the retry policy
     * @param additionalPolicies the additional policies
     * @param httpClient the http client
     * @return the http pipeline
     */
    public static HttpPipeline buildHttpPipeline(
        TokenCredential credential, AzureProfile profile, String[] scopes, HttpLogOptions httpLogOptions,
        Configuration configuration, RetryPolicy retryPolicy, List<HttpPipelinePolicy> additionalPolicies,
        HttpClient httpClient) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy(httpLogOptions, configuration));
        policies.add(new RequestIdPolicy());
        policies.add(new ReturnRequestIdHeaderPolicy(ReturnRequestIdHeaderPolicy.Option.COPY_CLIENT_REQUEST_ID));
        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        policies.add(retryPolicy);
        policies.add(new AddDatePolicy());
        if (credential != null) {
            policies.add(new AuthenticationPolicy(credential, profile.getEnvironment(), scopes));
        }
        policies.add(new ProviderRegistrationPolicy());
        if (additionalPolicies != null && !additionalPolicies.isEmpty()) {
            policies.addAll(additionalPolicies);
        }
        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(httpLogOptions));
        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }
}
