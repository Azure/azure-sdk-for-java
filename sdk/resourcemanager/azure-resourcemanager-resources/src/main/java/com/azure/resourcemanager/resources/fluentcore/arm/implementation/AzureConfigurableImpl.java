// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.policy.AuxiliaryAuthenticationPolicy;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The implementation for {@link AzureConfigurable} and the base class for
 * configurable implementations.
 *
 * @param <T> the type of the configurable interface
 */
public class AzureConfigurableImpl<T extends AzureConfigurable<T>>
    implements AzureConfigurable<T> {
    private HttpClient httpClient;
    private HttpLogOptions httpLogOptions;
    private List<HttpPipelinePolicy> policies;
    private List<String> scopes;
    private RetryPolicy retryPolicy;
    private Configuration configuration;
    private List<TokenCredential> tokens;

    protected AzureConfigurableImpl() {
        policies = new ArrayList<>();
        scopes = new ArrayList<>();
        tokens = new ArrayList<>();
        retryPolicy = new RetryPolicy("Retry-After", ChronoUnit.SECONDS);
        httpLogOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withLogOptions(HttpLogOptions httpLogOptions) {
        Objects.requireNonNull(httpLogOptions);
        this.httpLogOptions = httpLogOptions;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withLogLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        this.httpLogOptions = httpLogOptions.setLogLevel(logLevel);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withAuxiliaryCredential(TokenCredential token) {
        Objects.requireNonNull(token);
        this.tokens.add(token);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withAuxiliaryCredentials(List<TokenCredential> tokens) {
        Objects.requireNonNull(tokens);
        this.tokens.addAll(tokens);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withRetryPolicy(RetryPolicy retryPolicy) {
        Objects.requireNonNull(retryPolicy);
        this.retryPolicy = retryPolicy;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withScope(String scope) {
        Objects.requireNonNull(scope);
        this.scopes.add(scope);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withScopes(List<String> scopes) {
        Objects.requireNonNull(scopes);
        this.scopes.addAll(scopes);
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withHttpClient(HttpClient httpClient) {
        Objects.requireNonNull(httpClient);
        this.httpClient = httpClient;
        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T withConfiguration(Configuration configuration) {
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        return (T) this;
    }

    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile) {
        Objects.requireNonNull(credential);
        if (!tokens.isEmpty()) {
            policies.add(
                new AuxiliaryAuthenticationPolicy(profile.environment(), tokens.toArray(new TokenCredential[0])));
        }
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, scopes(), httpLogOptions, configuration,
            retryPolicy, policies, httpClient);
    }

    private String[] scopes() {
        return scopes.isEmpty() ? null : scopes.toArray(new String[0]);
    }
}
