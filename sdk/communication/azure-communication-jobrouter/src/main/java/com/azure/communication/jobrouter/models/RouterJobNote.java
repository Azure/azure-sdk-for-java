// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.time.OffsetDateTime;

/**
 * Note for closing job.
 */
public class RouterJobNote {
    /**
     * The time at which the note was added in UTC.
     */
    private OffsetDateTime addedAt;

    /**
     * Message for the note.
     */
    private String message;

    /**
     * setter for time.
     * @param addedAt time at which the note was added in UTC.
     * @return this
     */
    public RouterJobNote setAddedAt(OffsetDateTime addedAt) {
        this.addedAt = addedAt;
        return this;
    }

    /**
     * getter for addedAt.
     * @return addedAt
     */
    public OffsetDateTime getAddedAt() {
        return addedAt;
    }

    /**
     * Setter for message.
     * @param message Message for the note.
     * @return this
     */
    public RouterJobNote setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Getter for message.
     * @return message.
     */
    public String getMessage() {
        return message;
    }
}
