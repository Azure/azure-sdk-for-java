/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes an Operating System disk.
 */
public class OSDisk {
    /**
     * Gets or sets the Operating System type. Possible values include:
     * 'Windows', 'Linux'.
     */
    private OperatingSystemTypes osType;

    /**
     * Gets or sets the disk encryption settings.
     */
    private DiskEncryptionSettings encryptionSettings;

    /**
     * Gets or sets the disk name.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the Virtual Hard Disk.
     */
    @JsonProperty(required = true)
    private VirtualHardDisk vhd;

    /**
     * Gets or sets the Source User Image VirtualHardDisk. This
     * VirtualHardDisk will be copied before using it to attach to the
     * Virtual Machine.If SourceImage is provided, the destination
     * VirtualHardDisk should not exist.
     */
    private VirtualHardDisk image;

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
     * Gets or sets the initial disk size in GB for blank data disks, and the
     * new desired size for existing OS and Data disks.
     */
    private Integer diskSizeGB;

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
     * @return the OSDisk object itself.
     */
    public OSDisk withOsType(OperatingSystemTypes osType) {
        this.osType = osType;
        return this;
    }

    /**
     * Get the encryptionSettings value.
     *
     * @return the encryptionSettings value
     */
    public DiskEncryptionSettings encryptionSettings() {
        return this.encryptionSettings;
    }

    /**
     * Set the encryptionSettings value.
     *
     * @param encryptionSettings the encryptionSettings value to set
     * @return the OSDisk object itself.
     */
    public OSDisk withEncryptionSettings(DiskEncryptionSettings encryptionSettings) {
        this.encryptionSettings = encryptionSettings;
        return this;
    }

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
     * @return the OSDisk object itself.
     */
    public OSDisk withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the vhd value.
     *
     * @return the vhd value
     */
    public VirtualHardDisk vhd() {
        return this.vhd;
    }

    /**
     * Set the vhd value.
     *
     * @param vhd the vhd value to set
     * @return the OSDisk object itself.
     */
    public OSDisk withVhd(VirtualHardDisk vhd) {
        this.vhd = vhd;
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
     * @return the OSDisk object itself.
     */
    public OSDisk withImage(VirtualHardDisk image) {
        this.image = image;
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
     * @return the OSDisk object itself.
     */
    public OSDisk withCaching(CachingTypes caching) {
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
     * @return the OSDisk object itself.
     */
    public OSDisk withCreateOption(DiskCreateOptionTypes createOption) {
        this.createOption = createOption;
        return this;
    }

    /**
     * Get the diskSizeGB value.
     *
     * @return the diskSizeGB value
     */
    public Integer diskSizeGB() {
        return this.diskSizeGB;
    }

    /**
     * Set the diskSizeGB value.
     *
     * @param diskSizeGB the diskSizeGB value to set
     * @return the OSDisk object itself.
     */
    public OSDisk withDiskSizeGB(Integer diskSizeGB) {
        this.diskSizeGB = diskSizeGB;
        return this;
    }

}
