/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.microsoft.azure.cognitiveservices.faceapi.TrainingStatusType;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Training status object.
 */
public class TrainingStatusInner {
    /**
     * Training status: notstarted, running, succeeded, failed. If the training
     * process is waiting to perform, the status is notstarted. If the training
     * is ongoing, the status is running. Status succeed means this person
     * group is ready for Face - Identify. Status failed is often caused by no
     * person or no persisted face exist in the person group. Possible values
     * include: 'nonstarted', 'running', 'succeeded', 'failed'.
     */
    @JsonProperty(value = "status", required = true)
    private TrainingStatusType status;

    /**
     * A combined UTC date and time string that describes person group created
     * time.
     */
    @JsonProperty(value = "createdDateTime")
    private DateTime created;

    /**
     * Person group last modify time in the UTC, could be null value when the
     * person group is not successfully trained.
     */
    @JsonProperty(value = "lastActionDateTime")
    private DateTime lastAction;

    /**
     * Show failure message when training failed (omitted when training
     * succeed).
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public TrainingStatusType status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the TrainingStatusInner object itself.
     */
    public TrainingStatusInner withStatus(TrainingStatusType status) {
        this.status = status;
        return this;
    }

    /**
     * Get the created value.
     *
     * @return the created value
     */
    public DateTime created() {
        return this.created;
    }

    /**
     * Set the created value.
     *
     * @param created the created value to set
     * @return the TrainingStatusInner object itself.
     */
    public TrainingStatusInner withCreated(DateTime created) {
        this.created = created;
        return this;
    }

    /**
     * Get the lastAction value.
     *
     * @return the lastAction value
     */
    public DateTime lastAction() {
        return this.lastAction;
    }

    /**
     * Set the lastAction value.
     *
     * @param lastAction the lastAction value to set
     * @return the TrainingStatusInner object itself.
     */
    public TrainingStatusInner withLastAction(DateTime lastAction) {
        this.lastAction = lastAction;
        return this;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Set the message value.
     *
     * @param message the message value to set
     * @return the TrainingStatusInner object itself.
     */
    public TrainingStatusInner withMessage(String message) {
        this.message = message;
        return this;
    }

}
