/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.store.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Lake Store file or directory information.
 */
public class FileStatusProperties {
    /**
     * Gets the last access time as ticks since the epoch.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long accessTime;

    /**
     * Gets the block size for the file.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long blockSize;

    /**
     * Gets the number of children in the directory.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long childrenNum;

    /**
     * Gets the group owner.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String group;

    /**
     * Gets the number of bytes in a file.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long length;

    /**
     * Gets the modification time as ticks since the epoch.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long modificationTime;

    /**
     * Gets the user who is the owner.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String owner;

    /**
     * Gets the path suffix.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pathSuffix;

    /**
     * Gets the permission represented as an string.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String permission;

    /**
     * Gets the type of the path object. Possible values include: 'FILE',
     * 'DIRECTORY'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private FileType type;

    /**
     * Get the accessTime value.
     *
     * @return the accessTime value
     */
    public Long accessTime() {
        return this.accessTime;
    }

    /**
     * Get the blockSize value.
     *
     * @return the blockSize value
     */
    public Long blockSize() {
        return this.blockSize;
    }

    /**
     * Get the childrenNum value.
     *
     * @return the childrenNum value
     */
    public Long childrenNum() {
        return this.childrenNum;
    }

    /**
     * Get the group value.
     *
     * @return the group value
     */
    public String group() {
        return this.group;
    }

    /**
     * Get the length value.
     *
     * @return the length value
     */
    public Long length() {
        return this.length;
    }

    /**
     * Get the modificationTime value.
     *
     * @return the modificationTime value
     */
    public Long modificationTime() {
        return this.modificationTime;
    }

    /**
     * Get the owner value.
     *
     * @return the owner value
     */
    public String owner() {
        return this.owner;
    }

    /**
     * Get the pathSuffix value.
     *
     * @return the pathSuffix value
     */
    public String pathSuffix() {
        return this.pathSuffix;
    }

    /**
     * Get the permission value.
     *
     * @return the permission value
     */
    public String permission() {
        return this.permission;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public FileType type() {
        return this.type;
    }

}
