// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Note for a job.
 */
public class RouterJobNote {
    /**
     * Creates an instance of RouterJobNote class.
     *
     * @param message The message for the note.
     */
    public RouterJobNote(String message) {
        this.message = Objects.requireNonNull(message, "'message' cannot be null.");
    }

    /**
     * The time at which the note was added in UTC.
     */
    private OffsetDateTime addedAt;

    /**
     * Message for the note.
     */
    private final String message;

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
     * Getter for message.
     * @return message.
     */
    public String getMessage() {
        return message;
    }
}
