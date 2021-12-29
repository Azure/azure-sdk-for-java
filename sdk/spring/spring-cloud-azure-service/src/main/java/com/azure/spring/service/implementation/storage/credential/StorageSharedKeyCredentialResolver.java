// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.implementation.storage.credential;

import com.azure.spring.core.credential.AzureCredentialResolver;
import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.service.storage.common.StorageProperties;
import com.azure.spring.service.storage.common.credential.StorageSharedKeyCredentialProvider;
import org.springframework.util.StringUtils;

/**
 * Resolve the storage shared key credential according to the {@link StorageProperties}.
 */
public final class StorageSharedKeyCredentialResolver implements AzureCredentialResolver<StorageSharedKeyCredentialProvider> {

    @Override
    public StorageSharedKeyCredentialProvider resolve(AzureProperties azureProperties) {
        if (!isResolvable(azureProperties)) {
            return null;
        }

        StorageProperties properties = (StorageProperties) azureProperties;
        if (!StringUtils.hasText(properties.getAccountName()) || !StringUtils.hasText(properties.getAccountKey())) {
            return null;
        }

        return new StorageSharedKeyCredentialProvider(properties.getAccountName(), properties.getAccountKey());
    }

    @Override
    public boolean isResolvable(AzureProperties azureProperties) {
        return azureProperties instanceof StorageProperties;
    }
}
