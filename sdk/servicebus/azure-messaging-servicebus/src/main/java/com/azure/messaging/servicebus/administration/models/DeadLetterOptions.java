// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import java.util.Map;

/**
 * Options to specify while putting message in dead-letter queue.
 */
public class DeadLetterOptions {

    private String deadLetterReason;
    private String deadLetterErrorDescription;
    private Map<String, Object> propertiesToModify;

    /**
     * Sets the reason while putting message in dead letter sub-queue.
     *
     * @param deadLetterReason while putting message in dead letter sub-queue.
     *
     * @return {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setDeadLetterReason(String deadLetterReason) {
        this.deadLetterReason = deadLetterReason;
        return this;
    }

    /**
     * Sets the error description while putting message in dead letter sub-queue.
     *
     * @param deadLetterErrorDescription while putting message in dead letter sub-queue.
     *
     * @return {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setDeadLetterErrorDescription(String deadLetterErrorDescription) {
        this.deadLetterErrorDescription = deadLetterErrorDescription;
        return this;
    }

    /**
     * Sets the message properties to modify while putting message in dead letter sub-queue.
     *
     * @param propertiesToModify Message properties to modify.
     *
     * @return {@link DeadLetterOptions} object.
     */
    public DeadLetterOptions setPropertiesToModify(Map<String, Object> propertiesToModify) {
        this.propertiesToModify = propertiesToModify;
        return this;
    }

    /**
     * Gets the reason for putting put message in dead letter sub-queue.
     *
     * @return The reason for putting put message in dead letter sub-queue.
     */
    public String getDeadLetterReason() {
        return deadLetterReason;
    }

    /**
     * Gets the error description for putting put message in dead letter sub-queue.
     *
     * @return The error description to for putting message in dead letter sub-queue.
     */
    public String getDeadLetterErrorDescription() {
        return deadLetterErrorDescription;
    }

    /**
     * Gets the message properties to modify while putting put message in dead letter sub-queue.
     *
     * @return The message properties to modify while putting message in dead letter sub-queue.
     */
    public Map<String, Object> getPropertiesToModify() {
        return propertiesToModify;
    }
}
