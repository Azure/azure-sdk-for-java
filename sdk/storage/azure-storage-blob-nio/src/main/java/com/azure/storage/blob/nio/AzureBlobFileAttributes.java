package com.azure.storage.blob.nio;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.AccessTier;
import com.azure.storage.blob.models.ArchiveStatus;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.BlobType;
import com.azure.storage.blob.models.CopyStatusType;
import com.azure.storage.blob.models.LeaseDurationType;
import com.azure.storage.blob.models.LeaseStateType;
import com.azure.storage.blob.models.LeaseStatusType;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.OffsetDateTime;
import java.util.Map;

public class AzureBlobFileAttributes implements BasicFileAttributes {
    private final ClientLogger logger = new ClientLogger(AzureBlobFileAttributes.class);

    private final BlobProperties properties;
    private final boolean isVirtualDir;

    AzureBlobFileAttributes(Path path) throws IOException {
        try {
            this.properties = new AzureResource(path).getBlobClient().getProperties();
        } catch (BlobStorageException e) {
            if (e.getErrorCode().equals(BlobErrorCode.BLOB_NOT_FOUND)) {
                DirectoryStatus status = new AzureResource(path).checkDirStatus();
                if (DirectoryStatus.)
            }
        }
    }

    /**
     * @return the time when the blob was created
     */
    public OffsetDateTime getCreationTime();

    /**
     * @return the time when the blob was last modified
     */
    public OffsetDateTime getLastModified();

    /**
     * @return the eTag of the blob
     */
    public String getETag();

    /**
     * @return the size of the blob in bytes
     */
    public long getBlobSize();

    /**
     * @return the content type of the blob
     */
    public String getContentType();

    /**
     * @return the MD5 of the blob's content
     */
    public byte[] getContentMd5();

    /**
     * @return the content encoding of the blob
     */
    public String getContentEncoding();

    /**
     * @return the content disposition of the blob
     */
    public String getContentDisposition();

    /**
     * @return the content language of the blob
     */
    public String getContentLanguage();

    /**
     * @return the cache control of the blob
     */
    public String getCacheControl();

    /**
     * @return the current sequence number of the page blob. This is only returned for page blobs.
     */
    public Long getBlobSequenceNumber();

    /**
     * @return the type of the blob
     */
    public BlobType getBlobType();

    /**
     * @return the lease status of the blob
     */
    public LeaseStatusType getLeaseStatus();

    /**
     * @return the lease state of the blob
     */
    public LeaseStateType getLeaseState();

    /**
     * @return the lease duration if the blob is leased
     */
    public LeaseDurationType getLeaseDuration();

    /**
     * @return the identifier of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public String getCopyId();

    /**
     * @return the status of the last copy operation. If this blob hasn't been the target of a copy operation or has
     * been modified since this won't be set.
     */
    public CopyStatusType getCopyStatus();

    /**
     * @return the source blob URL from the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public String getCopySource();

    /**
     * @return the number of bytes copied and total bytes in the source from the last copy operation (bytes copied/total
     * bytes). If this blob hasn't been the target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyProgress();

    /**
     * @return the completion time of the last copy operation. If this blob hasn't been the target of a copy operation
     * or has been modified since this won't be set.
     */
    public OffsetDateTime getCopyCompletionTime();

    /**
     * @return the description of the last copy failure, this is set when the {@link #getCopyStatus() getCopyStatus} is
     * {@link CopyStatusType#FAILED failed} or {@link CopyStatusType#ABORTED aborted}. If this blob hasn't been the
     * target of a copy operation or has been modified since this won't be set.
     */
    public String getCopyStatusDescription();

    /**
     * @return the status of the blob being encrypted on the server
     */
    public Boolean isServerEncrypted();

    /**
     * @return the status of the blob being an incremental copy blob
     */
    public Boolean isIncrementalCopy();

    /**
     * @return the snapshot time of the last successful incremental copy snapshot for this blob. If this blob isn't an
     * incremental copy blob or incremental copy snapshot or {@link #getCopyStatus() getCopyStatus} isn't {@link
     * CopyStatusType#SUCCESS success} this won't be set.
     */
    public String getCopyDestinationSnapshot();

    /**
     * @return the tier of the blob. This is only set for Page blobs on a premium storage account or for Block blobs on
     * blob storage or general purpose V2 account.
     */
    public AccessTier getAccessTier();

    /**
     * @return the status of the tier being inferred for the blob. This is only set for Page blobs on a premium storage
     * account or for Block blobs on blob storage or general purpose V2 account.
     */
    public Boolean isAccessTierInferred();

    /**
     * @return the archive status of the blob. This is only for blobs on a blob storage and general purpose v2 account.
     */
    public ArchiveStatus getArchiveStatus();

    /**
     * @return the key used to encrypt the blob
     */
    public String getEncryptionKeySha256();

    /**
     * @return the time when the access tier for the blob was last changed
     */
    public OffsetDateTime getAccessTierChangeTime();

    /**
     * @return the metadata associated with this blob
     */
    public Map<String, String> getMetadata();

    /**
     * @return the number of committed blocks in the blob. This is only returned for Append blobs.
     */
    public Integer getCommittedBlockCount();

    /**
     *
     */
    public boolean isConcreteDirectory();

    /**
     * Makes a rest request.
     */
    public boolean isVirtualDirectory();

    @Override
    public FileTime lastModifiedTime() {
        return null;
    }

    @Override
    public FileTime lastAccessTime() {
        return null;
    }

    @Override
    public FileTime creationTime() {
        return null;
    }

    @Override
    public boolean isRegularFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public Object fileKey() {
        return null;
    }
}
