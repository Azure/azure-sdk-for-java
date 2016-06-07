/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Information about a file or directory on a compute node.
 */
public class NodeFile {
    /**
     * The file path.
     */
    private String name;

    /**
     * The URL of the file.
     */
    private String url;

    /**
     * Whether the object represents a directory.
     */
    private Boolean isDirectory;

    /**
     * The file properties.
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
     * @return the NodeFile object itself.
     */
    public NodeFile withName(String name) {
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
     * @return the NodeFile object itself.
     */
    public NodeFile withUrl(String url) {
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
     * @return the NodeFile object itself.
     */
    public NodeFile withIsDirectory(Boolean isDirectory) {
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
     * @return the NodeFile object itself.
     */
    public NodeFile withProperties(FileProperties properties) {
        this.properties = properties;
        return this;
    }

}
