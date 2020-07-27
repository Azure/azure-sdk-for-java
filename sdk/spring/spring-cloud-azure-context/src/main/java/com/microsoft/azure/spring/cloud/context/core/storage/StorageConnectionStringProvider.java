/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.storage;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.management.storage.StorageAccount;

public class StorageConnectionStringProvider {

    public static String getConnectionString(StorageAccount storageAccount, AzureEnvironment environment,
            boolean isSecureTransfer) {
        return buildConnectionString(storageAccount, environment, isSecureTransfer);
    }

    public static String getConnectionString(StorageAccount storageAccount, AzureEnvironment environment) {
        return getConnectionString(storageAccount, environment, true);
    }

    public static String getConnectionString(String storageAccount, String accessKey, AzureEnvironment environment) {
        return getConnectionString(storageAccount, accessKey, environment, true);
    }

    public static String getConnectionString(String storageAccount, String accessKey, AzureEnvironment environment,
            boolean isSecureTransfer) {
        return StorageConnectionStringBuilder.build(storageAccount, accessKey, environment, isSecureTransfer);
    }

    private static String buildConnectionString(StorageAccount storageAccount, AzureEnvironment environment,
            boolean isSecureTransfer) {
        return storageAccount.getKeys().stream().findFirst().map(key -> StorageConnectionStringBuilder
                .build(storageAccount.name(), key.value(), environment, isSecureTransfer))
                             .orElseThrow(() -> new RuntimeException("Storage account key is empty."));
    }
}
