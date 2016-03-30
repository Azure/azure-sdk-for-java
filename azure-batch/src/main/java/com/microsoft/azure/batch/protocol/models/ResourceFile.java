/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * A file to be downloaded from Azure blob storage to a compute node.
 */
public class ResourceFile {
    /**
     * Gets or sets the URL of a blob in Azure storage. The Batch service
     * downloads the blob to the specified file path. The URL must be
     * readable using anonymous access.
     */
    private String blobSource;

    /**
     * Gets or sets the location on the compute node to which the file should
     * be downloaded.
     */
    private String filePath;

    /**
     * Get the blobSource value.
     *
     * @return the blobSource value
     */
    public String getBlobSource() {
        return this.blobSource;
    }

    /**
     * Set the blobSource value.
     *
     * @param blobSource the blobSource value to set
     */
    public void setBlobSource(String blobSource) {
        this.blobSource = blobSource;
    }

    /**
     * Get the filePath value.
     *
     * @return the filePath value
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Set the filePath value.
     *
     * @param filePath the filePath value to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
