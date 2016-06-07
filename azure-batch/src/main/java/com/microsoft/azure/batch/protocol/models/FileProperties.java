/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The properties of a file on a compute node.
 */
public class FileProperties {
    /**
     * The file creation time.
     */
    private DateTime creationTime;

    /**
     * The time at which the file was last modified.
     */
    @JsonProperty(required = true)
    private DateTime lastModified;

    /**
     * The length of the file.
     */
    @JsonProperty(required = true)
    private long contentLength;

    /**
     * The content type of the file.
     */
    private String contentType;

    /**
     * The file mode attribute in octal format. This property will be returned
     * only from a Linux compute node.
     */
    private String fileMode;

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     * @return the FileProperties object itself.
     */
    public FileProperties withCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the FileProperties object itself.
     */
    public FileProperties withLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Get the contentLength value.
     *
     * @return the contentLength value
     */
    public long contentLength() {
        return this.contentLength;
    }

    /**
     * Set the contentLength value.
     *
     * @param contentLength the contentLength value to set
     * @return the FileProperties object itself.
     */
    public FileProperties withContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the FileProperties object itself.
     */
    public FileProperties withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the fileMode value.
     *
     * @return the fileMode value
     */
    public String fileMode() {
        return this.fileMode;
    }

    /**
     * Set the fileMode value.
     *
     * @param fileMode the fileMode value to set
     * @return the FileProperties object itself.
     */
    public FileProperties withFileMode(String fileMode) {
        this.fileMode = fileMode;
        return this;
    }

}
