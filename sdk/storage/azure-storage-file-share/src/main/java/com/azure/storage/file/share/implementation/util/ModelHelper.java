// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.implementation.MessageConstants;
import com.azure.storage.file.share.implementation.accesshelpers.FileSmbPropertiesHelper;
import com.azure.storage.file.share.implementation.accesshelpers.ShareFileDownloadHeadersConstructorProxy;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.DirectoriesCreateHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesSetMetadataHeaders;
import com.azure.storage.file.share.implementation.models.DirectoriesSetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.FileProperty;
import com.azure.storage.file.share.implementation.models.FilesCreateHeaders;
import com.azure.storage.file.share.implementation.models.FilesDownloadHeaders;
import com.azure.storage.file.share.implementation.models.FilesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.FilesSetHttpHeadersHeaders;
import com.azure.storage.file.share.implementation.models.FilesSetMetadataHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeFromURLHeaders;
import com.azure.storage.file.share.implementation.models.FilesUploadRangeHeaders;
import com.azure.storage.file.share.implementation.models.InternalShareFileItemProperties;
import com.azure.storage.file.share.implementation.models.ListFilesAndDirectoriesSegmentResponse;
import com.azure.storage.file.share.implementation.models.ServicesListSharesSegmentHeaders;
import com.azure.storage.file.share.implementation.models.ShareItemInternal;
import com.azure.storage.file.share.implementation.models.SharePropertiesInternal;
import com.azure.storage.file.share.implementation.models.ShareStats;
import com.azure.storage.file.share.implementation.models.SharesCreateSnapshotHeaders;
import com.azure.storage.file.share.implementation.models.SharesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.StringEncoded;
import com.azure.storage.file.share.models.CopyStatusType;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.HandleItem;
import com.azure.storage.file.share.models.LeaseDurationType;
import com.azure.storage.file.share.models.LeaseStateType;
import com.azure.storage.file.share.models.LeaseStatusType;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareDirectoryInfo;
import com.azure.storage.file.share.models.ShareDirectoryProperties;
import com.azure.storage.file.share.models.ShareDirectorySetMetadataInfo;
import com.azure.storage.file.share.models.ShareErrorCode;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileItem;
import com.azure.storage.file.share.models.ShareFileItemProperties;
import com.azure.storage.file.share.models.ShareFileMetadataInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.azure.storage.file.share.models.ShareFileUploadRangeFromUrlInfo;
import com.azure.storage.file.share.models.ShareInfo;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareSignedIdentifier;
import com.azure.storage.file.share.models.ShareSnapshotInfo;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;
import com.azure.storage.file.share.models.ShareStatistics;
import com.azure.storage.file.share.models.ShareStorageException;
import com.azure.storage.file.share.options.ShareFileCopyOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.azure.core.http.HttpHeaderName.LAST_MODIFIED;

public class ModelHelper {
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    private static final long MAX_FILE_PUT_RANGE_BYTES = 4 * Constants.MB;
    private static final int FILE_DEFAULT_NUMBER_OF_BUFFERS = 8;
    public static final long FILE_DEFAULT_BLOCK_SIZE = 4 * 1024 * 1024L;
    public static final long FILE_MAX_PUT_RANGE_SIZE = 4 * Constants.MB;

    private static final HttpHeaderName X_MS_ERROR_CODE = HttpHeaderName.fromString("x-ms-error-code");

