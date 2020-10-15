// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

import com.microsoft.azure.credentials.AzureTokenCredentials;

/**
 * Interface to provide the {@link AzureTokenCredentials} that will be used to call the service.
 *
 * @author Warren Zhu
 */
public interface CredentialsProvider {

    AzureTokenCredentials getCredentials();
}
