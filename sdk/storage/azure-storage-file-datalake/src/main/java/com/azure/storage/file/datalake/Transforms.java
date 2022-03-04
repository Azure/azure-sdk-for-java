// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobAnalyticsLogging;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerItemProperties;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobContainerProperties;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.models.BlobDownloadHeaders;
import com.azure.storage.blob.models.BlobDownloadResponse;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobMetrics;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobQueryArrowField;
import com.azure.storage.blob.models.BlobQueryArrowFieldType;
import com.azure.storage.blob.models.BlobQueryArrowSerialization;
import com.azure.storage.blob.models.BlobQueryAsyncResponse;
import com.azure.storage.blob.models.BlobQueryDelimitedSerialization;
import com.azure.storage.blob.models.BlobQueryError;
import com.azure.storage.blob.models.BlobQueryHeaders;
import com.azure.storage.blob.models.BlobQueryJsonSerialization;
import com.azure.storage.blob.models.BlobQueryParquetSerialization;
import com.azure.storage.blob.models.BlobQueryProgress;
import com.azure.storage.blob.models.BlobQueryResponse;
import com.azure.storage.blob.models.BlobQuerySerialization;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.ConsistentReadControl;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.StaticWebsite;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobQueryOptions;
import com.azure.storage.blob.options.UndeleteBlobContainerOptions;
import com.azure.storage.file.datalake.implementation.models.BlobItemInternal;
import com.azure.storage.file.datalake.implementation.models.BlobPrefix;
import com.azure.storage.file.datalake.implementation.models.Path;
import com.azure.storage.file.datalake.models.AccessTier;
import com.azure.storage.file.datalake.models.ArchiveStatus;
import com.azure.storage.file.datalake.models.CopyStatusType;
import com.azure.storage.file.datalake.models.DataLakeAccessPolicy;
import com.azure.storage.file.datalake.models.DataLakeAnalyticsLogging;
import com.azure.storage.file.datalake.models.DataLakeCorsRule;
import com.azure.storage.file.datalake.models.DataLakeMetrics;
import com.azure.storage.file.datalake.models.DataLakeRequestConditions;
import com.azure.storage.file.datalake.models.DataLakeRetentionPolicy;
import com.azure.storage.file.datalake.models.DataLakeServiceProperties;
import com.azure.storage.file.datalake.models.DataLakeSignedIdentifier;
import com.azure.storage.file.datalake.models.DataLakeStaticWebsite;
import com.azure.storage.file.datalake.models.DownloadRetryOptions;
import com.azure.storage.file.datalake.models.FileQueryArrowField;
import com.azure.storage.file.datalake.models.FileQueryArrowSerialization;
import com.azure.storage.file.datalake.models.FileQueryAsyncResponse;
import com.azure.storage.file.datalake.models.FileQueryDelimitedSerialization;
import com.azure.storage.file.datalake.models.FileQueryError;
import com.azure.storage.file.datalake.models.FileQueryHeaders;
import com.azure.storage.file.datalake.models.FileQueryJsonSerialization;
import com.azure.storage.file.datalake.models.FileQueryParquetSerialization;
import com.azure.storage.file.datalake.models.FileQueryProgress;
import com.azure.storage.file.datalake.models.FileQueryResponse;
import com.azure.storage.file.datalake.models.FileQuerySerialization;
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
import com.azure.storage.file.datalake.models.PathDeletedItem;
import com.azure.storage.file.datalake.models.PathHttpHeaders;
import com.azure.storage.file.datalake.models.PathItem;
import com.azure.storage.file.datalake.models.PathProperties;
import com.azure.storage.file.datalake.models.PublicAccessType;
import com.azure.storage.file.datalake.models.UserDelegationKey;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.FileQueryOptions;
import com.azure.storage.file.datalake.options.FileSystemUndeleteOptions;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class Transforms {

    private static final String SERIALIZATION_MESSAGE = String.format("'serialization' must be one of %s, %s, %s or "
            + "%s.", FileQueryJsonSerialization.class.getSimpleName(),
        FileQueryDelimitedSerialization.class.getSimpleName(), FileQueryArrowSerialization.class.getSimpleName(),
        FileQueryParquetSerialization.class.getSimpleName());

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
        if (fileSystemListDetails == null) {
            return null;
        }
        return new BlobContainerListDetails()
            .setRetrieveMetadata(fileSystemListDetails.getRetrieveMetadata())
            .setRetrieveDeleted(fileSystemListDetails.getRetrieveDeleted());
    }

    static ListBlobContainersOptions toListBlobContainersOptions(ListFileSystemsOptions listFileSystemsOptions) {
        if (listFileSystemsOptions == null) {
            return null;
        }
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

    static BlobInputStreamOptions toBlobInputStreamOptions(DataLakeFileInputStreamOptions options) {
        if (options == null) {
            return null;
        }
        return new BlobInputStreamOptions()
            .setBlockSize(options.getBlockSize())
            .setRange(toBlobRange(options.getRange()))
            .setRequestConditions(toBlobRequestConditions(options.getRequestConditions()))
            .setConsistentReadControl(toBlobConsistentReadControl(options.getConsistentReadControl()));
    }

    static com.azure.storage.blob.models.ConsistentReadControl toBlobConsistentReadControl(
        com.azure.storage.file.datalake.models.ConsistentReadControl datalakeConsistentReadControl) {
        if (datalakeConsistentReadControl == null) {
            return null;
        }
        switch (datalakeConsistentReadControl) {
            case NONE:
                return ConsistentReadControl.NONE;
            case ETAG:
                return ConsistentReadControl.ETAG;
            default:
                throw new IllegalArgumentException("Could not convert ConsistentReadControl");
        }
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
                properties.getAccessTierChangeTime(), properties.getMetadata(), properties.getExpiresOn());
        }
    }


    static FileSystemItem toFileSystemItem(BlobContainerItem blobContainerItem) {
        if (blobContainerItem == null) {
            return null;
        }
        return new FileSystemItem()
            .setName(blobContainerItem.getName())
            .setDeleted(blobContainerItem.isDeleted())
            .setVersion(blobContainerItem.getVersion())
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
            path.getLastModified() == null ? null : OffsetDateTime.parse(path.getLastModified(),
                DateTimeFormatter.RFC_1123_DATE_TIME), path.getContentLength() == null ? 0 : path.getContentLength(),
            path.getGroup(), path.isDirectory() == null ? false : path.isDirectory(), path.getName(), path.getOwner(),
            path.getPermissions());
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
            .setServerEncrypted(h.isServerEncrypted())
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

    static BlobQuerySerialization toBlobQuerySerialization(FileQuerySerialization ser) {
        if (ser == null) {
            return null;
        }
        if (ser instanceof FileQueryJsonSerialization) {
            FileQueryJsonSerialization jsonSer = (FileQueryJsonSerialization) ser;
            return new BlobQueryJsonSerialization().setRecordSeparator(jsonSer.getRecordSeparator());
        } else if (ser instanceof FileQueryDelimitedSerialization) {
            FileQueryDelimitedSerialization delSer = (FileQueryDelimitedSerialization) ser;
            return new BlobQueryDelimitedSerialization()
                .setColumnSeparator(delSer.getColumnSeparator())
                .setEscapeChar(delSer.getEscapeChar())
                .setFieldQuote(delSer.getFieldQuote())
                .setHeadersPresent(delSer.isHeadersPresent())
                .setRecordSeparator(delSer.getRecordSeparator());
        } else if (ser instanceof FileQueryArrowSerialization) {
            FileQueryArrowSerialization arrSer = (FileQueryArrowSerialization) ser;
            return new BlobQueryArrowSerialization().setSchema(toBlobQueryArrowSchema(arrSer.getSchema()));
        } else if (ser instanceof FileQueryParquetSerialization) {
            return new BlobQueryParquetSerialization();
        } else {
            throw new IllegalArgumentException(SERIALIZATION_MESSAGE);
        }
    }

    private static List<BlobQueryArrowField> toBlobQueryArrowSchema(List<FileQueryArrowField> schema) {
        if (schema == null) {
            return null;
        }
        List<BlobQueryArrowField> blobSchema = new ArrayList<>(schema.size());
        for (FileQueryArrowField field : schema) {
            blobSchema.add(toBlobQueryArrowField(field));
        }
        return blobSchema;
    }

    private static BlobQueryArrowField toBlobQueryArrowField(FileQueryArrowField field) {
        if (field == null) {
            return null;
        }
        return new BlobQueryArrowField(BlobQueryArrowFieldType.fromString(field.getType().toString()))
            .setName(field.getName())
            .setPrecision(field.getPrecision())
            .setScale(field.getScale());
    }

    static Consumer<BlobQueryError> toBlobQueryErrorConsumer(Consumer<FileQueryError> er) {
        if (er == null) {
            return null;
        }
        return error -> er.accept(toFileQueryError(error));
    }

    static Consumer<BlobQueryProgress> toBlobQueryProgressConsumer(Consumer<FileQueryProgress> pr) {
        if (pr == null) {
            return null;
        }
        return progress -> pr.accept(toFileQueryProgress(progress));
    }

    private static FileQueryError toFileQueryError(BlobQueryError error) {
        if (error == null) {
            return null;
        }
        return new FileQueryError(error.isFatal(), error.getName(), error.getDescription(), error.getPosition());
    }

    private static FileQueryProgress toFileQueryProgress(BlobQueryProgress progress) {
        if (progress == null) {
            return null;
        }
        return new FileQueryProgress(progress.getBytesScanned(), progress.getTotalBytes());
    }

    static FileQueryResponse toFileQueryResponse(BlobQueryResponse r) {
        if (r == null) {
            return null;
        }
        return new FileQueryResponse(Transforms.toFileQueryAsyncResponse(new BlobQueryAsyncResponse(r.getRequest(),
            r.getStatusCode(), r.getHeaders(), null, r.getDeserializedHeaders())));
    }

    static FileQueryAsyncResponse toFileQueryAsyncResponse(BlobQueryAsyncResponse r) {
        if (r == null) {
            return null;
        }
        return new FileQueryAsyncResponse(r.getRequest(), r.getStatusCode(), r.getHeaders(), r.getValue(),
            Transforms.toFileQueryHeaders(r.getDeserializedHeaders()));
    }

    private static FileQueryHeaders toFileQueryHeaders(BlobQueryHeaders h) {
        if (h == null) {
            return null;
        }
        return new FileQueryHeaders()
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
            .setServerEncrypted(h.isServerEncrypted())
            .setEncryptionKeySha256(h.getEncryptionKeySha256())
            .setFileContentMd5(h.getContentMd5())
            .setContentCrc64(h.getContentCrc64())
            .setErrorCode(h.getErrorCode());
    }

    static BlobQueryOptions toBlobQueryOptions(FileQueryOptions options) {
        if (options == null) {
            return null;
        }
        if (options.getOutputStream() == null) {
            return new BlobQueryOptions(options.getExpression())
                .setInputSerialization(Transforms.toBlobQuerySerialization(options.getInputSerialization()))
                .setOutputSerialization(Transforms.toBlobQuerySerialization(options.getOutputSerialization()))
                .setRequestConditions(Transforms.toBlobRequestConditions(options.getRequestConditions()))
                .setErrorConsumer(Transforms.toBlobQueryErrorConsumer(options.getErrorConsumer()))
                .setProgressConsumer(Transforms.toBlobQueryProgressConsumer(options.getProgressConsumer()));
        } else {
            return new BlobQueryOptions(options.getExpression(), options.getOutputStream())
                .setInputSerialization(Transforms.toBlobQuerySerialization(options.getInputSerialization()))
                .setOutputSerialization(Transforms.toBlobQuerySerialization(options.getOutputSerialization()))
                .setRequestConditions(Transforms.toBlobRequestConditions(options.getRequestConditions()))
                .setErrorConsumer(Transforms.toBlobQueryErrorConsumer(options.getErrorConsumer()))
                .setProgressConsumer(Transforms.toBlobQueryProgressConsumer(options.getProgressConsumer()));
        }

    }

