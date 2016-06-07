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
 * Describes a Virtual Machine Extension Image.
 */
@JsonFlatten
public class VirtualMachineExtensionImageInner extends Resource {
    /**
     * Gets or sets the operating system this extension supports.
     */
    @JsonProperty(value = "properties.operatingSystem", required = true)
    private String operatingSystem;

    /**
     * Gets or sets the type of role (IaaS or PaaS) this extension supports.
     */
    @JsonProperty(value = "properties.computeRole", required = true)
    private String computeRole;

    /**
     * Gets or sets the schema defined by publisher, where extension consumers
     * should provide settings in a matching schema.
     */
    @JsonProperty(value = "properties.handlerSchema", required = true)
    private String handlerSchema;

    /**
     * Gets or sets whether the extension can be used on xRP VMScaleSets.By
     * default existing extensions are usable on scalesets, but there might
     * be cases where a publisher wants to explicitly indicate the extension
     * is only enabled for CRP VMs but not VMSS.
     */
    @JsonProperty(value = "properties.vmScaleSetEnabled")
    private Boolean vmScaleSetEnabled;

    /**
     * Gets or sets whether the handler can support multiple extensions.
     */
    @JsonProperty(value = "properties.supportsMultipleExtensions")
    private Boolean supportsMultipleExtensions;

    /**
     * Get the operatingSystem value.
     *
     * @return the operatingSystem value
     */
    public String operatingSystem() {
        return this.operatingSystem;
    }

    /**
     * Set the operatingSystem value.
     *
     * @param operatingSystem the operatingSystem value to set
     * @return the VirtualMachineExtensionImageInner object itself.
     */
    public VirtualMachineExtensionImageInner withOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
        return this;
    }

    /**
     * Get the computeRole value.
     *
     * @return the computeRole value
     */
    public String computeRole() {
        return this.computeRole;
    }

    /**
     * Set the computeRole value.
     *
     * @param computeRole the computeRole value to set
     * @return the VirtualMachineExtensionImageInner object itself.
     */
    public VirtualMachineExtensionImageInner withComputeRole(String computeRole) {
        this.computeRole = computeRole;
        return this;
    }

    /**
     * Get the handlerSchema value.
     *
     * @return the handlerSchema value
     */
    public String handlerSchema() {
        return this.handlerSchema;
    }

    /**
     * Set the handlerSchema value.
     *
     * @param handlerSchema the handlerSchema value to set
     * @return the VirtualMachineExtensionImageInner object itself.
     */
    public VirtualMachineExtensionImageInner withHandlerSchema(String handlerSchema) {
        this.handlerSchema = handlerSchema;
        return this;
    }

    /**
     * Get the vmScaleSetEnabled value.
     *
     * @return the vmScaleSetEnabled value
     */
    public Boolean vmScaleSetEnabled() {
        return this.vmScaleSetEnabled;
    }

    /**
     * Set the vmScaleSetEnabled value.
     *
     * @param vmScaleSetEnabled the vmScaleSetEnabled value to set
     * @return the VirtualMachineExtensionImageInner object itself.
     */
    public VirtualMachineExtensionImageInner withVmScaleSetEnabled(Boolean vmScaleSetEnabled) {
        this.vmScaleSetEnabled = vmScaleSetEnabled;
        return this;
    }

    /**
     * Get the supportsMultipleExtensions value.
     *
     * @return the supportsMultipleExtensions value
     */
    public Boolean supportsMultipleExtensions() {
        return this.supportsMultipleExtensions;
    }

    /**
     * Set the supportsMultipleExtensions value.
     *
     * @param supportsMultipleExtensions the supportsMultipleExtensions value to set
     * @return the VirtualMachineExtensionImageInner object itself.
     */
    public VirtualMachineExtensionImageInner withSupportsMultipleExtensions(Boolean supportsMultipleExtensions) {
        this.supportsMultipleExtensions = supportsMultipleExtensions;
        return this;
    }

}
