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
import com.azure.core.management.profile.AzureProfile;
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
    private HttpPipeline httpPipeline;

    /**
     *  Configures the http pipeline.
     * (Internal use only)
     *
     * @param httpPipeline the http pipeline
     * @param azureConfigurable the azure configurable instance
     * @param <T> the type of azure configurable
     * @return the azure configurable instance
     */
    public static <T extends AzureConfigurable<?>> T configureHttpPipeline(HttpPipeline httpPipeline,
                                                                           T azureConfigurable) {
        ((AzureConfigurableImpl) azureConfigurable).withHttpPipeline(httpPipeline);
        return azureConfigurable;
    }

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

    /**
     *  Sets the http pipeline.
     * (Internal use only)
     *
     * @param httpPipeline the http pipeline
     */
    public void withHttpPipeline(HttpPipeline httpPipeline) {
        Objects.requireNonNull(httpPipeline);
        this.httpPipeline = httpPipeline;
    }

    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile) {
        // Check if this is internal build to make sure all managers could share same http pipeline in each module.
        if (this.httpPipeline != null) {
            return httpPipeline;
        }
        Objects.requireNonNull(credential);
        if (!tokens.isEmpty()) {
            policies.add(
                new AuxiliaryAuthenticationPolicy(profile.getEnvironment(), tokens.toArray(new TokenCredential[0])));
        }
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, scopes(), httpLogOptions, configuration,
            retryPolicy, policies, httpClient);
    }

    private String[] scopes() {
        return scopes.isEmpty() ? null : scopes.toArray(new String[0]);
    }
}
