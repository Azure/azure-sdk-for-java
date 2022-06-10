// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;

/**
 * Interface to be implemented that enables returning of a TokenCredential for authentication with an Azure App
 * Configuration stores.
 */
public interface AppConfigurationCredentialProvider {

    /**
     * Returns a Token Credential for connecting to the given endpoint.
     * @param uri App Configuration endpoint
     * @return TokenCredential for connecting to the uri.
     */
    TokenCredential getAppConfigCredential(String uri);

}
