// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.identity.implementation.util.ValidationUtil;

import java.util.HashMap;

/**
 * Fluent credential builder for instantiating a {@link AuthFileCredential}.
 *
 * @see AuthFileCredential
 */
public class AuthFileCredentialBuilder extends AadCredentialBuilderBase<AuthFileCredentialBuilder> {
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
        ValidationUtil.validate(getClass().getSimpleName(), new HashMap<String, Object>() {{
                put("filepath", filepath);
            }});
        return new AuthFileCredential(filepath, identityClientOptions);
    }
}
