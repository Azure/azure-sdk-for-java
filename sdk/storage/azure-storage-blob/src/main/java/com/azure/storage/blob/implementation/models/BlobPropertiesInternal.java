// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.models;

import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobImmutabilityPolicy;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;
import com.azure.storage.blob.models.ObjectReplicationPolicy;
import com.azure.storage.blob.models.RehydratePriority;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Internal interface that represents the getter methods for {@link BlobProperties}.
 * <p>
 * This interface exists to resolve the problem of the ever-growing constructor argument list as {@link BlobProperties}
 * has new properties added to the class. With this each unique API that returns {@link BlobProperties} but with a
 * different set of properties will implement this interface and manage which getter APIs it implements based on the
 * information available to it. With this interface {@link BlobProperties} should no longer be required to add a new
 * constructor every time a new property is added, instead this interface should add a new default getter and each
 * implementation of this interface should override it if it contains that property.
 */
public interface BlobPropertiesInternal {
    /**
     * @return the time when the blob was created
     */
    OffsetDateTime getCreationTime();

    /**
     * @return the time when the blob was last modified
     */
    OffsetDateTime getLastModified();

    /**
     * @return the eTag of the blob
     */
    String getETag();

    /**
     * @return the size of the blob in bytes
     */
    long getBlobSize();

    /**
     * @return the content type of the blob
     */
    String getContentType();

    /**
     * @return the MD5 of the blob's content
     */
    byte[] getContentMd5();

    /**
     * @return the content encoding of the blob
     */
    String getContentEncoding();

    /**
     * @return the content disposition of the blob
     */
    String getContentDisposition();

    /**
     * @return the content language of the blob
     */
    String getContentLanguage();

    /**
     * @return the cache control of the blob
     */
    String getCacheControl();

    /**
     * @return the current sequence number of the page blob. This is only returned for page blobs.
     */
    Long getBlobSequenceNumber();

    /**
     * @return the type of the blob
     */
    BlobType getBlobType();

    /**
     * @return the lease status of the blob
     */
    LeaseStatusType getLeaseStatus();

    /**
     * @return the lease state of the blob
     */
    LeaseStateType getLeaseState();

    /**
     * @return the lease duration if the blob is leased
     */
    LeaseDurationType getLeaseDuration();

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    String getCopyId();

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    CopyStatusType getCopyStatus();

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    String getCopySource();

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    String getCopyProgress();

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    OffsetDateTime getCopyCompletionTime();

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    String getCopyStatusDescription();

    /**
     * @return the status of the blob being encrypted on the server
     */
    Boolean isServerEncrypted();

    /**
     * @return the status of the blob being an incremental copy blob
     */
    Boolean isIncrementalCopy();

    /**
     * @return the snapshot time of the last successful incremental copy snapshot for this blob. If this blob isn't an
     * incremental copy blob or incremental copy snapshot or {@link #getCopyStatus() getCopyStatus} isn't {@link
     * CopyStatusType#SUCCESS success} this won't be set.
     */
    String getCopyDestinationSnapshot();

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    AccessTier getAccessTier();

    /**
     * @return the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account.
     */
    Boolean isAccessTierInferred();

    /**
     * @return the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     */
    ArchiveStatus getArchiveStatus();

    /**
     * @return the key used to encrypt the blob
     */
    String getEncryptionKeySha256();

    /**
     * @return The name of the encryption scope under which the blob is encrypted.
     */
    String getEncryptionScope();

    /**
     * @return the time when the access tier for the blob was last changed
     */
    OffsetDateTime getAccessTierChangeTime();

    /**
     * @return the metadata associated with this blob
     */
    Map<String, String> getMetadata();

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    Integer getCommittedBlockCount();

    /**
     * @return The number of tags associated with the blob.
     */
    Long getTagCount();

    /**
     * @return the version identifier the blob.
     */
    String getVersionId();

    /**
     * @return the flag indicating whether version identifier points to current version of the blob.
     */
    Boolean isCurrentVersion();

    /**
     * @return a {@link List} that contains information on the object replication policies associated with this blob and
     * the status of the replication for each policy. Only available when the blob is the source of object replication.
     */
    List<ObjectReplicationPolicy> getObjectReplicationSourcePolicies();

    /**
     * @return a {@code String} that identifies the Object Replication Policy which made this blob the destination of a
     * copy.
     */
    String getObjectReplicationDestinationPolicyId();

    /**
     * @return The {@link RehydratePriority} of the blob if it is in RehydratePending state.
     */
    RehydratePriority getRehydratePriority();

    /**
     * @return the flag indicating whether this blob has been sealed (marked as read only). This is only returned for
     * Append blobs.
     */
    Boolean isSealed();

    /**
     * @return The date and time the blob was read or written to.
     */
    OffsetDateTime getLastAccessedTime();

    /**
     * @return the time when the blob is going to expire.
     */
    OffsetDateTime getExpiresOn();

    /**
     * @return the immutability policy.
     */
    BlobImmutabilityPolicy getImmutabilityPolicy();

    /**
     * @return whether the blob has a legal hold.
     */
    Boolean hasLegalHold();
}
