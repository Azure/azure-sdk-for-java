package com.azure.storage.common;

public class TransferValidationOptions {
    private UploadTransferValidationOptions upload = new UploadTransferValidationOptions();
    private DownloadTransferValidationOptions download = new DownloadTransferValidationOptions();

    public TransferValidationOptions() {
    }

    public UploadTransferValidationOptions getUpload() {
        return upload;
    }

    public TransferValidationOptions setUpload(UploadTransferValidationOptions upload) {
        this.upload = upload;
        return this;
    }

    public DownloadTransferValidationOptions getDownload() {
        return download;
    }

    public TransferValidationOptions setDownload(DownloadTransferValidationOptions download) {
        this.download = download;
        return this;
    }
}
