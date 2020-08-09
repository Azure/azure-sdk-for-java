// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.storage;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.WritableResource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Locale;

/**
 * Abstract implementation of {@link WritableResource} for reading and writing objects in Azure
 * StorageAccount.
 *
 * @author Warren Zhu
 */
abstract class AzureStorageResource extends AbstractResource implements WritableResource {
    private static final String PATH_DELIMITER = "/";

    private boolean isAzureStorageResource(@NonNull String location) {
        Assert.hasText(location, "Location must not be null or empty");
        return location.toLowerCase(Locale.ROOT).startsWith(getProtocolPrefix());
    }

    String getContainerName(String location) {
        assertIsAzureStorageLocation(location);
        int containerEndIndex = assertContainerValid(location);
        return location.substring(getProtocolPrefix().length(), containerEndIndex);
    }

    String getFilename(String location) {
        assertIsAzureStorageLocation(location);
        int containerEndIndex = assertContainerValid(location);

        if (location.endsWith(PATH_DELIMITER)) {
            return location.substring(++containerEndIndex, location.length() - 1);
        }

        return location.substring(++containerEndIndex);
    }

    void assertIsAzureStorageLocation(String location) {
        if (!isAzureStorageResource(location)) {
            throw new IllegalArgumentException(
                String.format("The location '%s' is not a valid Azure storage location", location));
        }
    }

    private int assertContainerValid(String location) {
        int containerEndIndex = location.indexOf(PATH_DELIMITER, getProtocolPrefix().length());
        if (containerEndIndex == -1 || containerEndIndex == getProtocolPrefix().length()) {
            throw new IllegalArgumentException(
                String.format("The location '%s' does not contain a valid container name", location));
        }

        return containerEndIndex;
    }

    private String getProtocolPrefix() {
        return AzureStorageUtils.getStorageProtocolPrefix(getStorageType());
    }

    abstract StorageType getStorageType();
}