//    static BlobContainerRenameOptions toBlobContainerRenameOptions(FileSystemRenameOptions options) {
//        if (options == null) {
//            return null;
//        }
//        return new BlobContainerRenameOptions(options.getDestinationFileSystemName())
//            .setRequestConditions(toBlobRequestConditions(options.getRequestConditions()));
//    }

    static UndeleteBlobContainerOptions toBlobContainerUndeleteOptions(FileSystemUndeleteOptions options) {
        if (options == null) {
            return null;
        }
        return new UndeleteBlobContainerOptions(options.getDeletedFileSystemName(),
            options.getDeletedFileSystemVersion()).setDestinationContainerName(options.getDestinationFileSystemName());
    }

    static DataLakeServiceProperties toDataLakeServiceProperties(BlobServiceProperties blobProps) {
        if (blobProps == null) {
            return null;
        }

        return new DataLakeServiceProperties()
            .setDefaultServiceVersion(blobProps.getDefaultServiceVersion())
            .setCors(blobProps.getCors().stream().map(Transforms::toDataLakeCorsRule).collect(Collectors.toList()))
            .setDeleteRetentionPolicy(toDataLakeRetentionPolicy(blobProps.getDeleteRetentionPolicy()))
            .setHourMetrics(toDataLakeMetrics(blobProps.getHourMetrics()))
            .setMinuteMetrics(toDataLakeMetrics(blobProps.getMinuteMetrics()))
            .setLogging(toDataLakeAnalyticsLogging(blobProps.getLogging()))
            .setStaticWebsite(toDataLakeStaticWebsite(blobProps.getStaticWebsite()));
    }

    static DataLakeStaticWebsite toDataLakeStaticWebsite(StaticWebsite staticWebsite) {
        if (staticWebsite == null) {
            return null;
        }

        return new DataLakeStaticWebsite()
            .setDefaultIndexDocumentPath(staticWebsite.getDefaultIndexDocumentPath())
            .setEnabled(staticWebsite.isEnabled())
            .setErrorDocument404Path(staticWebsite.getErrorDocument404Path())
            .setIndexDocument(staticWebsite.getIndexDocument());
    }

    static DataLakeAnalyticsLogging toDataLakeAnalyticsLogging(BlobAnalyticsLogging blobLogging) {
        if (blobLogging == null) {
            return null;
        }

        return new DataLakeAnalyticsLogging()
            .setDelete(blobLogging.isDelete())
            .setRead(blobLogging.isRead())
            .setWrite(blobLogging.isWrite())
            .setRetentionPolicy(toDataLakeRetentionPolicy(blobLogging.getRetentionPolicy()))
            .setVersion(blobLogging.getVersion());
    }

    static DataLakeCorsRule toDataLakeCorsRule(BlobCorsRule blobRule) {
        if (blobRule == null) {
            return null;
        }

        return new DataLakeCorsRule()
            .setAllowedHeaders(blobRule.getAllowedHeaders())
            .setAllowedMethods(blobRule.getAllowedMethods())
            .setAllowedOrigins(blobRule.getAllowedOrigins())
            .setExposedHeaders(blobRule.getExposedHeaders())
            .setMaxAgeInSeconds(blobRule.getMaxAgeInSeconds());
    }

    static DataLakeMetrics toDataLakeMetrics(BlobMetrics blobMetrics) {
        if (blobMetrics == null) {
            return null;
        }

        return new DataLakeMetrics()
            .setEnabled(blobMetrics.isEnabled())
            .setIncludeApis(blobMetrics.isIncludeApis())
            .setVersion(blobMetrics.getVersion())
            .setRetentionPolicy(toDataLakeRetentionPolicy(blobMetrics.getRetentionPolicy()));
    }

    static DataLakeRetentionPolicy toDataLakeRetentionPolicy(BlobRetentionPolicy blobPolicy) {
        if (blobPolicy == null) {
            return null;
        }

        return new DataLakeRetentionPolicy()
            .setDays(blobPolicy.getDays())
            .setEnabled(blobPolicy.isEnabled());
    }

    static BlobServiceProperties toBlobServiceProperties(DataLakeServiceProperties datalakeProperties) {
        if (datalakeProperties == null) {
            return null;
        }

        return new BlobServiceProperties()
            .setDefaultServiceVersion(datalakeProperties.getDefaultServiceVersion())
            .setCors(datalakeProperties.getCors().stream().map(Transforms::toBlobCorsRule).collect(Collectors.toList()))
            .setDeleteRetentionPolicy(toBlobRetentionPolicy(datalakeProperties.getDeleteRetentionPolicy()))
            .setHourMetrics(toBlobMetrics(datalakeProperties.getHourMetrics()))
            .setMinuteMetrics(toBlobMetrics(datalakeProperties.getMinuteMetrics()))
            .setLogging(toBlobAnalyticsLogging(datalakeProperties.getLogging()))
            .setStaticWebsite(toBlobStaticWebsite(datalakeProperties.getStaticWebsite()));
    }

    static StaticWebsite toBlobStaticWebsite(DataLakeStaticWebsite staticWebsite) {
        if (staticWebsite == null) {
            return null;
        }

        return new StaticWebsite()
            .setDefaultIndexDocumentPath(staticWebsite.getDefaultIndexDocumentPath())
            .setEnabled(staticWebsite.isEnabled())
            .setErrorDocument404Path(staticWebsite.getErrorDocument404Path())
            .setIndexDocument(staticWebsite.getIndexDocument());
    }

    static BlobAnalyticsLogging toBlobAnalyticsLogging(DataLakeAnalyticsLogging datalakeLogging) {
        if (datalakeLogging == null) {
            return null;
        }

        return new BlobAnalyticsLogging()
            .setDelete(datalakeLogging.isDelete())
            .setRead(datalakeLogging.isRead())
            .setWrite(datalakeLogging.isWrite())
            .setRetentionPolicy(toBlobRetentionPolicy(datalakeLogging.getRetentionPolicy()))
            .setVersion(datalakeLogging.getVersion());
    }

    static BlobCorsRule toBlobCorsRule(DataLakeCorsRule datalakeRule) {
        if (datalakeRule == null) {
            return null;
        }

        return new BlobCorsRule()
            .setAllowedHeaders(datalakeRule.getAllowedHeaders())
            .setAllowedMethods(datalakeRule.getAllowedMethods())
            .setAllowedOrigins(datalakeRule.getAllowedOrigins())
            .setExposedHeaders(datalakeRule.getExposedHeaders())
            .setMaxAgeInSeconds(datalakeRule.getMaxAgeInSeconds());
    }

    static BlobMetrics toBlobMetrics(DataLakeMetrics datalakeMetrics) {
        if (datalakeMetrics == null) {
            return null;
        }

        return new BlobMetrics()
            .setEnabled(datalakeMetrics.isEnabled())
            .setIncludeApis(datalakeMetrics.isIncludeApis())
            .setVersion(datalakeMetrics.getVersion())
            .setRetentionPolicy(toBlobRetentionPolicy(datalakeMetrics.getRetentionPolicy()));
    }

    static BlobRetentionPolicy toBlobRetentionPolicy(DataLakeRetentionPolicy datalakePolicy) {
        if (datalakePolicy == null) {
            return null;
        }

        return new BlobRetentionPolicy()
            .setDays(datalakePolicy.getDays())
            .setEnabled(datalakePolicy.isEnabled());
    }

    static PathDeletedItem toPathDeletedItem(BlobItemInternal blobItem) {
        if (blobItem == null) {
            return null;
        }
        return new PathDeletedItem(blobItem.getName(), false, blobItem.getDeletionId(),
            blobItem.getProperties().getDeletedTime(), blobItem.getProperties().getRemainingRetentionDays());
    }

    static PathDeletedItem toPathDeletedItem(BlobPrefix blobPrefix) {
        return new PathDeletedItem(blobPrefix.getName(), true, null, null, null);
    }
}
