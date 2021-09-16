// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common.credential;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.descriptor.AuthenticationDescriptor;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;

import java.util.function.Consumer;

/**
 * A descriptor describes the storage shared key authentication.
 */
public class StorageSharedKeyAuthenticationDescriptor implements AuthenticationDescriptor<StorageSharedKeyCredentialProvider> {

    static final AzureCredentialType STORAGE_SHARED_KEY = new AzureCredentialType("storage_shared_key");

    private final Consumer<StorageSharedKeyCredentialProvider> consumer;

    public StorageSharedKeyAuthenticationDescriptor(Consumer<StorageSharedKeyCredentialProvider> consumer) {
        this.consumer = consumer;
    }

    @Override
    public AzureCredentialType azureCredentialType() {
        return STORAGE_SHARED_KEY;
    }

    @Override
    public AzureCredentialResolver<StorageSharedKeyCredentialProvider> azureCredentialResolver() {
        return new StorageSharedKeyCredentialResolver();
    }

    @Override
    public Consumer<StorageSharedKeyCredentialProvider> consumer() {
        return consumer;
    }
}
