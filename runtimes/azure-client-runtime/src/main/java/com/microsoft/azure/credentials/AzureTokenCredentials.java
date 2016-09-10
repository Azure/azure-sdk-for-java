/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.credentials;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

/**
 * AzureTokenCredentials represents a credentials object with access to Azure
 * Resource management.
 */
public interface AzureTokenCredentials extends ServiceClientCredentials {
    /**
     * Override this method to provide the mechanism to get a token.
     *
     * @param resource the resource the access token is for
     * @return the token to access the resource
     * @throws IOException exceptions from IO
     */
    String getToken(String resource) throws IOException;

    /**
     * Override this method to provide the domain or tenant ID the token is valid in.
     *
     * @return the domain or tenant ID string
     */
    String getDomain();

    /**
     * @return the environment details the credential has access to.
     */
    AzureEnvironment getEnvironment();
}
