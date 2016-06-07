/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;

/**
 * The instance view of a virtual machine extension.
 */
public class VirtualMachineExtensionInstanceView {
    /**
     * Gets or sets the virtual machine extension name.
     */
    private String name;

    /**
     * Gets or sets the full type of the extension handler which includes both
     * publisher and type.
     */
    private String type;

    /**
     * Gets or sets the type version of the extension handler.
     */
    private String typeHandlerVersion;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> substatuses;

    /**
     * Gets or sets the resource status information.
     */
    private List<InstanceViewStatus> statuses;

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
     * @return the VirtualMachineExtensionInstanceView object itself.
     */
    public VirtualMachineExtensionInstanceView withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the VirtualMachineExtensionInstanceView object itself.
     */
    public VirtualMachineExtensionInstanceView withType(String type) {
        this.type = type;
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
     * @return the VirtualMachineExtensionInstanceView object itself.
     */
    public VirtualMachineExtensionInstanceView withTypeHandlerVersion(String typeHandlerVersion) {
        this.typeHandlerVersion = typeHandlerVersion;
        return this;
    }

    /**
     * Get the substatuses value.
     *
     * @return the substatuses value
     */
    public List<InstanceViewStatus> substatuses() {
        return this.substatuses;
    }

    /**
     * Set the substatuses value.
     *
     * @param substatuses the substatuses value to set
     * @return the VirtualMachineExtensionInstanceView object itself.
     */
    public VirtualMachineExtensionInstanceView withSubstatuses(List<InstanceViewStatus> substatuses) {
        this.substatuses = substatuses;
        return this;
    }

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
    public List<InstanceViewStatus> statuses() {
        return this.statuses;
    }

    /**
     * Set the statuses value.
     *
     * @param statuses the statuses value to set
     * @return the VirtualMachineExtensionInstanceView object itself.
     */
    public VirtualMachineExtensionInstanceView withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
