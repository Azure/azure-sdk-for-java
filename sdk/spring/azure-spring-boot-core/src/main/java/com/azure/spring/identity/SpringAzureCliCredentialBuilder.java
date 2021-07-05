// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import org.springframework.core.env.Environment;

public class SpringAzureCliCredentialBuilder extends SpringCredentialBuilderBase<AzureCliCredential> {

    public SpringAzureCliCredentialBuilder(Environment environment) {
        super(environment);
        this.delegateCredentialBuilder = new AzureCliCredentialBuilder();
    }

    @Override
    public AzureCliCredential internalBuild() {
        return ((AzureCliCredentialBuilder) this.delegateCredentialBuilder).build();
    }
}
