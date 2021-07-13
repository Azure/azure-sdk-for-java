// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.identity;

public class SpringEnvironmentCredentialBuilder extends SpringCredentialBuilderBase<SpringEnvironmentCredentialBuilder, SpringEnvironmentCredential> {


    // TODO (xiada) idenity client options
    @Override
    public SpringEnvironmentCredential build() {
        return new SpringEnvironmentCredential(this.credentialPropertiesProvider, null);
    }

}
