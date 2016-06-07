/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;


/**
 * Describes Windows Configuration of the OS Profile.
 */
public class LinuxConfiguration {
    /**
     * Gets or sets whether Authentication using user name and password is
     * allowed or not.
     */
    private Boolean disablePasswordAuthentication;

    /**
     * Gets or sets the SSH configuration for linux VMs.
     */
    private SshConfiguration ssh;

    /**
     * Get the disablePasswordAuthentication value.
     *
     * @return the disablePasswordAuthentication value
     */
    public Boolean disablePasswordAuthentication() {
        return this.disablePasswordAuthentication;
    }

    /**
     * Set the disablePasswordAuthentication value.
     *
     * @param disablePasswordAuthentication the disablePasswordAuthentication value to set
     * @return the LinuxConfiguration object itself.
     */
    public LinuxConfiguration withDisablePasswordAuthentication(Boolean disablePasswordAuthentication) {
        this.disablePasswordAuthentication = disablePasswordAuthentication;
        return this;
    }

    /**
     * Get the ssh value.
     *
     * @return the ssh value
     */
    public SshConfiguration ssh() {
        return this.ssh;
    }

    /**
     * Set the ssh value.
     *
     * @param ssh the ssh value to set
     * @return the LinuxConfiguration object itself.
     */
    public LinuxConfiguration withSsh(SshConfiguration ssh) {
        this.ssh = ssh;
        return this;
    }

}
