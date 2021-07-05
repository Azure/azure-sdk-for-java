// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.core.env.Environment;

public class SpringManagedIdentityCredentialBuilder extends SpringCredentialBuilderBase<ManagedIdentityCredential> {

    private String clientId;

    public SpringManagedIdentityCredentialBuilder(Environment environment) {
        super(environment);
        this.clientId = new AzureEnvironment(environment).getClientId();// TODO (xiada) whether to make the AzureEnvironment first citizen
        this.delegateCredentialBuilder = new ManagedIdentityCredentialBuilder();
    }


    public SpringManagedIdentityCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public ManagedIdentityCredential internalBuild() {
        return ((ManagedIdentityCredentialBuilder) this.delegateCredentialBuilder).clientId(this.clientId).build();
    }
}
