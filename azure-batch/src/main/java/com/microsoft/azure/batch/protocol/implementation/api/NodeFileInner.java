/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


/**
 * Information about a file or directory on a compute node.
 */
public class NodeFileInner {
    /**
     * Gets or sets the file path.
     */
    private String name;

    /**
     * Gets or sets the URL of the file.
     */
    private String url;

    /**
     * Gets or sets whether the object represents a directory.
     */
    private Boolean isDirectory;

    /**
     * Gets or sets the file properties.
     */
    private FileProperties properties;

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
     * @return the NodeFileInner object itself.
     */
    public NodeFileInner setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the NodeFileInner object itself.
     */
    public NodeFileInner setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the isDirectory value.
     *
     * @return the isDirectory value
     */
    public Boolean isDirectory() {
        return this.isDirectory;
    }

    /**
     * Set the isDirectory value.
     *
     * @param isDirectory the isDirectory value to set
     * @return the NodeFileInner object itself.
     */
    public NodeFileInner setIsDirectory(Boolean isDirectory) {
        this.isDirectory = isDirectory;
        return this;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public FileProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the NodeFileInner object itself.
     */
    public NodeFileInner setProperties(FileProperties properties) {
        this.properties = properties;
        return this;
    }

}
