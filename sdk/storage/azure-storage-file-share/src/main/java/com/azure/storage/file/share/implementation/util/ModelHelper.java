// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.implementation.models.ShareItemInternal;
import com.azure.storage.file.share.implementation.models.SharePropertiesInternal;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareItem;
import com.azure.storage.file.share.models.ShareProperties;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;

public class ModelHelper {

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
                throw new IllegalArgumentException("Invalid " + option.getClass());
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
}
