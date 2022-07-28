package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;

/**
 * Helper to access package-private methods of {@link PathProperties}, {@link FileSystemProperties} and
 * {@link PathItem}
 */
public class AccessorUtility {

    private static PathPropertiesAccessor pathPropertiesAccessor;
    private static FileSystemPropertiesAccessor fileSystemPropertiesAccessor;
    private static PathItemAccessor pathItemAccessor;

    /**
     * Accessor interface for {@link PathProperties}
     */
    public interface PathPropertiesAccessor {
        PathProperties setPathProperties(PathProperties properties, String encryptionScope);
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
        PathItem setPathItem(PathItem pathItem, String encryptionScope);
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
