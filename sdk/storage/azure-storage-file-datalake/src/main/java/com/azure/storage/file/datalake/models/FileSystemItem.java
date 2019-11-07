// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.Map;

/**
 * An Azure Storage file system.
 */
public class FileSystemItem {
    private String name;
    private FileSystemItemProperties properties;
    private Map<String, String> metadata;

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The name property.
     *
     * @param name the name value to set.
     * @return the FileSystemItem object itself.
     */
    public FileSystemItem setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the properties property: The properties property.
     *
     * @return the properties value.
     */
    public FileSystemItemProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: The properties property.
     *
     * @param properties the properties value to set.
     * @return the FileSystemItem object itself.
     */
    public FileSystemItem setProperties(FileSystemItemProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the metadata property: The metadata property.
     *
     * @return the metadata value.
     */
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: The metadata property.
     *
     * @param metadata the metadata value to set.
     * @return the FileSystemItem object itself.
     */
    public FileSystemItem setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

}