    /**
     * Fills in default values for a ParallelTransferOptions where no value has been set. This will construct a new
     * object for safety.
     *
     * @param other The options to fill in defaults.
     * @return An object with defaults filled in for null values in the original.
     */
    public static ParallelTransferOptions populateAndApplyDefaults(ParallelTransferOptions other) {
        other = other == null ? new ParallelTransferOptions() : other;

        // For now these two checks are useful for when we transition to
        if (other.getBlockSizeLong() != null) {
            StorageImplUtils.assertInBounds("ParallelTransferOptions.blockSize", other.getBlockSizeLong(), 1,
                MAX_FILE_PUT_RANGE_BYTES);
        }

        if (other.getMaxSingleUploadSizeLong() != null) {
            StorageImplUtils.assertInBounds("ParallelTransferOptions.maxSingleUploadSize",
                other.getMaxSingleUploadSizeLong(), 1, MAX_FILE_PUT_RANGE_BYTES);
        }

        Long blockSize = other.getBlockSizeLong();
        if (blockSize == null) {
            blockSize = MAX_FILE_PUT_RANGE_BYTES;
        }

        Integer maxConcurrency = other.getMaxConcurrency();
        if (maxConcurrency == null) {
            maxConcurrency = FILE_DEFAULT_NUMBER_OF_BUFFERS;
        }

        Long maxSingleUploadSize = other.getMaxSingleUploadSizeLong();
        if (maxSingleUploadSize == null) {
            maxSingleUploadSize = MAX_FILE_PUT_RANGE_BYTES;
        }

        return new ParallelTransferOptions()
            .setBlockSizeLong(blockSize)
            .setMaxConcurrency(maxConcurrency)
            .setProgressListener(other.getProgressListener())
            .setMaxSingleUploadSizeLong(maxSingleUploadSize);
    }

    /**
     * Converts an internal type to a public type.
     *
     * @param option {@link ShareSnapshotsDeleteOptionType}
     * @return {@link DeleteSnapshotsOptionType}
     */
    public static DeleteSnapshotsOptionType toDeleteSnapshotsOptionType(ShareSnapshotsDeleteOptionType option) {
        if (option == null) {
            return null;
        }
        switch (option) {
            case INCLUDE:
                return DeleteSnapshotsOptionType.INCLUDE;
            case INCLUDE_WITH_LEASED:
                return DeleteSnapshotsOptionType.INCLUDE_LEASED;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid " + option.getClass()));
        }
    }

    /**
     * Transforms {@link ShareItemInternal} into a public {@link ShareItem}.
     *
     * @param shareItemInternal {@link ShareItemInternal}
     * @return {@link ShareItem}
     */
    public static ShareItem populateShareItem(ShareItemInternal shareItemInternal) {
        ShareItem item = new ShareItem();
        item.setName(shareItemInternal.getName());
        item.setSnapshot(shareItemInternal.getSnapshot());
        item.setDeleted(shareItemInternal.isDeleted());
        item.setVersion(shareItemInternal.getVersion());
        item.setProperties(populateShareProperties(shareItemInternal.getProperties()));
        item.setMetadata(shareItemInternal.getMetadata());
        return item;
    }

