// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.CredentialBuilderBase;
import com.azure.identity.implementation.IdentityClientOptions;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 *
 */
public abstract class SpringCredentialBuilderBase<T extends TokenCredential> {

    protected Environment environment;
    @SuppressWarnings("rawtypes")
    protected CredentialBuilderBase delegateCredentialBuilder;
    protected IdentityClientOptions identityClientOptions;

    public SpringCredentialBuilderBase(Environment environment) {
        Assert.notNull(environment, "To build a spring credential the environment must be set.");
        this.environment = environment;
        this.identityClientOptions = new IdentityClientOptions();
    }

    @SuppressWarnings("rawtypes")
    public SpringCredentialBuilderBase identityClientOptions(IdentityClientOptions identityClientOptions) {
        this.identityClientOptions = identityClientOptions;
        return this;
    }

    public T build() {
        this.configureIdentityOptions();
        return internalBuild();
    }

    @SuppressWarnings("unchecked")
    protected void configureIdentityOptions() {
        this.delegateCredentialBuilder
            .httpPipeline(this.identityClientOptions.getHttpPipeline())
            .maxRetry(this.identityClientOptions.getMaxRetry())
            .retryTimeout(this.identityClientOptions.getRetryTimeout());

        if (this.identityClientOptions.getHttpClient() != null) {
            this.delegateCredentialBuilder.httpClient(this.identityClientOptions.getHttpClient());
        }
    }

    protected abstract T internalBuild();

}
