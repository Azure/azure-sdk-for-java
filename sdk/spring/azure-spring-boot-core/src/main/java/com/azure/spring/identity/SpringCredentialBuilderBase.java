// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.ProxyOptions;
import com.azure.identity.CredentialBuilderBase;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 */
public abstract class SpringCredentialBuilderBase<B extends SpringCredentialBuilderBase<B, ?>, T extends TokenCredential> {

    protected CredentialPropertiesProvider credentialPropertiesProvider;

    @SuppressWarnings("rawtypes")
    protected CredentialBuilderBase delegateCredentialBuilder;

    @SuppressWarnings("unchecked")
    public B credentialPropertiesProvider(CredentialPropertiesProvider provider) {
        this.credentialPropertiesProvider = provider;
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B maxRetry(int maxRetry) {
        this.delegateCredentialBuilder.maxRetry(maxRetry);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B retryTimeout(Function<Duration, Duration> retryTimeout) {
        this.delegateCredentialBuilder.retryTimeout(retryTimeout);
        return (B) this;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public B proxyOptions(ProxyOptions proxyOptions) {
        this.delegateCredentialBuilder.proxyOptions(proxyOptions);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B httpPipeline(HttpPipeline httpPipeline) {
        this.delegateCredentialBuilder.httpPipeline(httpPipeline);
        return (B) this;
    }

    @SuppressWarnings("unchecked")
    public B httpClient(HttpClient client) {
        Objects.requireNonNull(client);
        this.delegateCredentialBuilder.httpClient(client);
        return (B) this;
    }

    public abstract T build();

}
