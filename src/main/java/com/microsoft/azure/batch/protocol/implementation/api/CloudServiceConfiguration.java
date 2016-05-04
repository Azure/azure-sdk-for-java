/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The configuration of cloud service for a pool.
 */
public class CloudServiceConfiguration {
    /**
     * Gets or sets the Azure Guest OS family to be installed on the virtual
     * machines in the pool.
     */
    @JsonProperty(required = true)
    private String osFamily;

    /**
     * Gets or sets the Azure Guest OS version to be installed on the virtual
     * machines in the pool. The default value is * which specifies the
     * latest operating system version for the specified OS family.
     */
    private String targetOSVersion;

    /**
     * Gets or sets the Azure Guest OS Version currently installed on the
     * virtual machines in the pool. This may differ from TargetOSVersion if
     * the pool state is Upgrading.
     */
    private String currentOSVersion;

    /**
     * Get the osFamily value.
     *
     * @return the osFamily value
     */
    public String osFamily() {
        return this.osFamily;
    }

    /**
     * Set the osFamily value.
     *
     * @param osFamily the osFamily value to set
     * @return the CloudServiceConfiguration object itself.
     */
    public CloudServiceConfiguration setOsFamily(String osFamily) {
        this.osFamily = osFamily;
        return this;
    }

    /**
     * Get the targetOSVersion value.
     *
     * @return the targetOSVersion value
     */
    public String targetOSVersion() {
        return this.targetOSVersion;
    }

    /**
     * Set the targetOSVersion value.
     *
     * @param targetOSVersion the targetOSVersion value to set
     * @return the CloudServiceConfiguration object itself.
     */
    public CloudServiceConfiguration setTargetOSVersion(String targetOSVersion) {
        this.targetOSVersion = targetOSVersion;
        return this;
    }

    /**
     * Get the currentOSVersion value.
     *
     * @return the currentOSVersion value
     */
    public String currentOSVersion() {
        return this.currentOSVersion;
    }

    /**
     * Set the currentOSVersion value.
     *
     * @param currentOSVersion the currentOSVersion value to set
     * @return the CloudServiceConfiguration object itself.
     */
    public CloudServiceConfiguration setCurrentOSVersion(String currentOSVersion) {
        this.currentOSVersion = currentOSVersion;
        return this;
    }

}
