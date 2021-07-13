// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;

public class SpringAzurePowerShellCredentialBuilder extends SpringCredentialBuilderBase<SpringAzurePowerShellCredentialBuilder, AzurePowerShellCredential> {

    public SpringAzurePowerShellCredentialBuilder() {
        this.delegateCredentialBuilder = new AzurePowerShellCredentialBuilder();
    }

    @Override
    public AzurePowerShellCredential build() {
        return ((AzurePowerShellCredentialBuilder) this.delegateCredentialBuilder).build();
    }
}