    /**
     * Transforms {@link SharePropertiesInternal} into a public {@link ShareProperties}.
     *
     * @param sharePropertiesInternal {@link SharePropertiesInternal}
     * @return {@link ShareProperties}
     */
    public static ShareProperties populateShareProperties(SharePropertiesInternal sharePropertiesInternal) {
        ShareProperties properties = new ShareProperties();
        properties.setLastModified(sharePropertiesInternal.getLastModified());
        properties.setETag(sharePropertiesInternal.getETag());
        properties.setQuota(sharePropertiesInternal.getQuota());
        properties.setProvisionedIops(sharePropertiesInternal.getProvisionedIops());
        properties.setProvisionedIngressMBps(sharePropertiesInternal.getProvisionedIngressMBps());
        properties.setProvisionedEgressMBps(sharePropertiesInternal.getProvisionedEgressMBps());
        properties.setNextAllowedQuotaDowngradeTime(sharePropertiesInternal.getNextAllowedQuotaDowngradeTime());
        properties.setDeletedTime(sharePropertiesInternal.getDeletedTime());
        properties.setRemainingRetentionDays(sharePropertiesInternal.getRemainingRetentionDays());
        properties.setAccessTier(sharePropertiesInternal.getAccessTier());
        properties.setAccessTierChangeTime(sharePropertiesInternal.getAccessTierChangeTime());
        properties.setAccessTierTransitionState(sharePropertiesInternal.getAccessTierTransitionState());
        properties.setLeaseStatus(sharePropertiesInternal.getLeaseStatus());
        properties.setLeaseState(sharePropertiesInternal.getLeaseState());
        properties.setLeaseDuration(sharePropertiesInternal.getLeaseDuration());
        properties.setProtocols(parseShareProtocols(sharePropertiesInternal.getEnabledProtocols()));
        properties.setRootSquash(sharePropertiesInternal.getRootSquash());
        properties.setMetadata(sharePropertiesInternal.getMetadata());
        properties.setProvisionedBandwidthMiBps(sharePropertiesInternal.getProvisionedBandwidthMiBps());
        properties.setSnapshotVirtualDirectoryAccessEnabled(sharePropertiesInternal.isEnableSnapshotVirtualDirectoryAccess());
        properties.setEnablePaidBursting(sharePropertiesInternal.isPaidBurstingEnabled());
        properties.setPaidBurstingMaxIops(sharePropertiesInternal.getPaidBurstingMaxIops());
        properties.setPaidBurstingMaxBandwidthMibps(sharePropertiesInternal.getPaidBurstingMaxBandwidthMibps());

        return properties;
    }

    /**
     * Parses a {@code String} into a {@code ShareProtocols}. Unrecognized protocols will be ignored.
     *
     * @param str The string to parse.
     * @return A {@code ShareProtocols} represented by the string.
     */
    public static ShareProtocols parseShareProtocols(String str) {
        if (str == null) {
            return null;
        }

        ShareProtocols protocols = new ShareProtocols();
        for (String s : str.split(",")) {
            switch (s) {
                case Constants.HeaderConstants.SMB_PROTOCOL:
                    protocols.setSmbEnabled(true);
                    break;
                case Constants.HeaderConstants.NFS_PROTOCOL:
                    protocols.setNfsEnabled(true);
                    break;
                default:
                    // Ignore unknown options
            }
        }
        return protocols;
    }

    public static ServicesListSharesSegmentHeaders transformListSharesHeaders(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }

