// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.credential.provider;

import com.azure.spring.core.credential.AzureCredentialType;

/**
 * Azure credential provider interface to unify all the credential cases.
 *
 * @param <T> The actual credential instance which azure SDKs used.
 */
public interface AzureCredentialProvider<T> {

    /**
     * Get the credential type.
     * @return the credential type.
     */
    AzureCredentialType getType();

    /**
     * Get the credential implementation.
     * @return the credential.
     */
    T getCredential();
}
