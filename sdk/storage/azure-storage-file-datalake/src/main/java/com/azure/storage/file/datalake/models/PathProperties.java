// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.util.CoreUtils;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.datalake.implementation.util.AccessorUtility;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * This class contains the response information returned from the service when getting path properties.
 */
public class PathProperties {
    private final OffsetDateTime creationTime;
    private final OffsetDateTime lastModified;
    private final String eTag;
    private final long fileSize;
    private final String contentType;
    private final byte[] contentMd5;
    private final String contentEncoding;
    private final String contentDisposition;
    private final String contentLanguage;
    private final String cacheControl;
    private final LeaseStatusType leaseStatus;
    private final LeaseStateType leaseState;
    private final LeaseDurationType leaseDuration;
    private final String copyId;
    private final CopyStatusType copyStatus;
    private final String copySource;
    private final String copyProgress;
    private final OffsetDateTime copyCompletionTime;
    private final String copyStatusDescription;
    private final Boolean isServerEncrypted;
    private final Boolean isIncrementalCopy;
    private final AccessTier accessTier;
    private final ArchiveStatus archiveStatus;
    private final String encryptionKeySha256;
    private final OffsetDateTime accessTierChangeTime;
    private final Map<String, String> metadata;
    private final Boolean isDirectory;
    private final OffsetDateTime expiresOn;
    private String encryptionScope;
    private String encryptionContext;
    private String owner;
    private String group;
    private String permissions;
    private List<PathAccessControlEntry> accessControlList;

    static {
        AccessorUtility.setPathPropertiesAccessor(
            (properties, encryptionScope, encryptionContext, owner, group, permissions, AccessControlList) -> {
                properties.encryptionScope = encryptionScope;
                properties.encryptionContext = encryptionContext;
                properties.owner = owner;
                properties.group = group;
                properties.permissions = permissions;
                properties.accessControlList = PathAccessControlEntry.parseList(AccessControlList);

                return properties;
            });
    }

    /**
     * Constructs a {@link PathProperties}.
     *
     * @param creationTime Creation time of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param eTag ETag of the file.
     * @param fileSize Size of the file.
     * @param contentType Content type specified for the file.
     * @param contentMd5 Content MD5 specified for the file.
     * @param contentEncoding Content encoding specified for the file.
     * @param contentDisposition Content disposition specified for the file.
     * @param contentLanguage Content language specified for the file.
     * @param cacheControl Cache control specified for the file.
     * @param leaseStatus Status of the lease on the file.
     * @param leaseState State of the lease on the file.
     * @param leaseDuration Type of lease on the file.
     * @param copyId Identifier of the last copy operation performed on the file.
     * @param copyStatus Status of the last copy operation performed on the file.
     * @param copySource Source of the last copy operation performed on the file.
     * @param copyProgress Progress of the last copy operation performed on the file.
     * @param copyCompletionTime Datetime when the last copy operation on the file completed.
     * @param copyStatusDescription Description of the last copy operation on the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     * @param isIncrementalCopy Flag indicating if the file was incrementally copied.
     * @param accessTier Access tier of the file.
     * @param archiveStatus Archive status of the file.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param accessTierChangeTime Datetime when the access tier of the file last changed.
     * @param metadata Metadata associated with the file.
     * pass {@code null}.
     */
    public PathProperties(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final String contentType, final byte[] contentMd5, final String contentEncoding,
        final String contentDisposition, final String contentLanguage, final String cacheControl,
        final LeaseStatusType leaseStatus, final LeaseStateType leaseState, final LeaseDurationType leaseDuration,
        final String copyId, final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final AccessTier accessTier, final ArchiveStatus archiveStatus,
        final String encryptionKeySha256, final OffsetDateTime accessTierChangeTime,
        final Map<String, String> metadata) {
        this(creationTime, lastModified, eTag, fileSize, contentType, contentMd5, contentEncoding, contentDisposition,
            contentLanguage, cacheControl, leaseStatus, leaseState, leaseDuration, copyId, copyStatus, copySource,
            copyProgress, copyCompletionTime, copyStatusDescription, isServerEncrypted, isIncrementalCopy, accessTier,
            archiveStatus, encryptionKeySha256, accessTierChangeTime, metadata, null);
    }

