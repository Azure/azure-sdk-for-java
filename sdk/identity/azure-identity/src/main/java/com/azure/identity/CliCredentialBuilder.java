// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link CliCredential}.
 *
 * @see CliCredential
 */
public class CliCredentialBuilder extends AadCredentialBuilderBase<CliCredentialBuilder> {
     /**
     * Creates a new {@link CliCredential} with the current configurations.
     *
     * @return a {@link CliCredential} with the current configurations.
     */
    public CliCredential build() {
        return new CliCredential(identityClientOptions);
    }
}
