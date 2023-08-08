// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.credential.descriptor;

import com.azure.spring.cloud.core.credential.AzureCredentialResolver;

import java.util.function.Consumer;

/**
 * Describe the azure credential authentication by providing the type of the credential, the method of resolving the
 * credential, and the consumer the credential.
 */
public interface AuthenticationDescriptor<T> {

    /**
     * Get the azure credential type.
     * @return the azure credential type.
     */
    Class<T> getAzureCredentialType();

    /**
     * Get the azure credential resolver ro resolver the builder.
     * @return the credential resolver.
     */
    AzureCredentialResolver<T> getAzureCredentialResolver();

    /**
     * Get the consumer function for credential.
     * @return the cunsumer function.
     */
    Consumer<T> getConsumer();
}
