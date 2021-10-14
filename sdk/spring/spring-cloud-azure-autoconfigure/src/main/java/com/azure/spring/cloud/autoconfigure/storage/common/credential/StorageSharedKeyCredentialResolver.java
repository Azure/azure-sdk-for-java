// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.storage.common.credential;

import com.azure.spring.cloud.autoconfigure.storage.common.AzureStorageProperties;
import com.azure.spring.core.credential.resolver.AzureCredentialResolver;
import com.azure.spring.core.properties.AzureProperties;
import org.springframework.util.StringUtils;

/**
 * Resolve the storage shared key credential according to the {@link AzureStorageProperties}.
 */
public class StorageSharedKeyCredentialResolver implements AzureCredentialResolver<StorageSharedKeyCredentialProvider> {

    @Override
    public StorageSharedKeyCredentialProvider resolve(AzureProperties azureProperties) {
        if (!isResolvable(azureProperties)) {
            return null;
        }

        AzureStorageProperties properties = (AzureStorageProperties) azureProperties;
        if (azureProperties == null
                || !StringUtils.hasText(properties.getAccountName())
                || !StringUtils.hasText(properties.getAccountKey())) {
            return null;
        }

        return new StorageSharedKeyCredentialProvider(properties.getAccountName(), properties.getAccountKey());
    }

    @Override
    public boolean isResolvable(AzureProperties azureProperties) {
        return azureProperties instanceof AzureStorageProperties;
    }
}
