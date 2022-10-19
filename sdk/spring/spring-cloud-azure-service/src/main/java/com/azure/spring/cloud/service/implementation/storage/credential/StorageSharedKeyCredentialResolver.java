// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.storage.credential;

import com.azure.spring.cloud.core.credential.AzureCredentialResolver;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.service.implementation.storage.common.StorageProperties;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.util.StringUtils;

/**
 * Resolve the storage shared key credential according to the {@link StorageProperties}.
 */
public final class StorageSharedKeyCredentialResolver implements AzureCredentialResolver<StorageSharedKeyCredential> {

    @Override
    public StorageSharedKeyCredential resolve(AzureProperties azureProperties) {
        if (!isResolvable(azureProperties)) {
            return null;
        }

        StorageProperties properties = (StorageProperties) azureProperties;
        if (!StringUtils.hasText(properties.getAccountName()) || !StringUtils.hasText(properties.getAccountKey())) {
            return null;
        }

        return new StorageSharedKeyCredential(properties.getAccountName(), properties.getAccountKey());
    }

    @Override
    public boolean isResolvable(AzureProperties azureProperties) {
        return azureProperties instanceof StorageProperties;
    }
}
