// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.common.credential;

import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.implementation.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.cloud.service.implementation.storage.credential.StorageSharedKeyCredentialResolver;
import com.azure.storage.common.StorageSharedKeyCredential;

import java.util.function.Consumer;

/**
 * A descriptor describes the storage shared key authentication.
 */
public class StorageSharedKeyAuthenticationDescriptor implements AuthenticationDescriptor<StorageSharedKeyCredential> {

    private final Consumer<StorageSharedKeyCredential> consumer;

    /**
     * Create a {@link StorageSharedKeyAuthenticationDescriptor} instance with the consumer of storage shared key
     * credential.
     *
     * @param consumer the consumer for setting the storage shared key credential.
     */
    public StorageSharedKeyAuthenticationDescriptor(Consumer<StorageSharedKeyCredential> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Class<StorageSharedKeyCredential> getAzureCredentialType() {
        return StorageSharedKeyCredential.class;
    }

    @Override
    public AzureCredentialResolver<StorageSharedKeyCredential> getAzureCredentialResolver() {
        return new StorageSharedKeyCredentialResolver();
    }

    @Override
    public Consumer<StorageSharedKeyCredential> getConsumer() {
        return consumer;
    }
}
