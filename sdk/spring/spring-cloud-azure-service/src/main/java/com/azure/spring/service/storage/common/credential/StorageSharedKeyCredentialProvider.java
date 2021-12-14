// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common.credential;

import com.azure.spring.core.credential.AzureCredentialType;
import com.azure.spring.core.credential.provider.AzureCredentialProvider;
import com.azure.storage.common.StorageSharedKeyCredential;

/**
 * Provide the azure storage shared key credential.
 */
public class StorageSharedKeyCredentialProvider implements AzureCredentialProvider<StorageSharedKeyCredential> {

    private final String accountName;
    private final String accountKey;

    /**
     * Create a {@link StorageSharedKeyCredentialProvider} instance with the account name and account key.
     * @param accountName the storage account name.
     * @param accountKey the storage account key.
     */
    public StorageSharedKeyCredentialProvider(String accountName, String accountKey) {
        this.accountName = accountName;
        this.accountKey = accountKey;
    }

    @Override
    public AzureCredentialType getType() {
        return StorageSharedKeyAuthenticationDescriptor.STORAGE_SHARED_KEY;
    }

    @Override
    public StorageSharedKeyCredential getCredential() {
        return new StorageSharedKeyCredential(accountName, accountKey);
    }
}
