// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.AzurePowerShellCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import org.springframework.core.env.Environment;

public class SpringAzurePowerShellCredentialBuilder extends SpringCredentialBuilderBase<AzurePowerShellCredential> {

    public SpringAzurePowerShellCredentialBuilder(Environment environment) {
        super(environment);
        this.delegateCredentialBuilder = new AzurePowerShellCredentialBuilder();
    }

    @Override
    public AzurePowerShellCredential internalBuild() {
        return ((AzurePowerShellCredentialBuilder) this.delegateCredentialBuilder).build();
    }
}