    /**
     * Constructs a {@link PathProperties}.
     *
     * @param creationTime Creation time of the file.
     * @param lastModified Datetime when the file was last modified.
     * @param eTag ETag of the file.
     * @param fileSize Size of the file.
     * @param contentType Content type specified for the file.
     * @param contentMd5 Content MD5 specified for the file.
     * @param contentEncoding Content encoding specified for the file.
     * @param contentDisposition Content disposition specified for the file.
     * @param contentLanguage Content language specified for the file.
     * @param cacheControl Cache control specified for the file.
     * @param leaseStatus Status of the lease on the file.
     * @param leaseState State of the lease on the file.
     * @param leaseDuration Type of lease on the file.
     * @param copyId Identifier of the last copy operation performed on the file.
     * @param copyStatus Status of the last copy operation performed on the file.
     * @param copySource Source of the last copy operation performed on the file.
     * @param copyProgress Progress of the last copy operation performed on the file.
     * @param copyCompletionTime Datetime when the last copy operation on the file completed.
     * @param copyStatusDescription Description of the last copy operation on the file.
     * @param isServerEncrypted Flag indicating if the file's content is encrypted on the server.
     * @param isIncrementalCopy Flag indicating if the file was incrementally copied.
     * @param accessTier Access tier of the file.
     * @param archiveStatus Archive status of the file.
     * @param encryptionKeySha256 SHA256 of the customer provided encryption key used to encrypt the file on the server.
     * @param accessTierChangeTime Datetime when the access tier of the file last changed.
     * @param metadata Metadata associated with the file.
     * pass {@code null}.
     * @param expiresOn the time when the path is going to expire.
     */
    public PathProperties(final OffsetDateTime creationTime, final OffsetDateTime lastModified, final String eTag,
        final long fileSize, final String contentType, final byte[] contentMd5, final String contentEncoding,
        final String contentDisposition, final String contentLanguage, final String cacheControl,
        final LeaseStatusType leaseStatus, final LeaseStateType leaseState, final LeaseDurationType leaseDuration,
        final String copyId, final CopyStatusType copyStatus, final String copySource, final String copyProgress,
        final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final Boolean isServerEncrypted,
        final Boolean isIncrementalCopy, final AccessTier accessTier, final ArchiveStatus archiveStatus,
        final String encryptionKeySha256, final OffsetDateTime accessTierChangeTime, final Map<String, String> metadata,
        final OffsetDateTime expiresOn) {
        this.creationTime = creationTime;
        this.lastModified = lastModified;
        this.eTag = eTag;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.contentMd5 = CoreUtils.clone(contentMd5);
        this.contentEncoding = contentEncoding;
        this.contentDisposition = contentDisposition;
        this.contentLanguage = contentLanguage;
        this.cacheControl = cacheControl;
        this.leaseStatus = leaseStatus;
        this.leaseState = leaseState;
        this.leaseDuration = leaseDuration;
        this.copyId = copyId;
        this.copyStatus = copyStatus;
        this.copySource = copySource;
        this.copyProgress = copyProgress;
        this.copyCompletionTime = copyCompletionTime;
        this.copyStatusDescription = copyStatusDescription;
        this.isServerEncrypted = isServerEncrypted;
        this.isIncrementalCopy = isIncrementalCopy;
        this.accessTier = accessTier;
        this.archiveStatus = archiveStatus;
        this.encryptionKeySha256 = encryptionKeySha256;
        this.accessTierChangeTime = accessTierChangeTime;
        this.metadata = metadata;
        /* Default isDirectory to false. */
        if (this.metadata == null) {
            this.isDirectory = false;
        } else {
            this.isDirectory = Boolean.parseBoolean(metadata.get(Constants.HeaderConstants.DIRECTORY_METADATA_KEY));
        }
        this.expiresOn = expiresOn;
    }

    /**
     * Gets the time when the path was created.
     *
     * @return the time when the path was created
     */
    public OffsetDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * Gets the time when the path was last modified.
     *
     * @return the time when the path was last modified
     */
    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    /**
     * Gets the eTag of the path.
     *
     * @return the eTag of the path
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Gets the size of the path in bytes.
     *
     * @return the size of the path in bytes
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the content type of the path.
     *
     * @return the content type of the path
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the MD5 of the path's content.
     *
     * @return the MD5 of the path's content
     */
    public byte[] getContentMd5() {
        return CoreUtils.clone(contentMd5);
    }

