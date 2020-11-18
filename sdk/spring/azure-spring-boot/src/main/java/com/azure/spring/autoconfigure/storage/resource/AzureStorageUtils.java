// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.storage.resource;

import org.springframework.util.Assert;

import java.util.Locale;

/**
 * Azure storage resource utility class
 *
 * @author Warren Zhu
 */
final class AzureStorageUtils {
    private static final String STORAGE_PROTOCOL_PREFIX = "azure-%s://";

    static boolean isAzureStorageResource(String location, StorageType storageType) {
        Assert.notNull(location, "Location must not be null");
        return location.toLowerCase(Locale.ROOT).startsWith(getStorageProtocolPrefix(storageType));
    }

    static String getStorageProtocolPrefix(StorageType storageType) {
        return String.format(STORAGE_PROTOCOL_PREFIX, storageType.getType());
    }
}
