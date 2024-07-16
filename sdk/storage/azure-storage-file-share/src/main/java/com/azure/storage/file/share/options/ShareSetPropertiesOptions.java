// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareRootSquash;

/**
 * Extended options that may be passed when setting properties on a share.
 */
@Fluent
public class ShareSetPropertiesOptions {

    private Integer quotaInGb;
    private ShareAccessTier accessTier;
    private ShareRootSquash rootSquash;
    private ShareRequestConditions requestConditions;
    private Boolean enableSnapshotVirtualDirectoryAccess;

    /**
     * @return {@link ShareAccessTier}
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

    /**
     * @return Size in GB to limit the share's growth.
     */
    public Integer getQuotaInGb() {
        return quotaInGb;
    }

    /**
     * @param quotaInGb Size in GB to limit the share's growth.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
        return this;
    }

    /**
     * @return The root squash to set for the share. Only valid for NFS.
     */
    public ShareRootSquash getRootSquash() {
        return rootSquash;
    }

    /**
     * @param rootSquash The root squash to set for the share. Only valid for NFS.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setRootSquash(ShareRootSquash rootSquash) {
        this.rootSquash = rootSquash;
        return this;
    }

    /**
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions}.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    /**
     * Get the enableSnapshotVirtualDirectoryAccess property: The EnableSnapshotVirtualDirectoryAccess property.
     * Optional. Supported in version 2023-08-03 and above. Only applicable for premium file storage accounts.
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS is enabled.
     * If not specified, the default is true.
     *
     * @return the enableSnapshotVirtualDirectoryAccess value.
     */
    public Boolean isSnapshotVirtualDirectoryAccessEnabled() {
        return enableSnapshotVirtualDirectoryAccess;
    }

    /**
     * Set the enableSnapshotVirtualDirectoryAccess property: The EnableSnapshotVirtualDirectoryAccess property.
     * Optional. Supported in version 2023-08-03 and above. Only applicable for premium file storage accounts.
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS is enabled.
     * If not specified, the default is true.
     *
     * @param snapshotVirtualDirectoryAccessEnabled the enableSnapshotVirtualDirectoryAccess value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setSnapshotVirtualDirectoryAccessEnabled(
        Boolean snapshotVirtualDirectoryAccessEnabled) {
        this.enableSnapshotVirtualDirectoryAccess = snapshotVirtualDirectoryAccessEnabled;
        return this;
    }
}
