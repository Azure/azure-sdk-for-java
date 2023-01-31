// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.util.Assert;

/**
 *
 */
public abstract class ClientRegistrationRepositoryConfigurerAdapter<O> {

    private ClientRegistrationRepositoryBuilder<O> repositoryBuilder;

    protected abstract boolean filterSignInClientRegistrations(ClientRegistration clientRegistration);

    public void configure(ClientRegistrationRepositoryBuilder<O> builder) throws Exception {

    }

    public ClientRegistrationRepositoryBuilder<O> and() {
        return getRepositoryBuilder();
    }

    public void setBuilder(ClientRegistrationRepositoryBuilder<O> repositoryBuilder) {
        this.repositoryBuilder = repositoryBuilder;
    }

    protected final ClientRegistrationRepositoryBuilder<O> getRepositoryBuilder() {
        Assert.state(this.repositoryBuilder != null, "repositoryBuilder cannot be null");
        return this.repositoryBuilder;
    }
}
