// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AadCredentialBuilderBase;
import com.azure.identity.CredentialBuilderBase;
import com.azure.identity.implementation.IdentityClientOptions;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 *
 */
public abstract class SpringCredentialBuilderBase<T extends TokenCredential> {

    protected Environment environment;
    protected CredentialBuilderBase delegateCredentialBuilder;
    protected IdentityClientOptions identityClientOptions;

    SpringCredentialBuilderBase(Environment environment) {
        Assert.notNull(environment, "To build a spring credential the environment must be set.");
        this.environment = environment;
        this.identityClientOptions = new IdentityClientOptions();
    }

    public SpringCredentialBuilderBase identityClientOptions(IdentityClientOptions identityClientOptions) {
        this.identityClientOptions = identityClientOptions;
        return this;
    }

    @SuppressWarnings("rawtypes")
    public T build() {
        this.configureIdentityOptions();
        return internalBuild();
    }

    @SuppressWarnings("rawtypes")
    protected void configureIdentityOptions() {
        this.delegateCredentialBuilder
            .httpClient(this.identityClientOptions.getHttpClient())
            .httpPipeline(this.identityClientOptions.getHttpPipeline())
            .maxRetry(this.identityClientOptions.getMaxRetry())
            .retryTimeout(this.identityClientOptions.getRetryTimeout());
    }

    protected abstract T internalBuild();

}
