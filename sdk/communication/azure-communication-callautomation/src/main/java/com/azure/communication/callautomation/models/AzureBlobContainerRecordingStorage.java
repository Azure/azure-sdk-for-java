package com.azure.communication.callautomation.models;

/** The AzureCommunicationRecordingStorage model. */
public class AzureBlobContainerRecordingStorage extends RecordingStorage {

    /** Creates an instance of AzureCommunicationRecordingStorage class. 
     * 
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
    */
    public AzureBlobContainerRecordingStorage(String recordingDestinationContainerUrl) {
        this.setRecordingStorageType(RecordingStorageType.fromString("AzureCommunicationServices"));
        this.setRecordingDestinationContainerUrl(recordingDestinationContainerUrl);
    }

    /**
     * Get the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @return the recordingDestinationContainerUrl value.
     */
    public String getRecordingDestinationContainerUrl() {
        return this.recordingDestinationContainerUrl;
    }

    /**
     * Set the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
     * @return the StorageInternal object itself.
     */
    public RecordingStorage setRecordingDestinationContainerUrl(String recordingDestinationContainerUrl) {
        super.recordingDestinationContainerUrl = recordingDestinationContainerUrl;
        return this;
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
