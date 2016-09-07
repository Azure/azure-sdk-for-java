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
 * AzureServiceClientCredentials is the abstraction for credentials used by
 * ServiceClients accessing Azure.
 */
public interface AzureTokenCredentials extends ServiceClientCredentials {
    String getToken(String resource) throws IOException;

    String getDomain();

    /**
     * @return the environment details the credential has access to.
     */
    AzureEnvironment getEnvironment();
}
