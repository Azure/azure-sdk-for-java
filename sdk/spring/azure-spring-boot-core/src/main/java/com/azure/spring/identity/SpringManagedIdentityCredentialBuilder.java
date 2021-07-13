// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.util.StringUtils;

public class SpringManagedIdentityCredentialBuilder extends SpringCredentialBuilderBase<SpringManagedIdentityCredentialBuilder, ManagedIdentityCredential> {

    private String clientId;

    public SpringManagedIdentityCredentialBuilder() {
        this.delegateCredentialBuilder = new ManagedIdentityCredentialBuilder();
    }

    public SpringManagedIdentityCredentialBuilder clientId(String clientId) {
        if (StringUtils.hasText(clientId)) {
            this.clientId = clientId;
        }
        return this;
    }

    @Override
    public ManagedIdentityCredential build() {
        return ((ManagedIdentityCredentialBuilder) this.delegateCredentialBuilder).clientId(this.clientId).build();
    }
}
