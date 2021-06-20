// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;

/**
 * Provides ability to generate Token Credential for connecting to Azure services.
 */
public interface TokenCredentialProvider {

    /**
     * Returns a TokenCredential that will be used for connecting to Azure App Configuration.
     * 
     * @param uri URI to App Configuration Store
     * @return TokenCredential
     */
    TokenCredential credentialForAppConfig(String uri);

    /**
     * Returns a TokenCredential that will be used for connecting to Azure Key Vault.
     * 
     * @param uri URI to Key Vault Instance
     * @return TokenCredential
     */
    TokenCredential credentialForKeyVault(String uri);

}
