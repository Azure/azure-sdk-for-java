/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.AzureAuthenticated;
import com.microsoft.azure.CloudException;
import com.microsoft.rest.credentials.ServiceClientCredentials;

import java.io.IOException;

public final class Azure {
    public static AzureAuthenticated authenticate(ServiceClientCredentials credentials) throws IOException, CloudException {
        return new AzureAuthenticatedImpl(credentials);
    }
}
