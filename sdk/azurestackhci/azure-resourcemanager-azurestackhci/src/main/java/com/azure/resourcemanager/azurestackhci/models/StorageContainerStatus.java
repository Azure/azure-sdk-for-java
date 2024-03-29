// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.azurestackhci.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The observed state of storage containers. */
@Fluent
public final class StorageContainerStatus {
    /*
     * StorageContainer provisioning error code
     */
    @JsonProperty(value = "errorCode")
    private String errorCode;

    /*
     * Descriptive error message
     */
    @JsonProperty(value = "errorMessage")
    private String errorMessage;

    /*
     * Amount of space available on the disk in MB
     */
    @JsonProperty(value = "availableSizeMB")
    private Long availableSizeMB;

    /*
     * Total size of the disk in MB
     */
    @JsonProperty(value = "containerSizeMB")
    private Long containerSizeMB;

    /*
     * The provisioningStatus property.
     */
    @JsonProperty(value = "provisioningStatus")
    private StorageContainerStatusProvisioningStatus provisioningStatus;

    /** Creates an instance of StorageContainerStatus class. */
    public StorageContainerStatus() {
    }

    /**
     * Get the errorCode property: StorageContainer provisioning error code.
     *
     * @return the errorCode value.
     */
    public String errorCode() {
        return this.errorCode;
    }

    /**
     * Set the errorCode property: StorageContainer provisioning error code.
     *
     * @param errorCode the errorCode value to set.
     * @return the StorageContainerStatus object itself.
     */
    public StorageContainerStatus withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    /**
     * Get the errorMessage property: Descriptive error message.
     *
     * @return the errorMessage value.
     */
    public String errorMessage() {
        return this.errorMessage;
    }

    /**
     * Set the errorMessage property: Descriptive error message.
     *
     * @param errorMessage the errorMessage value to set.
     * @return the StorageContainerStatus object itself.
     */
    public StorageContainerStatus withErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Get the availableSizeMB property: Amount of space available on the disk in MB.
     *
     * @return the availableSizeMB value.
     */
    public Long availableSizeMB() {
        return this.availableSizeMB;
    }

    /**
     * Set the availableSizeMB property: Amount of space available on the disk in MB.
     *
     * @param availableSizeMB the availableSizeMB value to set.
     * @return the StorageContainerStatus object itself.
     */
    public StorageContainerStatus withAvailableSizeMB(Long availableSizeMB) {
        this.availableSizeMB = availableSizeMB;
        return this;
    }

    /**
     * Get the containerSizeMB property: Total size of the disk in MB.
     *
     * @return the containerSizeMB value.
     */
    public Long containerSizeMB() {
        return this.containerSizeMB;
    }

    /**
     * Set the containerSizeMB property: Total size of the disk in MB.
     *
     * @param containerSizeMB the containerSizeMB value to set.
     * @return the StorageContainerStatus object itself.
     */
    public StorageContainerStatus withContainerSizeMB(Long containerSizeMB) {
        this.containerSizeMB = containerSizeMB;
        return this;
    }

    /**
     * Get the provisioningStatus property: The provisioningStatus property.
     *
     * @return the provisioningStatus value.
     */
    public StorageContainerStatusProvisioningStatus provisioningStatus() {
        return this.provisioningStatus;
    }

    /**
     * Set the provisioningStatus property: The provisioningStatus property.
     *
     * @param provisioningStatus the provisioningStatus value to set.
     * @return the StorageContainerStatus object itself.
     */
    public StorageContainerStatus withProvisioningStatus(StorageContainerStatusProvisioningStatus provisioningStatus) {
        this.provisioningStatus = provisioningStatus;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (provisioningStatus() != null) {
            provisioningStatus().validate();
        }
    }
}
