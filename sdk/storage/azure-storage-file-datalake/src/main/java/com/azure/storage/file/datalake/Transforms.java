// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerItemProperties;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.ArchiveStatus;
import com.azure.storage.file.datalake.models.CopyStatusType;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.FileRange;
import com.azure.storage.file.datalake.models.FileReadAsyncResponse;
import com.azure.storage.file.datalake.models.FileReadHeaders;
import com.azure.storage.file.datalake.models.FileReadResponse;
import com.azure.storage.file.datalake.models.FileSystemAccessPolicies;
import com.azure.storage.file.datalake.models.FileSystemItem;
import com.azure.storage.file.datalake.models.FileSystemItemProperties;
import com.azure.storage.file.datalake.models.FileSystemListDetails;
import com.azure.storage.file.datalake.models.FileSystemProperties;
import com.azure.storage.file.datalake.models.LeaseDurationType;
import com.azure.storage.file.datalake.models.LeaseStateType;
import com.azure.storage.file.datalake.models.LeaseStatusType;
import com.azure.storage.file.datalake.models.ListFileSystemsOptions;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.sas.DataLakeServiceSasSignatureValues;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class Transforms {

    static com.azure.storage.blob.models.PublicAccessType toBlobPublicAccessType(PublicAccessType
        fileSystemPublicAccessType) {
        if (fileSystemPublicAccessType == null) {
            return null;
        }
        return com.azure.storage.blob.models.PublicAccessType.fromString(fileSystemPublicAccessType.toString());
    }

    private static LeaseDurationType toDataLakeLeaseDurationType(com.azure.storage.blob.models.LeaseDurationType
        blobLeaseDurationType) {
        if (blobLeaseDurationType == null) {
            return null;
        }
        return LeaseDurationType.fromString(blobLeaseDurationType.toString());
    }

    private static LeaseStateType toDataLakeLeaseStateType(com.azure.storage.blob.models.LeaseStateType
        blobLeaseStateType) {
        if (blobLeaseStateType == null) {
            return null;
        }
        return LeaseStateType.fromString(blobLeaseStateType.toString());
    }

    private static LeaseStatusType toDataLakeLeaseStatusType(com.azure.storage.blob.models.LeaseStatusType
        blobLeaseStatusType) {
        if (blobLeaseStatusType == null) {
            return null;
        }
        return LeaseStatusType.fromString(blobLeaseStatusType.toString());
    }

    private static PublicAccessType toDataLakePublicAccessType(com.azure.storage.blob.models.PublicAccessType
        blobPublicAccessType) {
        if (blobPublicAccessType == null) {
            return null;
        }
        return PublicAccessType.fromString(blobPublicAccessType.toString());
    }

    private static CopyStatusType toDataLakeCopyStatusType(
        com.azure.storage.blob.models.CopyStatusType blobCopyStatus) {
        if (blobCopyStatus == null) {
            return null;
        }
        return CopyStatusType.fromString(blobCopyStatus.toString());
    }

    private static ArchiveStatus toDataLakeArchiveStatus(
        com.azure.storage.blob.models.ArchiveStatus blobArchiveStatus) {
        if (blobArchiveStatus == null) {
            return null;
        }
        return ArchiveStatus.fromString(blobArchiveStatus.toString());
    }

    private static AccessTier toDataLakeAccessTier(com.azure.storage.blob.models.AccessTier blobAccessTier) {
        if (blobAccessTier == null) {
            return null;
        }
        return AccessTier.fromString(blobAccessTier.toString());
    }

    static FileSystemProperties toFileSystemProperties(BlobContainerProperties blobContainerProperties) {
        if (blobContainerProperties == null) {
            return null;
        }
        return new FileSystemProperties(blobContainerProperties.getMetadata(), blobContainerProperties.getETag(),
            blobContainerProperties.getLastModified(),
            Transforms.toDataLakeLeaseDurationType(blobContainerProperties.getLeaseDuration()),
            Transforms.toDataLakeLeaseStateType(blobContainerProperties.getLeaseState()),
            Transforms.toDataLakeLeaseStatusType(blobContainerProperties.getLeaseStatus()),
            Transforms.toDataLakePublicAccessType(blobContainerProperties.getBlobPublicAccess()),
            blobContainerProperties.hasImmutabilityPolicy(), blobContainerProperties.hasLegalHold());
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

    static UserDelegationKey toDataLakeUserDelegationKey(com.azure.storage.blob.models.UserDelegationKey
        blobUserDelegationKey) {
        if (blobUserDelegationKey == null) {
            return null;
        }
        return new UserDelegationKey()
            .setSignedExpiry(blobUserDelegationKey.getSignedExpiry())
            .setSignedObjectId(blobUserDelegationKey.getSignedObjectId())
            .setSignedTenantId(blobUserDelegationKey.getSignedTenantId())
            .setSignedService(blobUserDelegationKey.getSignedService())
            .setSignedStart(blobUserDelegationKey.getSignedStart())
            .setSignedVersion(blobUserDelegationKey.getSignedVersion())
            .setValue(blobUserDelegationKey.getValue());
    }

    static com.azure.storage.blob.models.UserDelegationKey toBlobUserDelegationKey(UserDelegationKey
        dataLakeUserDelegationKey) {
        if (dataLakeUserDelegationKey == null) {
            return null;
        }
        return new com.azure.storage.blob.models.UserDelegationKey()
            .setSignedExpiry(dataLakeUserDelegationKey.getSignedExpiry())
            .setSignedObjectId(dataLakeUserDelegationKey.getSignedObjectId())
            .setSignedTenantId(dataLakeUserDelegationKey.getSignedTenantId())
            .setSignedService(dataLakeUserDelegationKey.getSignedService())
            .setSignedStart(dataLakeUserDelegationKey.getSignedStart())
            .setSignedVersion(dataLakeUserDelegationKey.getSignedVersion())
            .setValue(dataLakeUserDelegationKey.getValue());
    }

    static BlobHttpHeaders toBlobHttpHeaders(PathHttpHeaders pathHTTPHeaders) {
        if (pathHTTPHeaders == null) {
            return null;
        }
        return new BlobHttpHeaders()
            .setCacheControl(pathHTTPHeaders.getCacheControl())
            .setContentDisposition(pathHTTPHeaders.getContentDisposition())
            .setContentEncoding(pathHTTPHeaders.getContentEncoding())
            .setContentLanguage(pathHTTPHeaders.getContentLanguage())
            .setContentType(pathHTTPHeaders.getContentType())
            .setContentMd5(pathHTTPHeaders.getContentMd5());
    }

    static BlobRange toBlobRange(FileRange fileRange) {
        if (fileRange == null) {
            return null;
        }
        return new BlobRange(fileRange.getOffset(), fileRange.getCount());
    }

    static com.azure.storage.blob.models.DownloadRetryOptions toBlobDownloadRetryOptions(
        DownloadRetryOptions dataLakeOptions) {
        if (dataLakeOptions == null) {
            return null;
        }
        return new com.azure.storage.blob.models.DownloadRetryOptions()
            .setMaxRetryRequests(dataLakeOptions.getMaxRetryRequests());
    }

    static PathProperties toPathProperties(BlobProperties properties) {
        if (properties == null) {
            return null;
        } else {
            return new PathProperties(properties.getCreationTime(), properties.getLastModified(), properties.getETag(),
                properties.getBlobSize(), properties.getContentType(), properties.getContentMd5(),
                properties.getContentEncoding(), properties.getContentDisposition(), properties.getContentLanguage(),
                properties.getCacheControl(), Transforms.toDataLakeLeaseStatusType(properties.getLeaseStatus()),
                Transforms.toDataLakeLeaseStateType(properties.getLeaseState()),
                Transforms.toDataLakeLeaseDurationType(properties.getLeaseDuration()), properties.getCopyId(),
                Transforms.toDataLakeCopyStatusType(properties.getCopyStatus()), properties.getCopySource(),
                properties.getCopyProgress(), properties.getCopyCompletionTime(), properties.getCopyStatusDescription(),
                properties.isServerEncrypted(), properties.isIncrementalCopy(),
                Transforms.toDataLakeAccessTier(properties.getAccessTier()),
                Transforms.toDataLakeArchiveStatus(properties.getArchiveStatus()), properties.getEncryptionKeySha256(),
                properties.getAccessTierChangeTime(), properties.getMetadata());
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
        return new PathItem(path.getETag(),
            OffsetDateTime.parse(path.getLastModified(), DateTimeFormatter.RFC_1123_DATE_TIME),
            path.getContentLength(), path.getGroup(), path.isDirectory() == null ? false : path.isDirectory(),
            path.getName(), path.getOwner(), path.getPermissions());
    }

    static BlobRequestConditions toBlobRequestConditions(DataLakeRequestConditions requestConditions) {
        if (requestConditions == null) {
            return null;
        }
        return new BlobRequestConditions()
            .setLeaseId(requestConditions.getLeaseId())
            .setIfUnmodifiedSince(requestConditions.getIfUnmodifiedSince())
            .setIfNoneMatch(requestConditions.getIfNoneMatch())
            .setIfMatch(requestConditions.getIfMatch())
            .setIfModifiedSince(requestConditions.getIfModifiedSince());

    }

    static FileReadResponse toFileReadResponse(BlobDownloadResponse r) {
        if (r == null) {
            return null;
        }
        return new FileReadResponse(Transforms.toFileReadAsyncResponse(new BlobDownloadAsyncResponse(r.getRequest(),
            r.getStatusCode(), r.getHeaders(), null, r.getDeserializedHeaders())));
    }

    static FileReadAsyncResponse toFileReadAsyncResponse(BlobDownloadAsyncResponse r) {
        if (r == null) {
            return null;
        }
        return new FileReadAsyncResponse(r.getRequest(), r.getStatusCode(), r.getHeaders(), r.getValue(),
            Transforms.toPathReadHeaders(r.getDeserializedHeaders()));
    }

    private static FileReadHeaders toPathReadHeaders(BlobDownloadHeaders h) {
        if (h == null) {
            return null;
        }
        return new FileReadHeaders()
            .setLastModified(h.getLastModified())
            .setMetadata(h.getMetadata())
            .setContentLength(h.getContentLength())
            .setContentType(h.getContentType())
            .setContentRange(h.getContentRange())
            .setETag(h.getETag())
            .setContentMd5(h.getContentMd5())
            .setContentEncoding(h.getContentEncoding())
            .setCacheControl(h.getCacheControl())
            .setContentDisposition(h.getContentDisposition())
            .setContentLanguage(h.getContentLanguage())
            .setCopyCompletionTime(h.getCopyCompletionTime())
            .setCopyStatusDescription(h.getCopyStatusDescription())
            .setCopyId(h.getCopyId())
            .setCopyProgress(h.getCopyProgress())
            .setCopySource(h.getCopySource())
            .setCopyStatus(Transforms.toDataLakeCopyStatusType(h.getCopyStatus()))
            .setLeaseDuration(Transforms.toDataLakeLeaseDurationType(h.getLeaseDuration()))
            .setLeaseState(Transforms.toDataLakeLeaseStateType(h.getLeaseState()))
            .setLeaseStatus(Transforms.toDataLakeLeaseStatusType(h.getLeaseStatus()))
            .setClientRequestId(h.getClientRequestId())
            .setRequestId(h.getRequestId())
            .setVersion(h.getVersion())
            .setAcceptRanges(h.getAcceptRanges())
            .setDateProperty(h.getDateProperty())
            .setIsServerEncrypted(h.isServerEncrypted())
            .setEncryptionKeySha256(h.getEncryptionKeySha256())
            .setFileContentMd5(h.getBlobContentMD5())
            .setContentCrc64(h.getContentCrc64())
            .setErrorCode(h.getErrorCode());
    }

    static List<BlobSignedIdentifier> toBlobIdentifierList(List<DataLakeSignedIdentifier> identifiers) {
        if (identifiers == null) {
            return null;
        }
        List<BlobSignedIdentifier> blobIdentifiers = new ArrayList<>();
        for (DataLakeSignedIdentifier identifier : identifiers) {
            blobIdentifiers.add(Transforms.toBlobIdentifier(identifier));
        }
        return blobIdentifiers;
    }

    private static BlobSignedIdentifier toBlobIdentifier(DataLakeSignedIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return new BlobSignedIdentifier()
            .setId(identifier.getId())
            .setAccessPolicy(Transforms.toBlobAccessPolicy(identifier.getAccessPolicy()));
    }

    private static BlobAccessPolicy toBlobAccessPolicy(DataLakeAccessPolicy accessPolicy) {
        if (accessPolicy == null) {
            return null;
        }
        return new BlobAccessPolicy()
            .setExpiresOn(accessPolicy.getExpiresOn())
            .setStartsOn(accessPolicy.getStartsOn())
            .setPermissions(accessPolicy.getPermissions());
    }

    static FileSystemAccessPolicies toFileSystemAccessPolicies(BlobContainerAccessPolicies accessPolicies) {
        if (accessPolicies == null) {
            return null;
        }
        return new FileSystemAccessPolicies(Transforms.toDataLakePublicAccessType(accessPolicies.getBlobAccessType()),
            Transforms.toDataLakeIdentifierList(accessPolicies.getIdentifiers()));
    }

    static List<DataLakeSignedIdentifier> toDataLakeIdentifierList(List<BlobSignedIdentifier> identifiers) {
        if (identifiers == null) {
            return null;
        }
        List<DataLakeSignedIdentifier> dataLakeIdentifiers = new ArrayList<>();
        for (BlobSignedIdentifier identifier : identifiers) {
            dataLakeIdentifiers.add(Transforms.toDataLakeIdentifier(identifier));
        }
        return dataLakeIdentifiers;
    }

    private static DataLakeSignedIdentifier toDataLakeIdentifier(BlobSignedIdentifier identifier) {
        if (identifier == null) {
            return null;
        }
        return new DataLakeSignedIdentifier()
            .setId(identifier.getId())
            .setAccessPolicy(Transforms.toDataLakeAccessPolicy(identifier.getAccessPolicy()));
    }

    private static DataLakeAccessPolicy toDataLakeAccessPolicy(BlobAccessPolicy accessPolicy) {
        if (accessPolicy == null) {
            return null;
        }
        return new DataLakeAccessPolicy()
            .setExpiresOn(accessPolicy.getExpiresOn())
            .setStartsOn(accessPolicy.getStartsOn())
            .setPermissions(accessPolicy.getPermissions());
    }

    static BlobServiceSasSignatureValues toBlobSasValues(DataLakeServiceSasSignatureValues
        dataLakeServiceSasSignatureValues) {
        if (dataLakeServiceSasSignatureValues == null) {
            return null;
        }
        BlobServiceSasSignatureValues blobServiceSasSignatureValues;
        if (dataLakeServiceSasSignatureValues.getIdentifier() != null) {
            blobServiceSasSignatureValues =
                new BlobServiceSasSignatureValues(dataLakeServiceSasSignatureValues.getIdentifier());
        } else {
            // It's ok to use blob container sas permission since its a super set of blob sas permission
            blobServiceSasSignatureValues =
                new BlobServiceSasSignatureValues(dataLakeServiceSasSignatureValues.getExpiryTime(),
                    BlobContainerSasPermission.parse(dataLakeServiceSasSignatureValues.getPermissions()));
        }
        blobServiceSasSignatureValues.setVersion(dataLakeServiceSasSignatureValues.getVersion())
            .setProtocol(dataLakeServiceSasSignatureValues.getProtocol())
            .setStartTime(dataLakeServiceSasSignatureValues.getStartTime())
            .setExpiryTime(dataLakeServiceSasSignatureValues.getExpiryTime())
            .setSasIpRange(dataLakeServiceSasSignatureValues.getSasIpRange())
            .setIdentifier(dataLakeServiceSasSignatureValues.getIdentifier())
            .setCacheControl(dataLakeServiceSasSignatureValues.getCacheControl())
            .setContentDisposition(dataLakeServiceSasSignatureValues.getContentDisposition())
            .setContentEncoding(dataLakeServiceSasSignatureValues.getContentEncoding())
            .setContentLanguage(dataLakeServiceSasSignatureValues.getContentLanguage())
            .setContentType(dataLakeServiceSasSignatureValues.getContentType());
        if (dataLakeServiceSasSignatureValues.getPermissions() != null) {
            // It's ok to use blob container sas permission since its a super set of blob sas permission
            blobServiceSasSignatureValues.setPermissions(BlobContainerSasPermission.parse(
                dataLakeServiceSasSignatureValues.getPermissions()));
        }
        return blobServiceSasSignatureValues;
    }
}
