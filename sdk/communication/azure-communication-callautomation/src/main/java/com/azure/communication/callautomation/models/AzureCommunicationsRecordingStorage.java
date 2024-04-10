package com.azure.communication.callautomation.models;

/** The AzureBlobContainerRecordingStorage model. */
public class AzureCommunicationsRecordingStorage extends RecordingStorage {

    /** 
     * Creates an instance of AzureBlobContainerRecordingStorage class. 
     */
    public AzureCommunicationsRecordingStorage() {
        this.setRecordingStorageType(RecordingStorageType.fromString("AzureBlobStorage"));
    }

    /**
     * Get the recordingStorageType property: Defines the kind of external storage.
     *
     * @return the recordingStorageType value.
     */
    @Override
    public RecordingStorageType getRecordingStorageType() {
        return this.recordingStorageType;
    }
}
