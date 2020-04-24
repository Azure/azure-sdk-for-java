// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link IntelliJCredential}.
 *
 * @see IntelliJCredential
 */
public class IntelliJCredentialBuilder extends CredentialBuilderBase<IntelliJCredentialBuilder> {


    public IntelliJCredentialBuilder windowsKeepPassDatabasePath(String databasePath) {
        this.identityClientOptions.setKeepPassDatabasePath(databasePath);
        return this;
    }

    /**
     * Creates a new {@link IntelliJCredential} with the current configurations.
     *
     * @return a {@link IntelliJCredential} with the current configurations.
     */
    public IntelliJCredential build() {
        return new IntelliJCredential(identityClientOptions);
    }
}
