// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;

/**
 * A wrapper builder for the AzurePowerShellCredentialBuilder, could be removed when the EnvironmentCredential could
 * accept a Configuration as a constructor parameter.
 */
public class SpringAzurePowerShellCredentialBuilder extends SpringCredentialBuilderBase<SpringAzurePowerShellCredentialBuilder, AzurePowerShellCredential> {

    public SpringAzurePowerShellCredentialBuilder() {
        this.delegateCredentialBuilder = new AzurePowerShellCredentialBuilder();
    }

    @Override
    public AzurePowerShellCredential build() {
        return ((AzurePowerShellCredentialBuilder) this.delegateCredentialBuilder).build();
    }
}
