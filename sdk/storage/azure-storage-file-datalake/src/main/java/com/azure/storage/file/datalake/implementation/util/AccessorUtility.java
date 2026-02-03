// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PathSystemProperties;

import java.time.OffsetDateTime;

/**
 * Helper to access package-private methods of {@link PathProperties}, {@link FileSystemProperties} and
 * {@link PathItem}
 */
public final class AccessorUtility {

    private static PathPropertiesAccessor pathPropertiesAccessor;
    private static FileSystemPropertiesAccessor fileSystemPropertiesAccessor;
    private static PathItemAccessor pathItemAccessor;
    private static PathSystemPropertiesAccessor pathSystemPropertiesAccessor;

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
        FileSystemProperties setFileSystemProperties(FileSystemProperties properties, String encryptionScope,
            Boolean isOverride);
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

    /**
     * Type defining the methods to set the non-public properties of a {@link PathSystemProperties} instance.
     */
    public interface PathSystemPropertiesAccessor {
        /**
         * Creates a new instance of {@link PathSystemProperties}.
         *
         * @param creationTime The creation time of the path.
         * @param lastModified The last modified time of the path.
         * @param eTag The eTag of the path.
         * @param fileSize Size of the resource.
         * @param isDirectory A flag indicating if the path is a directory.
         * @param isServerEncrypted A flag indicating if the path's content is encrypted on the server.
         * @param encryptionKeySha256 The SHA256 of the customer provided encryption key used to encrypt the path on the server.
         * @param expiresOn The time when the path is going to expire.
         * @param encryptionScope The encryption scope of the path.
         * @param encryptionContext The additional context for encryption operations on the path.
         * @param owner The owner of the path.
         * @param group The group of the path.
         * @param permissions The {@link PathPermissions}
         *
         * @return A new instance of {@link PathSystemProperties}.
         */
        PathSystemProperties create(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag,
            Long fileSize, Boolean isDirectory, Boolean isServerEncrypted, String encryptionKeySha256,
            OffsetDateTime expiresOn, String encryptionScope, String encryptionContext, String owner, String group,
            PathPermissions permissions);
    }

    /**
     * Sets the {@link PathSystemPropertiesAccessor} instance.
     *
     * @param accessor the accessor instance.
     */
    public static void setPathSystemPropertiesAccessor(final PathSystemPropertiesAccessor accessor) {
        pathSystemPropertiesAccessor = accessor;
    }

    /**
     * Returns the accessor for {@link PathSystemProperties}.
     *
     * @param creationTime The creation time of the path.
     * @param lastModified The last modified time of the path.
     * @param eTag The eTag of the path.
     * @param fileSize Size of the resource.
     * @param isDirectory A flag indicating if the path is a directory.
     * @param isServerEncrypted A flag indicating if the path's content is encrypted on the server.
     * @param encryptionKeySha256 The SHA256 of the customer provided encryption key used to encrypt the path on the server.
     * @param expiresOn The time when the path is going to expire.
     * @param encryptionScope The encryption scope of the path.
     * @param encryptionContext The additional context for encryption operations on the path.
     * @param owner The owner of the path.
     * @param group The group of the path.
     * @param permissions The {@link PathPermissions}
     *
     * @return the {@link PathSystemPropertiesAccessor}.
     */
    public static PathSystemProperties create(OffsetDateTime creationTime, OffsetDateTime lastModified, String eTag,
        Long fileSize, Boolean isDirectory, Boolean isServerEncrypted, String encryptionKeySha256,
        OffsetDateTime expiresOn, String encryptionScope, String encryptionContext, String owner, String group,
        PathPermissions permissions) {
        if (pathSystemPropertiesAccessor == null) {
            new PathSystemProperties();
        }
        assert pathSystemPropertiesAccessor != null;
        return pathSystemPropertiesAccessor.create(creationTime, lastModified, eTag, fileSize, isDirectory,
            isServerEncrypted, encryptionKeySha256, expiresOn, encryptionScope, encryptionContext, owner, group,
            permissions);
    }

}
