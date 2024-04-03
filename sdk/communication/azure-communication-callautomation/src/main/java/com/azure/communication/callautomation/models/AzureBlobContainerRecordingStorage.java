package com.azure.communication.callautomation.models;

/** The AzureCommunicationRecordingStorage model. */
public class AzureBlobContainerRecordingStorage extends RecordingStorage {
    private String recordingDestinationContainerUrl;

    /** Creates an instance of AzureCommunicationRecordingStorage class. 
     * 
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
    */
    public AzureBlobContainerRecordingStorage(String recordingDestinationContainerUrl) {
        this.recordingStorageType = RecordingStorageType.fromString("AzureCommunicationServices");
        this.recordingDestinationContainerUrl = recordingDestinationContainerUrl;
    }

    /**
     * Get the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @return the recordingDestinationContainerUrl value.
     */
    public String getRecordingDestinationContainerUrl() {
        return recordingDestinationContainerUrl;
    }

    /**
     * Set the recordingDestinationContainerUrl property: Uri of a container or a location within a container.
     *
     * @param recordingDestinationContainerUrl the recordingDestinationContainerUrl value to set.
     * @return the StorageInternal object itself.
     */
    public RecordingStorage setRecordingDestinationContainerUrl(String recordingDestinationContainerUrl) {
        this.recordingDestinationContainerUrl = recordingDestinationContainerUrl;
        return this;
    }
}
