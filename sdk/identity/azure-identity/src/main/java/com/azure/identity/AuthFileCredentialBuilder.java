// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

/**
 * Fluent credential builder for instantiating a {@link AuthFileCredential}.
 *
 * @see AuthFileCredential
 */
public class AuthFileCredentialBuilder extends CredentialBuilderBase<AuthFileCredentialBuilder> {
    private String filepath;

    /**
     * Sets the file path for the authentication.
     * @param filepath The path to the SDK Auth file.
     * @return the AuthFileCredentialBuilder itself
     */
    public AuthFileCredentialBuilder filePath(String filepath) {
        this.filepath = filepath;
        return this;
    }

    /**
     * Creates a new {@link AuthFileCredential} with the current configurations.
     *
     * @return a {@link AuthFileCredential} with the current configurations.
     */
    public AuthFileCredential build() {
        return new AuthFileCredential(filepath, identityClientOptions);
    }
}
