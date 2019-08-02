// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

/**
 * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
 *
 * @see EnvironmentCredential
 */
public class EnvironmentCredentialBuilder extends AadCredentialBuilderBase<EnvironmentCredentialBuilder> {
    /**
     * @return a {@link EnvironmentCredential} with the current configurations.
     */
    public EnvironmentCredential build() {
        return new EnvironmentCredential(identityClientOptions);
    }
}
