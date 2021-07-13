// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;

/**
 * A wrapper builder for the SpringAzureCliCredentialBuilder, could be removed when the EnvironmentCredential could
 * accept a Configuration as a constructor parameter.
 */
public class SpringAzureCliCredentialBuilder extends SpringCredentialBuilderBase<SpringAzureCliCredentialBuilder, AzureCliCredential> {

    public SpringAzureCliCredentialBuilder() {
        this.delegateCredentialBuilder = new AzureCliCredentialBuilder();
    }

    @Override
    public AzureCliCredential build() {
        return ((AzureCliCredentialBuilder) this.delegateCredentialBuilder).build();
    }
}
