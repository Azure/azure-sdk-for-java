/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.storage;

import org.springframework.util.Assert;

/**
 * Azure storage resource utility class
 *
 * @author Warren Zhu
 */
final class AzureStorageUtils {
    private static final String STORAGE_PROTOCOL_PREFIX = "azure-%s://";

    static boolean isAzureStorageResource(String location, StorageType storageType) {
        Assert.notNull(location, "Location must not be null");
        return location.toLowerCase().startsWith(getStorageProtocolPrefix(storageType));
    }

    static String getStorageProtocolPrefix(StorageType storageType){
        return String.format(STORAGE_PROTOCOL_PREFIX, storageType.getType());
    }
}
