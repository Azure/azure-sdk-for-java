// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * Helper to access package-private methods of {@link PathProperties}, {@link FileSystemProperties} and
 * {@link PathItem}
 */
public final class AccessorUtility {

    private static PathPropertiesAccessor pathPropertiesAccessor;
    private static FileSystemPropertiesAccessor fileSystemPropertiesAccessor;
    private static PathItemAccessor pathItemAccessor;

    private AccessorUtility() {

    }

    /**
     * Accessor interface for {@link PathProperties}
     */
    public interface PathPropertiesAccessor {
        PathProperties setPathProperties(PathProperties properties, String encryptionScope, String encryptionContext,
            String owner, String group, String permissions, String acl);
    }

    /**
     * Sets the {@link PathPropertiesAccessor} instance.
     *
     * @param accessor the accessor instance.
     */
    public static void setPathPropertiesAccessor(PathPropertiesAccessor accessor) {
        pathPropertiesAccessor = accessor;
    }

    /**
     * Returns the accessor for {@link PathProperties}.
     *
     * @return the {@link PathPropertiesAccessor}.
     */
    public static PathPropertiesAccessor getPathPropertiesAccessor() {
        return pathPropertiesAccessor;
    }

    /**
     * Accessor interface for {@link FileSystemProperties}
     */
    public interface FileSystemPropertiesAccessor {
        FileSystemProperties setFileSystemProperties(FileSystemProperties properties, String encryptionScope, Boolean isOverride);
    }

    /**
     * Sets the {@link FileSystemPropertiesAccessor} instance.
     *
     * @param accessor the accessor instance.
     */
    public static void setFileSystemPropertiesAccessor(FileSystemPropertiesAccessor accessor) {
        fileSystemPropertiesAccessor = accessor;
    }

    /**
     * Returns the accessor for {@link FileSystemProperties}.
     *
     * @return the {@link FileSystemPropertiesAccessor}.
     */
    public static FileSystemPropertiesAccessor getFileSystemPropertiesAccessor() {
        return fileSystemPropertiesAccessor;
    }

    /**
     * Accessor interface for {@link PathItem}
     */
    public interface PathItemAccessor {
        PathItem setPathItemProperties(PathItem pathItem, String encryptionScope, String encryptionContext);
    }

    /**
     * Sets the {@link PathItemAccessor} instance.
     *
     * @param accessor the accessor instance.
     */
    public static void setPathItemAccessor(PathItemAccessor accessor) {
        pathItemAccessor = accessor;
    }

    /**
     * Returns the accessor for {@link PathItem}.
     *
     * @return the {@link PathItemAccessor}.
     */
    public static PathItemAccessor getPathItemAccessor() {
        return pathItemAccessor;
    }


}
