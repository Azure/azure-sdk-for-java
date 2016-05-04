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
     * Gets or sets the file creation time.
     */
    private DateTime creationTime;

    /**
     * Gets or sets the time at which the file was last modified.
     */
    @JsonProperty(required = true)
    private DateTime lastModified;

    /**
     * Gets or sets the length of the file.
     */
    @JsonProperty(required = true)
    private long contentLength;

    /**
     * Gets or sets the content type of the file.
     */
    private String contentType;

    /**
     * Gets or sets the file mode attribute in octal format. This property
     * will be returned only from a Linux compute node.
     */
    private String fileMode;

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime getCreationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     */
    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime getLastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     */
    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Get the contentLength value.
     *
     * @return the contentLength value
     */
    public long getContentLength() {
        return this.contentLength;
    }

    /**
     * Set the contentLength value.
     *
     * @param contentLength the contentLength value to set
     */
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the fileMode value.
     *
     * @return the fileMode value
     */
    public String getFileMode() {
        return this.fileMode;
    }

    /**
     * Set the fileMode value.
     *
     * @param fileMode the fileMode value to set
     */
    public void setFileMode(String fileMode) {
        this.fileMode = fileMode;
    }

}
