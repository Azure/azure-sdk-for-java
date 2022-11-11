// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * Options for configuring validation of transfer contents to and from storage.
 */
public class TransferValidationOptions {
    private UploadTransferValidationOptions upload = new UploadTransferValidationOptions();
    private DownloadTransferValidationOptions download = new DownloadTransferValidationOptions();

    /**
     * Options constructor.
     */
    public TransferValidationOptions() {
    }

    /**
     * Gets the options for an upload.
     * @return The upload options.
     */
    public UploadTransferValidationOptions getUpload() {
        return upload;
    }

    /**
     * Sets the options for an upload.
     * @param upload Upload options to set.
     * @return The updated options class for uploads and downloads.
     */
    public TransferValidationOptions setUpload(UploadTransferValidationOptions upload) {
        this.upload = upload;
        return this;
    }

    /**
     * Gets the options for a download.
     * @return The download options.
     */
    public DownloadTransferValidationOptions getDownload() {
        return download;
    }

    /**
     * Sets the options for a download.
     * @param download Download options to set.
     * @return The updated options class for uploads and downloads.
     */
    public TransferValidationOptions setDownload(DownloadTransferValidationOptions download) {
        this.download = download;
        return this;
    }
}
