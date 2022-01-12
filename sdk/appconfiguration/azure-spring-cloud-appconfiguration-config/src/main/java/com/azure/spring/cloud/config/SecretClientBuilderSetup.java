// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import com.azure.security.keyvault.secrets.SecretClientBuilder;

/**
 * Creates Custom SecretClientBuilder for connecting to Key Vault.
 */
public interface SecretClientBuilderSetup {

    /**
     * Updates the SecretClientBuilder for connecting to the given uri.
     * @param builder SecretClientBuilder
     * @param uri String
     */
    void setup(SecretClientBuilder builder, String uri);

}
