/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.microsoft.azure.cognitiveservices.faceapi.TrainingStatus;
import com.microsoft.rest.DateTimeRfc1123;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Training status object.
 */
public class TrainingStatus1Inner {
    /**
     * Training status: notstarted, running, succeeded, failed. If the training
     * process is waiting to perform, the status is notstarted. If the training
     * is ongoing, the status is running. Status succeed means this person
     * group is ready for Face - Identify. Status failed is often caused by no
     * person or no persisted face exist in the person group. Possible values
     * include: 'nonstarted', 'running', 'succeeded', 'failed'.
     */
    @JsonProperty(value = "status", required = true)
    private TrainingStatus status;

    /**
     * A combined UTC date and time string that describes person group created
     * time.
     */
    @JsonProperty(value = "createdDateTime")
    private DateTimeRfc1123 created;

    /**
     * Person group last modify time in the UTC, could be null value when the
     * person group is not successfully trained.
     */
    @JsonProperty(value = "lastActionDateTime")
    private DateTimeRfc1123 lastAction;

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
    public TrainingStatus status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the TrainingStatus1Inner object itself.
     */
    public TrainingStatus1Inner withStatus(TrainingStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Get the created value.
     *
     * @return the created value
     */
    public DateTime created() {
        if (this.created == null) {
            return null;
        }
        return this.created.dateTime();
    }

    /**
     * Set the created value.
     *
     * @param created the created value to set
     * @return the TrainingStatus1Inner object itself.
     */
    public TrainingStatus1Inner withCreated(DateTime created) {
        if (created == null) {
            this.created = null;
        } else {
            this.created = new DateTimeRfc1123(created);
        }
        return this;
    }

    /**
     * Get the lastAction value.
     *
     * @return the lastAction value
     */
    public DateTime lastAction() {
        if (this.lastAction == null) {
            return null;
        }
        return this.lastAction.dateTime();
    }

    /**
     * Set the lastAction value.
     *
     * @param lastAction the lastAction value to set
     * @return the TrainingStatus1Inner object itself.
     */
    public TrainingStatus1Inner withLastAction(DateTime lastAction) {
        if (lastAction == null) {
            this.lastAction = null;
        } else {
            this.lastAction = new DateTimeRfc1123(lastAction);
        }
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
     * @return the TrainingStatus1Inner object itself.
     */
    public TrainingStatus1Inner withMessage(String message) {
        this.message = message;
        return this;
    }

}
