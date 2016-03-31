/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response to a ComputeNodeOperation.GetRemoteLoginSettings request.
 */
public class ComputeNodeGetRemoteLoginSettingsResult {
    /**
     * Gets or sets the IP address used for remote login to the compute node.
     */
    @JsonProperty(required = true)
    private String remoteLoginIPAddress;

    /**
     * Gets or sets the port used for remote login to the compute node.
     */
    @JsonProperty(required = true)
    private int remoteLoginPort;

    /**
     * Get the remoteLoginIPAddress value.
     *
     * @return the remoteLoginIPAddress value
     */
    public String getRemoteLoginIPAddress() {
        return this.remoteLoginIPAddress;
    }

    /**
     * Set the remoteLoginIPAddress value.
     *
     * @param remoteLoginIPAddress the remoteLoginIPAddress value to set
     */
    public void setRemoteLoginIPAddress(String remoteLoginIPAddress) {
        this.remoteLoginIPAddress = remoteLoginIPAddress;
    }

    /**
     * Get the remoteLoginPort value.
     *
     * @return the remoteLoginPort value
     */
    public int getRemoteLoginPort() {
        return this.remoteLoginPort;
    }

    /**
     * Set the remoteLoginPort value.
     *
     * @param remoteLoginPort the remoteLoginPort value to set
     */
    public void setRemoteLoginPort(int remoteLoginPort) {
        this.remoteLoginPort = remoteLoginPort;
    }

}
