// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.storage.common.ParallelTransferOptions;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.FileProperty;
import com.azure.storage.file.share.implementation.models.InternalShareFileItemProperties;
import com.azure.storage.file.share.implementation.models.ServicesListSharesSegmentHeaders;
import com.azure.storage.file.share.implementation.models.ShareItemInternal;
import com.azure.storage.file.share.implementation.models.SharePropertiesInternal;
import com.azure.storage.file.share.models.ShareFileDownloadHeaders;
import com.azure.storage.file.share.models.ShareFileItemProperties;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;

import java.io.IOException;

public class ModelHelper {

    private static final SerializerAdapter SERIALIZER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final ClientLogger LOGGER = new ClientLogger(ModelHelper.class);

    private static final long MAX_FILE_PUT_RANGE_BYTES = 4 * Constants.MB;
    private static final int FILE_DEFAULT_NUMBER_OF_BUFFERS = 8;

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
            .setProgressReceiver(other.getProgressReceiver())
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
        try {
            return SERIALIZER.deserialize(headers, ServicesListSharesSegmentHeaders.class);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public static ShareFileDownloadHeaders transformFileDownloadHeaders(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        try {
            return SERIALIZER.deserialize(headers, ShareFileDownloadHeaders.class);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public static String getETag(HttpHeaders headers) {
        return headers.getValue("ETag");
    }

    public static ShareFileItemProperties transformFileProperty(FileProperty property) {
        if (property == null) {
            return null;
        }
        return new InternalShareFileItemProperties(property.getCreationTime(), property.getLastAccessTime(),
            property.getLastWriteTime(), property.getChangeTime(), property.getLastModified(), property.getEtag());
    }
}
