// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link ClientSecretCredential}.
 *
 * @see ClientSecretCredential
 */
public class AuthFileCredentialBuilder extends AadCredentialBuilderBase<AuthFileCredentialBuilder> {
    private String filepath;

    /**
     * Sets the client secret for the authentication.
     * @param clientSecret the secret value of the AAD application.
     * @return the ClientSecretCredentialBuilder itself
     */
    public AuthFileCredentialBuilder filePath(String filepath) {
        this.filepath = filepath;
        return this;
    }

    /**
     * Creates a new {@link ClientCertificateCredential} with the current configurations.
     *
     * @return a {@link ClientSecretCredentialBuilder} with the current configurations.
     */
    public AuthFileCredential build() {
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("filepath", filepath);
            }});
        return new AuthFileCredential(filepath, identityClientOptions);
    }
}
