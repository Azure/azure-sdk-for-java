/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


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
     * Gets or sets the file mode attribute in octal format. This property
     * will be ignored if it is specified for a resourceFile which will be
     * downloaded to a Windows compute node.
     */
    private String fileMode;

    /**
     * Get the blobSource value.
     *
     * @return the blobSource value
     */
    public String blobSource() {
        return this.blobSource;
    }

    /**
     * Set the blobSource value.
     *
     * @param blobSource the blobSource value to set
     * @return the ResourceFile object itself.
     */
    public ResourceFile setBlobSource(String blobSource) {
        this.blobSource = blobSource;
        return this;
    }

    /**
     * Get the filePath value.
     *
     * @return the filePath value
     */
    public String filePath() {
        return this.filePath;
    }

    /**
     * Set the filePath value.
     *
     * @param filePath the filePath value to set
     * @return the ResourceFile object itself.
     */
    public ResourceFile setFilePath(String filePath) {
        this.filePath = filePath;
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
     * @return the ResourceFile object itself.
     */
    public ResourceFile setFileMode(String fileMode) {
        this.fileMode = fileMode;
        return this;
    }

}
