package com.azure.communication.callautomation.models;

/** The AzureBlobContainerRecordingStorage model. */
public class AzureBlobContainerRecordingStorage extends RecordingStorage {

    /** Creates an instance of AzureBlobContainerRecordingStorage class. */
    public AzureBlobContainerRecordingStorage() {
        this.recordingStorageType = RecordingStorageType.fromString("AzureBlobStorage");
    }
}
