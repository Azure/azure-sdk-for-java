// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.utils;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.management.AzureEnvironment;
import com.azure.management.AuthenticationPolicy;
import com.azure.management.UserAgentPolicy;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * A Http Pipeline Provider.
 */
public final class HttpPipelineProvider {

    private HttpPipelineProvider() {
    }

    /**
     * Creates http pipeline with token credential
     *
     * @param credential the token credential
     * @return the http pipeline
     */
    public static HttpPipeline buildHttpPipeline(TokenCredential credential) {
        // TODO: basic policies should be provided
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        HttpLogOptions httpLogOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC);
        policies.add(new UserAgentPolicy(httpLogOptions, null));

        List<HttpPipelinePolicy> retryPolicies = new ArrayList<>();
        retryPolicies.add(new ProviderRegistrationPolicy(credential));
        retryPolicies.add(new ResourceManagerThrottlingPolicy());
        retryPolicies.add(new AuthenticationPolicy(credential, AzureEnvironment.AZURE, null));
        retryPolicies.add(new HttpLoggingPolicy(httpLogOptions));
        return buildHttpPipeline(policies, retryPolicies, null);
    }

    /**
     * Creates http pipeline with policies and http client.
     *
     * @param policies the policies not required in retry strategy
     * @param retryPolicies the policies required in retry strategy
     * @param httpClient the http client
     * @return the http pipeline
     */
    public static HttpPipeline buildHttpPipeline(
        List<HttpPipelinePolicy> policies,
        List<HttpPipelinePolicy> retryPolicies,
        HttpClient httpClient) {
        final List<HttpPipelinePolicy> allPolicies = new ArrayList<>();
        allPolicies.addAll(policies);
        HttpPolicyProviders.addBeforeRetryPolicies(allPolicies);
        allPolicies.addAll(retryPolicies);
        HttpPolicyProviders.addAfterRetryPolicies(allPolicies);
        // TODO: Add proxy support
        return new HttpPipelineBuilder()
            .policies(allPolicies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }
}