        return new ServicesListSharesSegmentHeaders(headers);
    }

    public static ShareFileDownloadHeaders transformFileDownloadHeaders(FilesDownloadHeaders headers,
        HttpHeaders rawHeaders) {
        if (headers == null) {
            return null;
        }

        return ShareFileDownloadHeadersConstructorProxy.create(headers)
            .setErrorCode(rawHeaders.getValue(X_MS_ERROR_CODE));
    }

    public static String getETag(HttpHeaders headers) {
        return headers.getValue(HttpHeaderName.ETAG);
    }

    public static ShareFileItemProperties transformFileProperty(FileProperty property) {
        if (property == null) {
            return null;
        }
        return new InternalShareFileItemProperties(property.getCreationTime(), property.getLastAccessTime(),
            property.getLastWriteTime(), property.getChangeTime(), property.getLastModified(), property.getEtag());
    }

    public static HandleItem transformHandleItem(com.azure.storage.file.share.implementation.models.HandleItem handleItem) {
        return new HandleItem()
            .setHandleId(handleItem.getHandleId())
            .setPath(decodeName(handleItem.getPath())) // handles decoding path if path is encoded
            .setSessionId(handleItem.getSessionId())
            .setClientIp(handleItem.getClientIp())
            .setFileId(handleItem.getFileId())
            .setParentId(handleItem.getParentId())
            .setLastReconnectTime(handleItem.getLastReconnectTime())
            .setOpenTime(handleItem.getOpenTime())
            .setAccessRights(handleItem.getAccessRightList())
            .setClientName(handleItem.getClientName());
    }

    public static List<HandleItem> transformHandleItems(List<com.azure.storage.file.share.implementation.models.HandleItem> handleItems) {
        List<HandleItem> result = new ArrayList<>();
        handleItems.forEach(item -> {
            result.add(transformHandleItem(item));
        });
        return result;
    }

    public static String decodeName(StringEncoded stringEncoded) {
        if (stringEncoded.isEncoded() != null && stringEncoded.isEncoded()) {
            try {
                return URLDecoder.decode(stringEncoded.getContent(), StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        } else {
            return stringEncoded.getContent();
        }
    }

    /**
     * Verifies that the file permission and file permission key are not both set and if the file permission is set,
     * the file permission is of valid length.
     * @param filePermission The file permission.
     * @param filePermissionKey The file permission key.
     * @throws IllegalArgumentException for invalid file permission or file permission keys.
     */
    public static void validateFilePermissionAndKey(String filePermission, String  filePermissionKey) {
        if (filePermission != null && filePermissionKey != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                MessageConstants.FILE_PERMISSION_FILE_PERMISSION_KEY_INVALID));
        }

        if (filePermission != null) {
            StorageImplUtils.assertInBounds("filePermission",
                filePermission.getBytes(StandardCharsets.UTF_8).length, 0, 8 * Constants.KB);
        }
    }

    public static boolean checkDoesNotExistStatusCode(Throwable t) {
        if (t instanceof ShareStorageException) {
            ShareStorageException s = (ShareStorageException) t;
            return s.getStatusCode() == 404
                && (s.getErrorCode() == ShareErrorCode.RESOURCE_NOT_FOUND
                || s.getErrorCode() == ShareErrorCode.SHARE_NOT_FOUND);
            /* HttpResponseException - file get properties is a head request so a body is not returned. Error
             conversion logic does not properly handle errors that don't return XML. */
        } else if (t instanceof HttpResponseException) {
            HttpResponseException h = (HttpResponseException) t;
            String errorCode = h.getResponse().getHeaderValue(X_MS_ERROR_CODE);
            return h.getResponse().getStatusCode() == 404
                && (ShareErrorCode.RESOURCE_NOT_FOUND.toString().equals(errorCode)
                || ShareErrorCode.SHARE_NOT_FOUND.toString().equals(errorCode));
        } else {
            return false;
        }
    }

    public static Response<ShareFileInfo> createFileInfoResponse(ResponseBase<FilesCreateHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    public static Response<ShareFileProperties> getPropertiesResponse(
        final ResponseBase<FilesGetPropertiesHeaders, Void> response) {
        FilesGetPropertiesHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Map<String, String> metadata = headers.getXMsMeta();
        String fileType = headers.getXMsType();
        Long contentLength = headers.getContentLength();
        String contentType = headers.getContentType();
        byte[] contentMD5 = headers.getContentMD5();
//        try {
//            contentMD5 = headers.getContentMD5();
//        } catch (NullPointerException e) {
//            contentMD5 = null;
//        }
        String contentEncoding = headers.getContentEncoding();
        String cacheControl = headers.getCacheControl();
        String contentDisposition = headers.getContentDisposition();
        LeaseStatusType leaseStatusType = headers.getXMsLeaseStatus();
        LeaseStateType leaseStateType = headers.getXMsLeaseState();
        LeaseDurationType leaseDurationType = headers.getXMsLeaseDuration();
        OffsetDateTime copyCompletionTime = headers.getXMsCopyCompletionTime();
        String copyStatusDescription = headers.getXMsCopyStatusDescription();
        String copyId = headers.getXMsCopyId();
        String copyProgress = headers.getXMsCopyProgress();
        String copySource = headers.getXMsCopySource();
        CopyStatusType copyStatus = headers.getXMsCopyStatus();
        Boolean isServerEncrypted = headers.isXMsServerEncrypted();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareFileProperties shareFileProperties = new ShareFileProperties(eTag, lastModified, metadata, fileType,
            contentLength, contentType, contentMD5, contentEncoding, cacheControl, contentDisposition,
            leaseStatusType, leaseStateType, leaseDurationType, copyCompletionTime, copyStatusDescription, copyId,
            copyProgress, copySource, copyStatus, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileProperties);
    }

    public static Response<ShareFileInfo> setPropertiesResponse(
        final ResponseBase<FilesSetHttpHeadersHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareFileInfo shareFileInfo = new ShareFileInfo(eTag, lastModified, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareFileInfo);
    }

    public static Response<ShareFileMetadataInfo> setMetadataResponse(
        final ResponseBase<FilesSetMetadataHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        Boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        ShareFileMetadataInfo shareFileMetadataInfo = new ShareFileMetadataInfo(eTag, isServerEncrypted);
        return new SimpleResponse<>(response, shareFileMetadataInfo);
    }

    public static void validateCopyFlagAndSmbProperties(ShareFileCopyOptions options,
                                                        FileSmbProperties tempSmbProperties) {
        // check if only copy flag or smb properties are set (not both)
        CopyableFileSmbPropertiesList list = options.getSmbPropertiesToCopy()  == null
            ? new CopyableFileSmbPropertiesList() : options.getSmbPropertiesToCopy();
        if (list.isFileAttributes() && tempSmbProperties.getNtfsFileAttributes() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetFileAttributes and smbProperties.ntfsFileAttributes cannot be set."));
        }
        if (list.isCreatedOn() && tempSmbProperties.getFileCreationTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetCreatedOn and smbProperties.fileCreationTime cannot be set."));
        }
        if (list.isLastWrittenOn() && tempSmbProperties.getFileLastWriteTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetLastWrittenOn and smbProperties.fileLastWriteTime cannot be set."));
        }
        if (list.isChangedOn() && tempSmbProperties.getFileChangeTime() != null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Both CopyableFileSmbPropertiesList.isSetChangedOn and smbProperties.fileChangeTime cannot be set."));
        }
    }

    public static Response<ShareFileUploadInfo> transformUploadResponse(ResponseBase<FilesUploadRangeHeaders, Void> response) {
        FilesUploadRangeHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        byte[] contentMD5 = headers.getContentMD5();
//        try {
//            contentMD5 = headers.getContentMD5();
//        } catch (NullPointerException e) {
//            contentMD5 = null;
//        }
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadInfo shareFileUploadInfo = new ShareFileUploadInfo(eTag, lastModified, contentMD5,
            isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadInfo);
    }

    public static Response<ShareInfo> mapToShareInfoResponse(Response<?> response) {
        String eTag = getETag(response.getHeaders());
        OffsetDateTime lastModified = new DateTimeRfc1123(response.getHeaders().getValue(LAST_MODIFIED)).getDateTime();
        return new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            new ShareInfo(eTag, lastModified));
    }

    public static Response<ShareProperties> mapGetPropertiesResponse(ResponseBase<SharesGetPropertiesHeaders, Void> response) {
        SharesGetPropertiesHeaders headers = response.getDeserializedHeaders();
        ShareProperties shareProperties = new ShareProperties()
            .setETag(headers.getETag())
            .setLastModified(headers.getLastModified())
            .setMetadata(headers.getXMsMeta())
            .setQuota(headers.getXMsShareQuota())
            .setNextAllowedQuotaDowngradeTime(headers.getXMsShareNextAllowedQuotaDowngradeTime())
            .setProvisionedEgressMBps(headers.getXMsShareProvisionedEgressMbps())
            .setProvisionedIngressMBps(headers.getXMsShareProvisionedIngressMbps())
            .setProvisionedBandwidthMiBps(headers.getXMsShareProvisionedBandwidthMibps())
            .setProvisionedIops(headers.getXMsShareProvisionedIops())
            .setLeaseDuration(headers.getXMsLeaseDuration())
            .setLeaseState(headers.getXMsLeaseState())
            .setLeaseStatus(headers.getXMsLeaseStatus())
            .setAccessTier(headers.getXMsAccessTier())
            .setAccessTierChangeTime(headers.getXMsAccessTierChangeTime())
            .setAccessTierTransitionState(headers.getXMsAccessTierTransitionState())
            .setProtocols(ModelHelper.parseShareProtocols(headers.getXMsEnabledProtocols()))
            .setSnapshotVirtualDirectoryAccessEnabled(headers.isXMsEnableSnapshotVirtualDirectoryAccess())
            .setEnablePaidBursting(headers.isXMsSharePaidBurstingEnabled())
            .setPaidBurstingMaxIops(headers.getXMsSharePaidBurstingMaxIops())
            .setPaidBurstingMaxBandwidthMibps(headers.getXMsSharePaidBurstingMaxBandwidthMibps())
            .setRootSquash(headers.getXMsRootSquash());

        return new SimpleResponse<>(response, shareProperties);
    }

    public static Response<ShareStatistics> mapGetStatisticsResponse(Response<ShareStats> response) {
        return new SimpleResponse<>(response, new ShareStatistics(response.getValue().getShareUsageBytes()));
    }

    public static Response<ShareFileUploadInfo> uploadRangeHeadersToShareFileInfo(ResponseBase<FilesUploadRangeHeaders, Void> response) {
        FilesUploadRangeHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        byte[] contentMD5 = headers.getContentMD5();
//        try {
//            contentMD5 = headers.getContentMD5();
//        } catch (NullPointerException e) {
//            contentMD5 = null;
//        }
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadInfo shareFileUploadInfo = new ShareFileUploadInfo(eTag, lastModified, contentMD5,
            isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadInfo);
    }

    public static Response<ShareFileUploadRangeFromUrlInfo> mapUploadRangeFromUrlResponse(
        final ResponseBase<FilesUploadRangeFromURLHeaders, Void> response) {
        FilesUploadRangeFromURLHeaders headers = response.getDeserializedHeaders();
        String eTag = headers.getETag();
        OffsetDateTime lastModified = headers.getLastModified();
        Boolean isServerEncrypted = headers.isXMsRequestServerEncrypted();
        ShareFileUploadRangeFromUrlInfo shareFileUploadRangeFromUrlInfo =
            new ShareFileUploadRangeFromUrlInfo(eTag, lastModified, isServerEncrypted);
        return new SimpleResponse<>(response, shareFileUploadRangeFromUrlInfo);
    }

    public static Response<ShareSnapshotInfo> mapCreateSnapshotResponse(
        ResponseBase<SharesCreateSnapshotHeaders, Void> response) {
        SharesCreateSnapshotHeaders headers = response.getDeserializedHeaders();
        ShareSnapshotInfo snapshotInfo =
            new ShareSnapshotInfo(headers.getXMsSnapshot(), headers.getETag(), headers.getLastModified());

        return new SimpleResponse<>(response, snapshotInfo);
    }

    public static Response<ShareDirectoryInfo> mapShareDirectoryInfo(
        final ResponseBase<DirectoriesCreateHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareDirectoryInfo shareDirectoryInfo = new ShareDirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryInfo);
    }

    public static Response<ShareDirectoryProperties> mapShareDirectoryPropertiesResponse(
        ResponseBase<DirectoriesGetPropertiesHeaders, Void> response) {
        Map<String, String> metadata = response.getDeserializedHeaders().getXMsMeta();
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime offsetDateTime = response.getDeserializedHeaders().getLastModified();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsServerEncrypted();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareDirectoryProperties shareDirectoryProperties =
            new ShareDirectoryProperties(metadata, eTag, offsetDateTime, isServerEncrypted, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryProperties);
    }

    public static Response<ShareDirectoryInfo> mapSetPropertiesResponse(
        final ResponseBase<DirectoriesSetPropertiesHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        OffsetDateTime lastModified = response.getDeserializedHeaders().getLastModified();
        FileSmbProperties smbProperties = FileSmbPropertiesHelper.create(response.getHeaders());
        ShareDirectoryInfo shareDirectoryInfo = new ShareDirectoryInfo(eTag, lastModified, smbProperties);
        return new SimpleResponse<>(response, shareDirectoryInfo);
    }

    public static Response<ShareDirectorySetMetadataInfo> setShareDirectoryMetadataResponse(
        final ResponseBase<DirectoriesSetMetadataHeaders, Void> response) {
        String eTag = response.getDeserializedHeaders().getETag();
        boolean isServerEncrypted = response.getDeserializedHeaders().isXMsRequestServerEncrypted();
        ShareDirectorySetMetadataInfo shareDirectorySetMetadataInfo = new ShareDirectorySetMetadataInfo(eTag,
            isServerEncrypted);
        return new SimpleResponse<>(response, shareDirectorySetMetadataInfo);
    }

    public static List<ShareFileItem> convertResponseAndGetNumOfResults(
        Response<ListFilesAndDirectoriesSegmentResponse> res) {
        Set<ShareFileItem> shareFileItems = new TreeSet<>(Comparator.comparing(ShareFileItem::getName));
        if (res.getValue().getSegment() != null) {

            res.getValue().getSegment().getDirectoryItems()
                .forEach(directoryItem -> shareFileItems.add(new ShareFileItem(
                    ModelHelper.decodeName(directoryItem.getName()), true, directoryItem.getFileId(),
                    ModelHelper.transformFileProperty(directoryItem.getProperties()),
                    NtfsFileAttributes.toAttributes(directoryItem.getAttributes()), directoryItem.getPermissionKey(),
                    null)));

            res.getValue().getSegment().getFileItems()
                .forEach(fileItem -> shareFileItems.add(new ShareFileItem(ModelHelper.decodeName(fileItem.getName()),
                    false, fileItem.getFileId(), ModelHelper.transformFileProperty(fileItem.getProperties()),
                    NtfsFileAttributes.toAttributes(fileItem.getAttributes()), fileItem.getPermissionKey(),
                    fileItem.getProperties().getContentLength())));
        }

        return new ArrayList<>(shareFileItems);
    }

    public static List<ShareSignedIdentifier> truncateAccessPolicyPermissionsToSeconds(
        List<ShareSignedIdentifier> permissions) {
        /*
        We truncate to seconds because the service only supports nanoseconds or seconds, but doing an
        OffsetDateTime.now will only give back milliseconds (more precise fields are zeroed and not serialized). This
        allows for proper serialization with no real detriment to users as sub-second precision on active time for
        signed identifiers is not really necessary.
         */
        if (permissions != null) {
            for (ShareSignedIdentifier permission : permissions) {
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getStartsOn() != null) {
                    permission.getAccessPolicy().setStartsOn(
                        permission.getAccessPolicy().getStartsOn().truncatedTo(ChronoUnit.SECONDS));
                }
                if (permission.getAccessPolicy() != null && permission.getAccessPolicy().getExpiresOn() != null) {
                    permission.getAccessPolicy().setExpiresOn(
                        permission.getAccessPolicy().getExpiresOn().truncatedTo(ChronoUnit.SECONDS));
                }
            }
        }
        return permissions;
    }

    public static LongRunningOperationStatus mapStatusToLongRunningOperationStatus(CopyStatusType status) {
        LongRunningOperationStatus operationStatus;
        switch (status) {
            case SUCCESS:
                operationStatus = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
                break;
            case FAILED:
                operationStatus = LongRunningOperationStatus.FAILED;
                break;
            case ABORTED:
                operationStatus = LongRunningOperationStatus.USER_CANCELLED;
                break;
            case PENDING:
                operationStatus = LongRunningOperationStatus.IN_PROGRESS;
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "CopyStatusType is not supported. Status: " + status));
        }
        return operationStatus;
    }
}
