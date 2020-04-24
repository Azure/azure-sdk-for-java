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
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.management.AuthenticationPolicy;
import com.azure.management.UserAgentPolicy;
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;

import java.util.ArrayList;
import java.util.List;

public class HttpPipelineProvider {

    private HttpPipelineProvider() {
    }

    public static HttpPipeline buildHttpPipeline(TokenCredential credential) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new ProviderRegistrationPolicy(credential));
        policies.add(new ResourceManagerThrottlingPolicy());
        return buildHttpPipeline(policies, credential, null, new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE), null, new RetryPolicy(), buildHttpClient(), AzureEnvironment.AZURE);
    }

    public static HttpPipeline buildHttpPipeline(List<HttpPipelinePolicy> policies, TokenCredential credential, List<String> scopes, HttpLogOptions httpLogOptions, Configuration configuration, RetryPolicy retryPolicy, HttpClient httpClient, AzureEnvironment environment) {
        final List<HttpPipelinePolicy> allPolicies = new ArrayList<>();
        allPolicies.add(new UserAgentPolicy(httpLogOptions, configuration));
        if (credential != null) {
            allPolicies.add(new AuthenticationPolicy(credential, environment, buildScopes(scopes)));
        }
        allPolicies.add(new HttpLoggingPolicy(httpLogOptions));
        HttpPolicyProviders.addBeforeRetryPolicies(allPolicies);
        allPolicies.add(retryPolicy);
        allPolicies.addAll(policies);
        HttpPolicyProviders.addAfterRetryPolicies(allPolicies);
        // TODO: Add proxy support
        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();
    }

    private static String[] buildScopes(List<String> scopes) {
        return scopes == null ||scopes.isEmpty() ? null : scopes.toArray(new String[0]);
    }

    private static HttpClient buildHttpClient() {
        // TODO: set default creation for http client
        return null;
    }
}
