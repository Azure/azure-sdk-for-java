// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

import org.springframework.core.env.Environment;

public class SpringEnvironmentCredentialBuilder extends SpringCredentialBuilderBase<SpringEnvironmentCredential> {


    public SpringEnvironmentCredentialBuilder(Environment environment) {
        super(environment);
    }

    @Override
    public SpringEnvironmentCredential internalBuild() {
        return new SpringEnvironmentCredential(this.environment, this.identityClientOptions);
    }

    @Override
    public SpringEnvironmentCredential build() {
        return internalBuild();
    }
}
