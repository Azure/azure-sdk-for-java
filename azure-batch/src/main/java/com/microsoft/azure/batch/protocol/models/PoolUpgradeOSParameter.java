/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a CloudPoolOperations.UpgradeOS request.
 */
public class PoolUpgradeOSParameter {
    /**
     * The Azure Guest OS version to be installed on the virtual machines in
     * the pool.
     */
    @JsonProperty(required = true)
    private String targetOSVersion;

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
     * @return the PoolUpgradeOSParameter object itself.
     */
    public PoolUpgradeOSParameter withTargetOSVersion(String targetOSVersion) {
        this.targetOSVersion = targetOSVersion;
        return this;
    }

}
