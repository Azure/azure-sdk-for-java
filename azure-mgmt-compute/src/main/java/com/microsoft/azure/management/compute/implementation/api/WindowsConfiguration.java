/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * Describes Windows Configuration of the OS Profile.
 */
public class WindowsConfiguration {
    /**
     * Gets or sets whether VM Agent should be provisioned on the Virtual
     * Machine.
     */
    private Boolean provisionVMAgent;

    /**
     * Gets or sets whether Windows updates are automatically installed on the
     * VM.
     */
    private Boolean enableAutomaticUpdates;

    /**
     * Gets or sets the Time Zone of the VM.
     */
    private String timeZone;

    /**
     * Gets or sets the additional base-64 encoded XML formatted information
     * that can be included in the Unattend.xml file.
     */
    private List<AdditionalUnattendContent> additionalUnattendContent;

    /**
     * Gets or sets the Windows Remote Management configuration of the VM.
     */
    private WinRMConfiguration winRM;

    /**
     * Get the provisionVMAgent value.
     *
     * @return the provisionVMAgent value
     */
    public Boolean provisionVMAgent() {
        return this.provisionVMAgent;
    }

    /**
     * Set the provisionVMAgent value.
     *
     * @param provisionVMAgent the provisionVMAgent value to set
     * @return the WindowsConfiguration object itself.
     */
    public WindowsConfiguration withProvisionVMAgent(Boolean provisionVMAgent) {
        this.provisionVMAgent = provisionVMAgent;
        return this;
    }

    /**
     * Get the enableAutomaticUpdates value.
     *
     * @return the enableAutomaticUpdates value
     */
    public Boolean enableAutomaticUpdates() {
        return this.enableAutomaticUpdates;
    }

    /**
     * Set the enableAutomaticUpdates value.
     *
     * @param enableAutomaticUpdates the enableAutomaticUpdates value to set
     * @return the WindowsConfiguration object itself.
     */
    public WindowsConfiguration withEnableAutomaticUpdates(Boolean enableAutomaticUpdates) {
        this.enableAutomaticUpdates = enableAutomaticUpdates;
        return this;
    }

    /**
     * Get the timeZone value.
     *
     * @return the timeZone value
     */
    public String timeZone() {
        return this.timeZone;
    }

    /**
     * Set the timeZone value.
     *
     * @param timeZone the timeZone value to set
     * @return the WindowsConfiguration object itself.
     */
    public WindowsConfiguration withTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    /**
     * Get the additionalUnattendContent value.
     *
     * @return the additionalUnattendContent value
     */
    public List<AdditionalUnattendContent> additionalUnattendContent() {
        return this.additionalUnattendContent;
    }

    /**
     * Set the additionalUnattendContent value.
     *
     * @param additionalUnattendContent the additionalUnattendContent value to set
     * @return the WindowsConfiguration object itself.
     */
    public WindowsConfiguration withAdditionalUnattendContent(List<AdditionalUnattendContent> additionalUnattendContent) {
        this.additionalUnattendContent = additionalUnattendContent;
        return this;
    }

    /**
     * Get the winRM value.
     *
     * @return the winRM value
     */
    public WinRMConfiguration winRM() {
        return this.winRM;
    }

    /**
     * Set the winRM value.
     *
     * @param winRM the winRM value to set
     * @return the WindowsConfiguration object itself.
     */
    public WindowsConfiguration withWinRM(WinRMConfiguration winRM) {
        this.winRM = winRM;
        return this;
    }

}
