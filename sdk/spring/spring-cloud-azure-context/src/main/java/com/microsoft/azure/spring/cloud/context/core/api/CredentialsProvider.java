/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

import com.microsoft.azure.credentials.AzureTokenCredentials;

/**
 * Interface to provide the {@link AzureTokenCredentials} that
 * will be used to call the service.
 *
 * @author Warren Zhu
 */
public interface CredentialsProvider {
    AzureTokenCredentials getCredentials();
}
