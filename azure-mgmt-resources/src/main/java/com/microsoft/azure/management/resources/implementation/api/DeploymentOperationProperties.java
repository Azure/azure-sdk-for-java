/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import org.joda.time.DateTime;

/**
 * Deployment operation properties.
 */
public class DeploymentOperationProperties {
    /**
     * Gets or sets the state of the provisioning.
     */
    private String provisioningState;

    /**
     * Gets or sets the date and time of the operation.
     */
    private DateTime timestamp;

    /**
     * Gets or sets operation status code.
     */
    private String statusCode;

    /**
     * Gets or sets operation status message.
     */
    private Object statusMessage;

    /**
     * Gets or sets the target resource.
     */
    private TargetResource targetResource;

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the DeploymentOperationProperties object itself.
     */
    public DeploymentOperationProperties withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public DateTime timestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the DeploymentOperationProperties object itself.
     */
    public DeploymentOperationProperties withTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get the statusCode value.
     *
     * @return the statusCode value
     */
    public String statusCode() {
        return this.statusCode;
    }

    /**
     * Set the statusCode value.
     *
     * @param statusCode the statusCode value to set
     * @return the DeploymentOperationProperties object itself.
     */
    public DeploymentOperationProperties withStatusCode(String statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Get the statusMessage value.
     *
     * @return the statusMessage value
     */
    public Object statusMessage() {
        return this.statusMessage;
    }

    /**
     * Set the statusMessage value.
     *
     * @param statusMessage the statusMessage value to set
     * @return the DeploymentOperationProperties object itself.
     */
    public DeploymentOperationProperties withStatusMessage(Object statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    /**
     * Get the targetResource value.
     *
     * @return the targetResource value
     */
    public TargetResource targetResource() {
        return this.targetResource;
    }

    /**
     * Set the targetResource value.
     *
     * @param targetResource the targetResource value to set
     * @return the DeploymentOperationProperties object itself.
     */
    public DeploymentOperationProperties withTargetResource(TargetResource targetResource) {
        this.targetResource = targetResource;
        return this;
    }

}
