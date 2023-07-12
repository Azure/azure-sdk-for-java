// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.time.OffsetDateTime;

/**
 * Note for closing job.
 */
public class RouterJobNote {
    /**
     * Time the note is written.
     */
    private OffsetDateTime time;

    /**
     * Message for the note.
     */
    private String message;

    /**
     * setter for time.
     * @param time time the note is written.
     * @return this
     */
    public RouterJobNote setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * getter for time.
     * @return time
     */
    public OffsetDateTime getTime() {
        return time;
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
