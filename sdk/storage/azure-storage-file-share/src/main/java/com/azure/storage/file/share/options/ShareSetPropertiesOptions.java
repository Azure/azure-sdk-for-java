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
    private Boolean enablePaidBursting;
    private Long paidBurstingMaxIops;
    private Long paidBurstingMaxBandwidthMibps;
    private Long provisionedMaxIops;
    private Long provisionedMaxBandwidthMibps;

    /**
     * Creates a new instance of {@link ShareSetPropertiesOptions}.
     */
    public ShareSetPropertiesOptions() {
    }

    /**
     * Gets the {@link ShareAccessTier}.
     *
     * @return {@link ShareAccessTier}
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * Sets the {@link ShareAccessTier}.
     *
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
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
     * @param quotaInGb Size in GB to limit the share's growth.
     * @return The updated options.
     */
    public ShareSetPropertiesOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
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
    public ShareSetPropertiesOptions setRootSquash(ShareRootSquash rootSquash) {
        this.rootSquash = rootSquash;
        return this;
    }

    /**
     * Gets the {@link ShareRequestConditions}.
     *
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the {@link ShareRequestConditions}.
     *
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
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS
     * is enabled.
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
     * Specifies whether the snapshot virtual directory should be accessible at the root of share mount point when NFS
     * is enabled.
     * If not specified, the default is true.
     *
     * @param snapshotVirtualDirectoryAccessEnabled the enableSnapshotVirtualDirectoryAccess value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions
        setSnapshotVirtualDirectoryAccessEnabled(Boolean snapshotVirtualDirectoryAccessEnabled) {
        this.enableSnapshotVirtualDirectoryAccess = snapshotVirtualDirectoryAccessEnabled;
        return this;
    }

    /**
     * Get the enablePaidBursting property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * This property enables paid bursting on premium file storage accounts.
     * @return the enablePaidBursting value.
     */
    public Boolean isPaidBurstingEnabled() {
        return enablePaidBursting;
    }

    /**
     * Set the enablePaidBursting property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * This property enables paid bursting on premium file storage accounts.
     * @param enablePaidBursting the enablePaidBursting value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setPaidBurstingEnabled(Boolean enablePaidBursting) {
        this.enablePaidBursting = enablePaidBursting;
        return this;
    }

    /**
     * Get the paidBurstingMaxIops property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * Default if not specified is the maximum IOPS the file share can support.
     * Current maximum for a file share is 102,400 IOPS.
     * @return the paidBurstingMaxIops value.
     */
    public Long getPaidBurstingMaxIops() {
        return paidBurstingMaxIops;
    }

    /**
     * Set the paidBurstingMaxIops property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * Default if not specified is the maximum IOPS the file share can support.
     * Current maximum for a file share is 102,400 IOPS.
     * @param paidBurstingMaxIops the paidBurstingMaxIops value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setPaidBurstingMaxIops(Long paidBurstingMaxIops) {
        this.paidBurstingMaxIops = paidBurstingMaxIops;
        return this;
    }

    /**
     * Get the paidBurstingMaxBandwidthMibps property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * Default if not specified is the maximum throughput the file share can support.
     * Current maximum for a file share is 10,340 MiB/sec.
     * @return the paidBurstingMaxBandwidthMibps value.
     */
    public Long getPaidBurstingMaxBandwidthMibps() {
        return paidBurstingMaxBandwidthMibps;
    }

    /**
     * Set the paidBurstingMaxBandwidthMibps property:
     * Optional. Supported in version 2023-11-03 and above. Only applicable for premium file storage accounts.
     * Default if not specified is the maximum throughput the file share can support.
     * Current maximum for a file share is 10,340 MiB/sec.
     * @param paidBurstingMaxBandwidthMibps the paidBurstingMaxBandwidthMibps value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setPaidBurstingMaxBandwidthMibps(Long paidBurstingMaxBandwidthMibps) {
        this.paidBurstingMaxBandwidthMibps = paidBurstingMaxBandwidthMibps;
        return this;
    }

    /**
     * Get the provisionedMaxIops property:
     * Optional. Only applicable to provisioned v2 storage accounts.
     * The provisioned IOPS of the share. For SSD, minimum IOPS is 3,000 and maximum is 100,000.
     * For HDD, minimum IOPS is 500 and maximum is 50,000.
     * @return the provisionedMaxIops value.
     */
    public Long getProvisionedMaxIops() {
        return provisionedMaxIops;
    }

    /**
     * Set the provisionedMaxIops property:
     * Optional. Only applicable to provisioned v2 storage accounts.
     * The provisioned IOPS of the share. For SSD, minimum IOPS is 3,000 and maximum is 100,000.
     * For HDD, minimum IOPS is 500 and maximum is 50,000.
     * @param provisionedMaxIops the provisionedIops value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setProvisionedMaxIops(Long provisionedMaxIops) {
        this.provisionedMaxIops = provisionedMaxIops;
        return this;
    }

    /**
     * Get the provisionedMaxBandwidthMibps property:
     * Optional. Only applicable to provisioned v2 storage accounts.
     * The provisioned throughput of the share. For SSD, minimum throughput is 125 MiB/sec and maximum is 10,340 MiB/sec.
     * For HDD, minimum throughput is 60 MiB/sec and maximum is 5,125 MiB/sec.
     * @return the provisionedMaxBandwidthMibps value.
     */
    public Long getProvisionedMaxBandwidthMibps() {
        return provisionedMaxBandwidthMibps;
    }

    /**
     * Set the provisionedMaxBandwidthMibps property:
     * Optional. Only applicable to provisioned v2 storage accounts.
     * The provisioned throughput of the share. For SSD, minimum throughput is 125 MiB/sec and maximum is 10,340 MiB/sec.
     * For HDD, minimum throughput is 60 MiB/sec and maximum is 5,125 MiB/sec.
     * @param provisionedMaxBandwidthMibps the provisionedMaxBandwidthMibps value to set.
     * @return the ShareSetPropertiesOptions object itself.
     */
    public ShareSetPropertiesOptions setProvisionedMaxBandwidthMibps(Long provisionedMaxBandwidthMibps) {
        this.provisionedMaxBandwidthMibps = provisionedMaxBandwidthMibps;
        return this;
    }
}
