package com.azure.storage.file.datalake;

import com.azure.storage.blob.BlobContainerProperties;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PublicAccessType;

class Transforms {

    static BlobContainerAccessConditions toBlobContainerAccessConditions(FileSystemAccessConditions
        fileSystemAccessConditions) {
        if (fileSystemAccessConditions == null) {
            return null;
        } else {
            return new BlobContainerAccessConditions()
                .setModifiedAccessConditions(toBlobModifiedAccessConditions(fileSystemAccessConditions
                    .getModifiedAccessConditions()))
                .setLeaseAccessConditions(toBlobLeaseAccessConditions(fileSystemAccessConditions
                    .getLeaseAccessConditions()));
        }
    }

    static com.azure.storage.blob.models.ModifiedAccessConditions toBlobModifiedAccessConditions(
        ModifiedAccessConditions fileSystemModifiedAccessConditions) {
        if (fileSystemModifiedAccessConditions == null) {
            return null;
        } else {
            return new com.azure.storage.blob.models.ModifiedAccessConditions()
                .setIfMatch(fileSystemModifiedAccessConditions.getIfMatch())
                .setIfModifiedSince(fileSystemModifiedAccessConditions.getIfModifiedSince())
                .setIfNoneMatch(fileSystemModifiedAccessConditions.getIfNoneMatch())
                .setIfUnmodifiedSince(fileSystemModifiedAccessConditions.getIfUnmodifiedSince());
        }
    }

    static com.azure.storage.blob.models.LeaseAccessConditions toBlobLeaseAccessConditions(LeaseAccessConditions
        fileSystemLeaseAccessConditions) {
        if (fileSystemLeaseAccessConditions == null) {
            return null;
        } else {
            return new com.azure.storage.blob.models.LeaseAccessConditions()
                .setLeaseId(fileSystemLeaseAccessConditions.getLeaseId());
        }
    }

    static com.azure.storage.blob.models.PublicAccessType toBlobPublicAccessType(PublicAccessType
        fileSystemPublicAccessType) {
        return com.azure.storage.blob.models.PublicAccessType.fromString(fileSystemPublicAccessType.toString());
    }

    static LeaseDurationType toDataLakeLeaseDurationType(com.azure.storage.blob.models.LeaseDurationType
        blobLeaseDurationType) {
        return LeaseDurationType.fromString(blobLeaseDurationType.toString());
    }

    static LeaseStateType toDataLakeLeaseStateType(com.azure.storage.blob.models.LeaseStateType
        blobLeaseStateType) {
        return LeaseStateType.fromString(blobLeaseStateType.toString());
    }

    static LeaseStatusType toDataLakeLeaseStatusType(com.azure.storage.blob.models.LeaseStatusType
        blobLeaseStatusType) {
        return LeaseStatusType.fromString(blobLeaseStatusType.toString());
    }

    static PublicAccessType toDataLakePublicAccessType(com.azure.storage.blob.models.PublicAccessType
        blobPublicAccessType) {
        return PublicAccessType.fromString(blobPublicAccessType.toString());
    }

    static FileSystemProperties toFileSystemProperties(BlobContainerProperties blobContainerProperties) {
        return new FileSystemProperties()
            .setEtag(blobContainerProperties.getETag())
            .setHasImmutabilityPolicy(blobContainerProperties.hasImmutabilityPolicy())
            .setHasLegalHold(blobContainerProperties.hasLegalHold())
            .setLastModified(blobContainerProperties.getLastModified())
            .setLeaseDuration(toDataLakeLeaseDurationType(blobContainerProperties.getLeaseDuration()))
            .setLeaseState(toDataLakeLeaseStateType(blobContainerProperties.getLeaseState()))
            .setLeaseStatus(toDataLakeLeaseStatusType(blobContainerProperties.getLeaseStatus()))
            .setPublicAccess(toDataLakePublicAccessType(blobContainerProperties.getBlobPublicAccess()));
    }

    static BlobContainerListDetails toBlobContainerListDetails(FileSystemListDetails fileSystemListDetails) {
        return new BlobContainerListDetails()
            .setRetrieveMetadata(fileSystemListDetails.getRetrieveMetadata());
    }

    static ListBlobContainersOptions toListBlobContainersOptions(ListFileSystemsOptions listFileSystemsOptions) {
        return new ListBlobContainersOptions()
            .setDetails(toBlobContainerListDetails(listFileSystemsOptions.getDetails()))
            .setMaxResults(listFileSystemsOptions.getMaxResults())
            .setPrefix(listFileSystemsOptions.getPrefix());
    }

    static FileSystemItem toFileSystemItem(BlobContainerItem blobContainerItem) {
        return new FileSystemItem()
            .setMetadata(blobContainerItem.getMetadata())
            .setName(blobContainerItem.getName())
            .setProperties(toFileSystemProperties(blobContainerItem.getProperties()));
    }

    static FileSystemProperties toFileSystemProperties(com.azure.storage.blob.models.BlobContainerProperties
        blobContainerProperties) {
        return new FileSystemProperties()
            .setEtag(blobContainerProperties.getEtag())
            .setHasImmutabilityPolicy(blobContainerProperties.isHasImmutabilityPolicy())
            .setHasLegalHold(blobContainerProperties.isHasLegalHold())
            .setLastModified(blobContainerProperties.getLastModified())
            .setLeaseDuration(toDataLakeLeaseDurationType(blobContainerProperties.getLeaseDuration()))
            .setLeaseState(toDataLakeLeaseStateType(blobContainerProperties.getLeaseState()))
            .setLeaseStatus(toDataLakeLeaseStatusType(blobContainerProperties.getLeaseStatus()))
            .setPublicAccess(toDataLakePublicAccessType(blobContainerProperties.getPublicAccess()));
    }
}
