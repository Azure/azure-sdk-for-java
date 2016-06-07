/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Describes a Virtual Machine Extension.
 */
@JsonFlatten
public class VirtualMachineExtensionInner extends Resource {
    /**
     * Gets or sets how the extension handler should be forced to update even
     * if the extension configuration has not changed. Possible values
     * include: 'RerunExtension'.
     */
    @JsonProperty(value = "properties.forceUpdateTag")
    private ForceUpdateTagTypes forceUpdateTag;

    /**
     * Gets or sets the name of the extension handler publisher.
     */
    @JsonProperty(value = "properties.publisher")
    private String publisher;

    /**
     * Gets or sets the type of the extension handler.
     */
    @JsonProperty(value = "properties.type")
    private String virtualMachineExtensionType;

    /**
     * Gets or sets the type version of the extension handler.
     */
    @JsonProperty(value = "properties.typeHandlerVersion")
    private String typeHandlerVersion;

    /**
     * Gets or sets whether the extension handler should be automatically
     * upgraded across minor versions.
     */
    @JsonProperty(value = "properties.autoUpgradeMinorVersion")
    private Boolean autoUpgradeMinorVersion;

    /**
     * Gets or sets Json formatted public settings for the extension.
     */
    @JsonProperty(value = "properties.settings")
    private Object settings;

    /**
     * Gets or sets Json formatted protected settings for the extension.
     */
    @JsonProperty(value = "properties.protectedSettings")
    private Object protectedSettings;

    /**
     * Gets or sets the provisioning state, which only appears in the response.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets or sets the virtual machine extension instance view.
     */
    @JsonProperty(value = "properties.instanceView")
    private VirtualMachineExtensionInstanceView instanceView;

    /**
     * Get the forceUpdateTag value.
     *
     * @return the forceUpdateTag value
     */
    public ForceUpdateTagTypes forceUpdateTag() {
        return this.forceUpdateTag;
    }

    /**
     * Set the forceUpdateTag value.
     *
     * @param forceUpdateTag the forceUpdateTag value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withForceUpdateTag(ForceUpdateTagTypes forceUpdateTag) {
        this.forceUpdateTag = forceUpdateTag;
        return this;
    }

    /**
     * Get the publisher value.
     *
     * @return the publisher value
     */
    public String publisher() {
        return this.publisher;
    }

    /**
     * Set the publisher value.
     *
     * @param publisher the publisher value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withPublisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    /**
     * Get the virtualMachineExtensionType value.
     *
     * @return the virtualMachineExtensionType value
     */
    public String virtualMachineExtensionType() {
        return this.virtualMachineExtensionType;
    }

    /**
     * Set the virtualMachineExtensionType value.
     *
     * @param virtualMachineExtensionType the virtualMachineExtensionType value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withVirtualMachineExtensionType(String virtualMachineExtensionType) {
        this.virtualMachineExtensionType = virtualMachineExtensionType;
        return this;
    }

    /**
     * Get the typeHandlerVersion value.
     *
     * @return the typeHandlerVersion value
     */
    public String typeHandlerVersion() {
        return this.typeHandlerVersion;
    }

    /**
     * Set the typeHandlerVersion value.
     *
     * @param typeHandlerVersion the typeHandlerVersion value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withTypeHandlerVersion(String typeHandlerVersion) {
        this.typeHandlerVersion = typeHandlerVersion;
        return this;
    }

    /**
     * Get the autoUpgradeMinorVersion value.
     *
     * @return the autoUpgradeMinorVersion value
     */
    public Boolean autoUpgradeMinorVersion() {
        return this.autoUpgradeMinorVersion;
    }

    /**
     * Set the autoUpgradeMinorVersion value.
     *
     * @param autoUpgradeMinorVersion the autoUpgradeMinorVersion value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withAutoUpgradeMinorVersion(Boolean autoUpgradeMinorVersion) {
        this.autoUpgradeMinorVersion = autoUpgradeMinorVersion;
        return this;
    }

    /**
     * Get the settings value.
     *
     * @return the settings value
     */
    public Object settings() {
        return this.settings;
    }

    /**
     * Set the settings value.
     *
     * @param settings the settings value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withSettings(Object settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Get the protectedSettings value.
     *
     * @return the protectedSettings value
     */
    public Object protectedSettings() {
        return this.protectedSettings;
    }

    /**
     * Set the protectedSettings value.
     *
     * @param protectedSettings the protectedSettings value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withProtectedSettings(Object protectedSettings) {
        this.protectedSettings = protectedSettings;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the instanceView value.
     *
     * @return the instanceView value
     */
    public VirtualMachineExtensionInstanceView instanceView() {
        return this.instanceView;
    }

    /**
     * Set the instanceView value.
     *
     * @param instanceView the instanceView value to set
     * @return the VirtualMachineExtensionInner object itself.
     */
    public VirtualMachineExtensionInner withInstanceView(VirtualMachineExtensionInstanceView instanceView) {
        this.instanceView = instanceView;
        return this;
    }

}
