// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.arm.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.Configuration;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.policy.AuxiliaryAuthenticationPolicy;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    private TokenCredential[] tokens;

    protected AzureConfigurableImpl() {
        policies = new ArrayList<>();
        scopes = new ArrayList<>();
        retryPolicy = new RetryPolicy();
        httpLogOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.NONE);
    }

    @Override
    public T withLogOptions(HttpLogOptions httpLogOptions) {
        Objects.requireNonNull(httpLogOptions);
        this.httpLogOptions = httpLogOptions;
        return (T) this;
    }

    @Override
    public T withLogLevel(HttpLogDetailLevel logLevel) {
        Objects.requireNonNull(logLevel);
        this.httpLogOptions = httpLogOptions.setLogLevel(logLevel);
        return (T) this;
    }

    @Override
    public T withPolicy(HttpPipelinePolicy policy) {
        Objects.requireNonNull(policy);
        policies.add(policy);
        return (T) this;
    }

    @Override
    public T withAuxiliaryCredentials(TokenCredential... tokens) {
        Objects.requireNonNull(tokens);
        this.tokens = tokens;
        return (T) this;
    }

    @Override
    public T withUserAgent(String userAgent) {
        // TODO: pending
        return (T) this;
    }

    @Override
    public T withReadTimeout(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public T withConnectionTimeout(long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public T useHttpClientThreadPool(boolean useHttpClientThreadPool) {
        return null;
    }

    @Override
    public T withProxy(Proxy proxy) {
        return null;
    }

    @Override
    public T withScope(String scope) {
        Objects.requireNonNull(scope);
        this.scopes.add(scope);
        return (T) this;
    }

    @Override
    public T withHttpClient(HttpClient httpClient) {
        Objects.requireNonNull(httpClient);
        this.httpClient = httpClient;
        return (T) this;
    }

    @Override
    public T withConfiguration(Configuration configuration) {
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        return (T) this;
    }

    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile) {
        Objects.requireNonNull(credential);
        if (tokens != null) {
            policies.add(new AuxiliaryAuthenticationPolicy(profile.environment(), tokens));
        }
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, scopes(), httpLogOptions, configuration,
            retryPolicy, policies, httpClient);
    }

    private String[] scopes() {
        return scopes.isEmpty() ? null : scopes.toArray(new String[0]);
    }
}
