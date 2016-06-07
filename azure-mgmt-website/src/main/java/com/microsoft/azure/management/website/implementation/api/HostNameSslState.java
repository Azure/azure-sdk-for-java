/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object that represents a SSL-enabled host name.
 */
public class HostNameSslState {
    /**
     * Host name.
     */
    private String name;

    /**
     * SSL type. Possible values include: 'Disabled', 'SniEnabled',
     * 'IpBasedEnabled'.
     */
    @JsonProperty(required = true)
    private SslState sslState;

    /**
     * Virtual IP address assigned to the host name if IP based SSL is enabled.
     */
    private String virtualIP;

    /**
     * SSL cert thumbprint.
     */
    private String thumbprint;

    /**
     * Set this flag to update existing host name.
     */
    private Boolean toUpdate;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the HostNameSslState object itself.
     */
    public HostNameSslState withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the sslState value.
     *
     * @return the sslState value
     */
    public SslState sslState() {
        return this.sslState;
    }

    /**
     * Set the sslState value.
     *
     * @param sslState the sslState value to set
     * @return the HostNameSslState object itself.
     */
    public HostNameSslState withSslState(SslState sslState) {
        this.sslState = sslState;
        return this;
    }

    /**
     * Get the virtualIP value.
     *
     * @return the virtualIP value
     */
    public String virtualIP() {
        return this.virtualIP;
    }

    /**
     * Set the virtualIP value.
     *
     * @param virtualIP the virtualIP value to set
     * @return the HostNameSslState object itself.
     */
    public HostNameSslState withVirtualIP(String virtualIP) {
        this.virtualIP = virtualIP;
        return this;
    }

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String thumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     * @return the HostNameSslState object itself.
     */
    public HostNameSslState withThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
        return this;
    }

    /**
     * Get the toUpdate value.
     *
     * @return the toUpdate value
     */
    public Boolean toUpdate() {
        return this.toUpdate;
    }

    /**
     * Set the toUpdate value.
     *
     * @param toUpdate the toUpdate value to set
     * @return the HostNameSslState object itself.
     */
    public HostNameSslState withToUpdate(Boolean toUpdate) {
        this.toUpdate = toUpdate;
        return this;
    }

}
