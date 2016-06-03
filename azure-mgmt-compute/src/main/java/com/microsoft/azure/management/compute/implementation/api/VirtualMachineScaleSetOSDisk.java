/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a virtual machine scale set operating system disk.
 */
public class VirtualMachineScaleSetOSDisk {
    /**
     * Gets or sets the disk name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the caching type. Possible values include: 'None',
     * 'ReadOnly', 'ReadWrite'.
     */
    private CachingTypes caching;

    /**
     * Gets or sets the create option. Possible values include: 'fromImage',
     * 'empty', 'attach'.
     */
    @JsonProperty(required = true)
    private DiskCreateOptionTypes createOption;

    /**
     * Gets or sets the Operating System type. Possible values include:
     * 'Windows', 'Linux'.
     */
    private OperatingSystemTypes osType;

    /**
     * Gets or sets the Source User Image VirtualHardDisk. This
     * VirtualHardDisk will be copied before using it to attach to the
     * Virtual Machine.If SourceImage is provided, the destination
     * VirtualHardDisk should not exist.
     */
    private VirtualHardDisk image;

    /**
     * Gets or sets the list of virtual hard disk container uris.
     */
    private List<String> vhdContainers;

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
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the caching value.
     *
     * @return the caching value
     */
    public CachingTypes caching() {
        return this.caching;
    }

    /**
     * Set the caching value.
     *
     * @param caching the caching value to set
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withCaching(CachingTypes caching) {
        this.caching = caching;
        return this;
    }

    /**
     * Get the createOption value.
     *
     * @return the createOption value
     */
    public DiskCreateOptionTypes createOption() {
        return this.createOption;
    }

    /**
     * Set the createOption value.
     *
     * @param createOption the createOption value to set
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withCreateOption(DiskCreateOptionTypes createOption) {
        this.createOption = createOption;
        return this;
    }

    /**
     * Get the osType value.
     *
     * @return the osType value
     */
    public OperatingSystemTypes osType() {
        return this.osType;
    }

    /**
     * Set the osType value.
     *
     * @param osType the osType value to set
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withOsType(OperatingSystemTypes osType) {
        this.osType = osType;
        return this;
    }

    /**
     * Get the image value.
     *
     * @return the image value
     */
    public VirtualHardDisk image() {
        return this.image;
    }

    /**
     * Set the image value.
     *
     * @param image the image value to set
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withImage(VirtualHardDisk image) {
        this.image = image;
        return this;
    }

    /**
     * Get the vhdContainers value.
     *
     * @return the vhdContainers value
     */
    public List<String> vhdContainers() {
        return this.vhdContainers;
    }

    /**
     * Set the vhdContainers value.
     *
     * @param vhdContainers the vhdContainers value to set
     * @return the VirtualMachineScaleSetOSDisk object itself.
     */
    public VirtualMachineScaleSetOSDisk withVhdContainers(List<String> vhdContainers) {
        this.vhdContainers = vhdContainers;
        return this;
    }

}
