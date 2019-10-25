package com.azure.storage.file.datalake;

import com.azure.storage.blob.BlobContainerProperties;
import com.azure.storage.blob.BlobProperties;
import com.azure.storage.blob.models.BlobAccessConditions;
import com.azure.storage.blob.models.BlobContainerAccessConditions;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerItemProperties;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ReliableDownloadOptions;
import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.implementation.models.PathHTTPHeaders;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileSystemAccessConditions;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.FileSystemItemProperties;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PathAccessConditions;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;

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
        if (fileSystemPublicAccessType == null) {
            return null;
        }
        return com.azure.storage.blob.models.PublicAccessType.fromString(fileSystemPublicAccessType.toString());
    }

    static LeaseDurationType toDataLakeLeaseDurationType(com.azure.storage.blob.models.LeaseDurationType
        blobLeaseDurationType) {
        if (blobLeaseDurationType == null) {
            return null;
        }
        return LeaseDurationType.fromString(blobLeaseDurationType.toString());
    }

    static LeaseStateType toDataLakeLeaseStateType(com.azure.storage.blob.models.LeaseStateType
        blobLeaseStateType) {
        if (blobLeaseStateType == null) {
            return null;
        }
        return LeaseStateType.fromString(blobLeaseStateType.toString());
    }

    static LeaseStatusType toDataLakeLeaseStatusType(com.azure.storage.blob.models.LeaseStatusType
        blobLeaseStatusType) {
        if (blobLeaseStatusType == null) {
            return null;
        }
        return LeaseStatusType.fromString(blobLeaseStatusType.toString());
    }

    static PublicAccessType toDataLakePublicAccessType(com.azure.storage.blob.models.PublicAccessType
        blobPublicAccessType) {
        if (blobPublicAccessType == null) {
            return null;
        }
        return PublicAccessType.fromString(blobPublicAccessType.toString());
    }

    static FileSystemProperties toFileSystemProperties(BlobContainerProperties blobContainerProperties) {
        if (blobContainerProperties == null) {
            return null;
        }
        return new FileSystemProperties(blobContainerProperties);
    }

    private static BlobContainerListDetails toBlobContainerListDetails(FileSystemListDetails fileSystemListDetails) {
        return new BlobContainerListDetails()
            .setRetrieveMetadata(fileSystemListDetails.getRetrieveMetadata());
    }

    static ListBlobContainersOptions toListBlobContainersOptions(ListFileSystemsOptions listFileSystemsOptions) {
        return new ListBlobContainersOptions()
            .setDetails(toBlobContainerListDetails(listFileSystemsOptions.getDetails()))
            .setMaxResultsPerPage(listFileSystemsOptions.getMaxResultsPerPage())
            .setPrefix(listFileSystemsOptions.getPrefix());
    }

    static UserDelegationKey toDataLakeUserDelegationKey(com.azure.storage.blob.models.UserDelegationKey blobUserDelegationKey) {
        return new UserDelegationKey()
            .setSignedExpiry(blobUserDelegationKey.getSignedExpiry())
            .setSignedOid(blobUserDelegationKey.getSignedOid())
            .setSignedTid(blobUserDelegationKey.getSignedTid())
            .setSignedService(blobUserDelegationKey.getSignedService())
            .setSignedStart(blobUserDelegationKey.getSignedStart())
            .setSignedVersion(blobUserDelegationKey.getSignedVersion())
            .setValue(blobUserDelegationKey.getValue());
    }

    static BlobAccessConditions toBlobAccessConditions(PathAccessConditions
        pathAccessConditions) {
        if (pathAccessConditions == null) {
            return null;
        } else {
            return new BlobAccessConditions()
                .setModifiedAccessConditions(toBlobModifiedAccessConditions(pathAccessConditions
                    .getModifiedAccessConditions()))
                .setLeaseAccessConditions(toBlobLeaseAccessConditions(pathAccessConditions
                    .getLeaseAccessConditions()));
        }
    }

    static String endpointToDesiredEndpoint(String endpoint, String desiredEndpoint, String currentEndpoint) {
        String desiredRegex = "." + desiredEndpoint + ".";
        String currentRegex = "." + currentEndpoint + ".";
        if (endpoint.contains(desiredRegex)) {
            return endpoint;
        } else {
            return endpoint.replaceFirst(currentRegex, desiredRegex);
        }
    }

    static BlobHttpHeaders toBlobHttpHeaders(PathHTTPHeaders pathHTTPHeaders) {
        if (pathHTTPHeaders == null) {
            return null;
        }
        return new BlobHttpHeaders()
            .setBlobCacheControl(pathHTTPHeaders.getCacheControl())
            .setBlobContentDisposition(pathHTTPHeaders.getContentDisposition())
            .setBlobContentEncoding(pathHTTPHeaders.getContentEncoding())
            .setBlobContentLanguage(pathHTTPHeaders.getContentLanguage())
            .setBlobContentType(pathHTTPHeaders.getContentType())
            .setBlobContentMD5(pathHTTPHeaders.getContentMD5());
    }

    static BlobRange toBlobRange(FileRange fileRange) {
        if (fileRange == null) {
            return null;
        }
        return new BlobRange(fileRange.getOffset(), fileRange.getCount());
    }

    static ReliableDownloadOptions toBlobReliableDownloadOptions(
        com.azure.storage.file.datalake.models.ReliableDownloadOptions dataLakeOptions) {
        if (dataLakeOptions == null) {
            return null;
        }
        return new ReliableDownloadOptions()
            .maxRetryRequests(dataLakeOptions.maxRetryRequests());
    }

    static PathProperties toPathProperties(BlobProperties blobProperties) {
        if (blobProperties == null) {
            return null;
        } else {
            return new PathProperties(blobProperties);
        }
    }

    static FileSystemItem toFileSystemItem(BlobContainerItem blobContainerItem) {
        if (blobContainerItem == null) {
            return null;
        }
        return new FileSystemItem()
            .setName(blobContainerItem.getName())
            .setMetadata(blobContainerItem.getMetadata())
            .setProperties(Transforms.toFileSystemItemProperties(blobContainerItem.getProperties()));
    }

    private static FileSystemItemProperties toFileSystemItemProperties(
        BlobContainerItemProperties blobContainerItemProperties) {
        if (blobContainerItemProperties == null) {
            return null;
        }
        return new FileSystemItemProperties()
            .setETag(blobContainerItemProperties.getETag())
            .setLastModified(blobContainerItemProperties.getLastModified())
            .setLeaseStatus(toDataLakeLeaseStatusType(blobContainerItemProperties.getLeaseStatus()))
            .setLeaseState(toDataLakeLeaseStateType(blobContainerItemProperties.getLeaseState()))
            .setLeaseDuration(toDataLakeLeaseDurationType(blobContainerItemProperties.getLeaseDuration()))
            .setPublicAccess(toDataLakePublicAccessType(blobContainerItemProperties.getPublicAccess()))
            .setHasLegalHold(blobContainerItemProperties.isHasLegalHold())
            .setHasImmutabilityPolicy(blobContainerItemProperties.isHasImmutabilityPolicy());
    }

    static PathItem toPathItem(Path path) {
        if (path == null) {
            return null;
        }
        return new PathItem(path);
    }
}
