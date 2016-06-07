/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Describes an OS profile.
 */
public class OSProfile {
    /**
     * Gets or sets the computer name.
     */
    private String computerName;

    /**
     * Gets or sets the admin user name.
     */
    private String adminUsername;

    /**
     * Gets or sets the admin user password.
     */
    private String adminPassword;

    /**
     * Gets or sets a base-64 encoded string of custom data.
     */
    private String customData;

    /**
     * Gets or sets the Windows Configuration of the OS profile.
     */
    private WindowsConfiguration windowsConfiguration;

    /**
     * Gets or sets the Linux Configuration of the OS profile.
     */
    private LinuxConfiguration linuxConfiguration;

    /**
     * Gets or sets the List of certificates for addition to the VM.
     */
    private List<VaultSecretGroup> secrets;

    /**
     * Get the computerName value.
     *
     * @return the computerName value
     */
    public String computerName() {
        return this.computerName;
    }

    /**
     * Set the computerName value.
     *
     * @param computerName the computerName value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withComputerName(String computerName) {
        this.computerName = computerName;
        return this;
    }

    /**
     * Get the adminUsername value.
     *
     * @return the adminUsername value
     */
    public String adminUsername() {
        return this.adminUsername;
    }

    /**
     * Set the adminUsername value.
     *
     * @param adminUsername the adminUsername value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
        return this;
    }

    /**
     * Get the adminPassword value.
     *
     * @return the adminPassword value
     */
    public String adminPassword() {
        return this.adminPassword;
    }

    /**
     * Set the adminPassword value.
     *
     * @param adminPassword the adminPassword value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
        return this;
    }

    /**
     * Get the customData value.
     *
     * @return the customData value
     */
    public String customData() {
        return this.customData;
    }

    /**
     * Set the customData value.
     *
     * @param customData the customData value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withCustomData(String customData) {
        this.customData = customData;
        return this;
    }

    /**
     * Get the windowsConfiguration value.
     *
     * @return the windowsConfiguration value
     */
    public WindowsConfiguration windowsConfiguration() {
        return this.windowsConfiguration;
    }

    /**
     * Set the windowsConfiguration value.
     *
     * @param windowsConfiguration the windowsConfiguration value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withWindowsConfiguration(WindowsConfiguration windowsConfiguration) {
        this.windowsConfiguration = windowsConfiguration;
        return this;
    }

    /**
     * Get the linuxConfiguration value.
     *
     * @return the linuxConfiguration value
     */
    public LinuxConfiguration linuxConfiguration() {
        return this.linuxConfiguration;
    }

    /**
     * Set the linuxConfiguration value.
     *
     * @param linuxConfiguration the linuxConfiguration value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withLinuxConfiguration(LinuxConfiguration linuxConfiguration) {
        this.linuxConfiguration = linuxConfiguration;
        return this;
    }

    /**
     * Get the secrets value.
     *
     * @return the secrets value
     */
    public List<VaultSecretGroup> secrets() {
        return this.secrets;
    }

    /**
     * Set the secrets value.
     *
     * @param secrets the secrets value to set
     * @return the OSProfile object itself.
     */
    public OSProfile withSecrets(List<VaultSecretGroup> secrets) {
        this.secrets = secrets;
        return this;
    }

}
