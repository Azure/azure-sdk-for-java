/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;

/**
 * Class containting details about site recovery operation.
 */
public class CsmSiteRecoveryEntityInner {
    /**
     * Point in time in which the site recover should be attempted.
     */
    private DateTime snapshotTime;

    /**
     * If true, then the website's configuration will be reverted to its state
     * at SnapshotTime.
     */
    private Boolean recoverConfig;

    /**
     * [Optional] Destination web app name into which web app should be
     * recovered. This is case when new web app should be created instead.
     */
    private String siteName;

    /**
     * [Optional] Destination web app slot name into which web app should be
     * recovered.
     */
    private String slotName;

    /**
     * Get the snapshotTime value.
     *
     * @return the snapshotTime value
     */
    public DateTime snapshotTime() {
        return this.snapshotTime;
    }

    /**
     * Set the snapshotTime value.
     *
     * @param snapshotTime the snapshotTime value to set
     * @return the CsmSiteRecoveryEntityInner object itself.
     */
    public CsmSiteRecoveryEntityInner withSnapshotTime(DateTime snapshotTime) {
        this.snapshotTime = snapshotTime;
        return this;
    }

    /**
     * Get the recoverConfig value.
     *
     * @return the recoverConfig value
     */
    public Boolean recoverConfig() {
        return this.recoverConfig;
    }

    /**
     * Set the recoverConfig value.
     *
     * @param recoverConfig the recoverConfig value to set
     * @return the CsmSiteRecoveryEntityInner object itself.
     */
    public CsmSiteRecoveryEntityInner withRecoverConfig(Boolean recoverConfig) {
        this.recoverConfig = recoverConfig;
        return this;
    }

    /**
     * Get the siteName value.
     *
     * @return the siteName value
     */
    public String siteName() {
        return this.siteName;
    }

    /**
     * Set the siteName value.
     *
     * @param siteName the siteName value to set
     * @return the CsmSiteRecoveryEntityInner object itself.
     */
    public CsmSiteRecoveryEntityInner withSiteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    /**
     * Get the slotName value.
     *
     * @return the slotName value
     */
    public String slotName() {
        return this.slotName;
    }

    /**
     * Set the slotName value.
     *
     * @param slotName the slotName value to set
     * @return the CsmSiteRecoveryEntityInner object itself.
     */
    public CsmSiteRecoveryEntityInner withSlotName(String slotName) {
        this.slotName = slotName;
        return this;
    }

}
