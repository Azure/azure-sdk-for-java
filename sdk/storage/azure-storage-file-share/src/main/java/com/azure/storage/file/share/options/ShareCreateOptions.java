// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRootSquash;

import java.util.Map;

/**
 * Extended options that may be passed when creating a share.
 */
@Fluent
public class ShareCreateOptions {
    private Integer quotaInGb;
    private Map<String, String> metadata;
    private ShareAccessTier accessTier;
    private ShareProtocols protocols;
    private ShareRootSquash rootSquash;
    private Boolean enableSnapshotVirtualDirectoryAccess;

    /**
     * Creates a new instance of {@link ShareCreateOptions}.
     */
    public ShareCreateOptions() {
    }

    /**
     * Gets the size in GB to limit the share's growth.
     *
     * @return Size in GB to limit the share's growth.
     */
    public Integer getQuotaInGb() {
        return quotaInGb;
    }

    /**
     * Sets the size in GB to limit the share's growth.
     *
     * @param quotaInGb Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The updated options.
     */
    public ShareCreateOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
        return this;
    }

    /**
     * Gets the metadata to associate with the share.
     *
     * @return Metadata to associate with the share
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata to associate with the share.
     *
     * @param metadata Metadata to associate with the share. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the access tier for the share.
     *
     * @return {@link ShareAccessTier}.
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * Sets the access tier for the share.
     *
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareCreateOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

    /**
     * Gets the protocols to enable on the share.
     *
     * @return {@link ShareProtocols}
     */
    public ShareProtocols getProtocols() {
        return protocols;
    }

    /**
     * Sets the protocols to enable on the share.
     *
     * @param protocols {@link ShareProtocols}
     * @return The updated options.
     */
    public ShareCreateOptions setProtocols(ShareProtocols protocols) {
        this.protocols = protocols;
        return this;
    }

    /**
     * Gets the root squash to set for the share. Only valid for NFS.
     *
     * @return The root squash to set for the share. Only valid for NFS.
     */
    public ShareRootSquash getRootSquash() {
        return rootSquash;
    }

    /**
     * Sets the root squash to set for the share. Only valid for NFS.
     *
     * @param rootSquash The root squash to set for the share. Only valid for NFS.
     * @return The updated options.
     */
    public ShareCreateOptions setRootSquash(ShareRootSquash rootSquash) {
        this.rootSquash = rootSquash;
        return this;
    }

    /**
     * Get the enableSnapshotVirtualDirectoryAccess property: The EnableSnapshotVirtualDirectoryAccess property.
     * Optional. Supported in version 2023-08-03 and above.  Only applicable for premium file storage accounts.
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS is enabled.
     * If not specified, the default is true.
     * @return the enableSnapshotVirtualDirectoryAccess value.
     */
    public Boolean isSnapshotVirtualDirectoryAccessEnabled() {
        return enableSnapshotVirtualDirectoryAccess;
    }

    /**
     * Set the enableSnapshotVirtualDirectoryAccess property:
     * Optional. Supported in version 2023-08-03 and above. Only applicable for premium file storage accounts.
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS is enabled.
     * If not specified, the default is true.
     * @param snapshotVirtualDirectoryAccessEnabled the enableSnapshotVirtualDirectoryAccess value to set.
     * @return the ShareCreateOptions object itself.
     */
    public ShareCreateOptions setSnapshotVirtualDirectoryAccessEnabled(
        Boolean snapshotVirtualDirectoryAccessEnabled) {
        this.enableSnapshotVirtualDirectoryAccess = snapshotVirtualDirectoryAccessEnabled;
        return this;
    }
}