    /**
     * Gets the content encoding of the path.
     *
     * @return the content encoding of the path
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Gets the content disposition of the path.
     *
     * @return the content disposition of the path
     */
    public String getContentDisposition() {
        return contentDisposition;
    }

    /**
     * Gets the content language of the path.
     *
     * @return the content language of the path
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Gets the cache control of the path.
     *
     * @return the cache control of the path
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Gets the lease status of the path.
     *
     * @return the lease status of the path
     */
    public LeaseStatusType getLeaseStatus() {
        return leaseStatus;
    }

    /**
     * Gets the lease state of the path.
     *
     * @return the lease state of the path
     */
    public LeaseStateType getLeaseState() {
        return leaseState;
    }

    /**
     * Gets the lease duration if the path is leased.
     *
     * @return the lease duration if the path is leased
     */
    public LeaseDurationType getLeaseDuration() {
        return leaseDuration;
    }

    /**
     * Gets the identifier of the last copy operation.
     *
     * @return the identifier of the last copy operation. If this path hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId() {
        return copyId;
    }

    /**
     * Gets the status of the last copy operation.
     *
     * @return the status of the last copy operation. If this path hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus() {
        return copyStatus;
    }

    /**
     * Gets the source path URL from the last copy operation.
     *
     * @return the source path URL from the last copy operation. If this path hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource() {
        return copySource;
    }

    /**
     * Gets the number of bytes copied and total bytes in the source from the last copy operation.
     *
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this path hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress() {
        return copyProgress;
    }

    /**
     * Gets the completion time of the last copy operation.
     *
     * @return the completion time of the last copy operation. If this path hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime() {
        return copyCompletionTime;
    }

    /**
     * Gets the description of the last copy failure.
     *
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this path hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription() {
        return copyStatusDescription;
    }

    /**
     * Gets the status of the path being encrypted on the server.
     *
     * @return the status of the path being encrypted on the server
     */
    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    /**
     * Gets the status of the path being an incremental copy file.
     *
     * @return the status of the path being an incremental copy file
     */
    public Boolean isIncrementalCopy() {
        return isIncrementalCopy;
    }

    /**
     * Gets the tier of the path.
     *
     * @return the tier of the path.
     */
    public AccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * Gets the archive status of the path.
     *
     * @return the archive status of the path.
     */
    public ArchiveStatus getArchiveStatus() {
        return archiveStatus;
    }

    /**
     * Gets the SHA256 of the encryption key used to encrypt the path.
     *
     * @return the key used to encrypt the path
     */
    public String getEncryptionKeySha256() {
        return encryptionKeySha256;
    }

    /**
     * Gets the time when the access tier for the path was last changed.
     *
     * @return the time when the access tier for the path was last changed
     */
    public OffsetDateTime getAccessTierChangeTime() {
        return accessTierChangeTime;
    }

    /**
     * Gets the metadata associated to this path.
     *
     * @return the metadata associated to this path
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Gets whether this path represents a directory.
     *
     * @return whether this path represents a directory
     */
    public Boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Gets the time when the path is going to expire.
     *
     * @return the time when the path is going to expire.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * Gets the path's encryption scope.
     *
     * @return the path's encryption scope.
     */
    public String getEncryptionScope() {
        return encryptionScope;
    }

    /**
     * Gets the encryption context for this path. Only applicable for files.
     *
     * @return the encryption context for this path. Only applicable for files.
     */
    public String getEncryptionContext() {
        return encryptionContext;
    }

    /**
     * Get the owner property of the path: The owner property.
     *
     * @return the owner value.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Get the group property of the path: The owner property.
     *
     * @return the group value.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Get the permissions property of the path: The permissions property.
     *
     * @return the permissions value.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Optional. The POSIX access control list for the file or directory.
     *
     * @return the access control list.
     */
    public List<PathAccessControlEntry> getAccessControlList() {
        return accessControlList;
    }

}
