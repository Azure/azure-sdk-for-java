// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

import java.util.Locale;
import org.springframework.util.Assert;

/**
 * Azure storage resource utility class
 */
final class AzureStorageUtils {
    /**
     * Path separator character for resource location.
     */
    public static final String PATH_DELIMITER = "/";
    private static final String STORAGE_PROTOCOL_PREFIX = "azure-%s://";


    static boolean isAzureStorageResource(String location, StorageType storageType) {
        Assert.notNull(location, "Location must not be null");
        return location.toLowerCase(Locale.ROOT).startsWith(getStorageProtocolPrefix(storageType));
    }

    static String getStorageProtocolPrefix(StorageType storageType) {
        return String.format(STORAGE_PROTOCOL_PREFIX, storageType.getType());
    }

    static String stripProtocol(String location, StorageType storageType) {
        Assert.notNull(location, "Location must not be null");
        assertIsAzureStorageLocation(location, storageType);
        return location.substring(getStorageProtocolPrefix(storageType).length());
    }

    static String getContainerName(String location, StorageType storageType) {
        assertIsAzureStorageLocation(location, storageType);
        int containerEndIndex = assertContainerValid(location, storageType);
        return location.substring(getStorageProtocolPrefix(storageType).length(),
            containerEndIndex);
    }

    static String getFilename(String location, StorageType storageType) {
        assertIsAzureStorageLocation(location, storageType);
        int containerEndIndex = assertContainerValid(location, storageType);
        if (location.endsWith(PATH_DELIMITER)) {
            return location.substring(++containerEndIndex, location.length() - 1);
        }
        return location.substring(++containerEndIndex);
    }

    static void assertIsAzureStorageLocation(String location, StorageType storageType) {
        if (!AzureStorageUtils.isAzureStorageResource(location, storageType)) {
            throw new IllegalArgumentException(
                String.format("The location '%s' is not a valid Azure storage %s location", location,
                    storageType.getType()));
        }
    }

    private static int assertContainerValid(String location, StorageType storageType) {
        String storageProtocolPrefix = AzureStorageUtils.getStorageProtocolPrefix(storageType);
        int containerEndIndex = location.indexOf(PATH_DELIMITER, storageProtocolPrefix.length());
        if (containerEndIndex == -1 || containerEndIndex == storageProtocolPrefix.length()) {
            throw new IllegalArgumentException(
                String.format("The location '%s' does not contain a valid container name", location));
        }
        return containerEndIndex;
    }
}
