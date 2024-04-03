package com.azure.communication.callautomation.models;

/** The AzureBlobContainerRecordingStorage model. */
public class AzureCommunicationsRecordingStorage extends RecordingStorage {

    /** Creates an instance of AzureBlobContainerRecordingStorage class. */
    public AzureCommunicationsRecordingStorage() {
        this.recordingStorageType = RecordingStorageType.fromString("AzureBlobStorage");
    }
}
