// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.WritableResource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.net.URLConnection;
import java.util.Locale;

/**
 * Abstract implementation of {@link WritableResource} for reading and writing objects in Azure StorageAccount.
 *
 */
abstract class AzureStorageResource extends AbstractResource implements WritableResource {
    private static final String PATH_DELIMITER = "/";

    private boolean isAzureStorageResource(@NonNull String location) {
        Assert.hasText(location, "Location must not be null or empty");
        return location.toLowerCase(Locale.ROOT).startsWith(getProtocolPrefix());
    }

    /**
     * Get the storage container(fileShare) name from the given location.
     *
     * @param location the location represents the resource
     * @return the container name of current location
     */
    String getContainerName(String location) {
        assertIsAzureStorageLocation(location);
        int containerEndIndex = assertContainerValid(location);
        return location.substring(getProtocolPrefix().length(), containerEndIndex);
    }

    /**
     * Gets the content type.
     *
     * @param location the location
     * @return the content type
     */
    String getContentType(String location) {
        String objectName = getFilename(location);
        if (StringUtils.hasText(objectName)) {
            return URLConnection.guessContentTypeFromName(objectName);
        }
        return null;
    }

    /**
     * Get the file name from the given location.
     *
     * @param location the location represents the resource
     * @return the file name of current location
     */
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
