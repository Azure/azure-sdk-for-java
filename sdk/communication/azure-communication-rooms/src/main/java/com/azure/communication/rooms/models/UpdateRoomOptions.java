// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;

import com.azure.core.annotation.Fluent;

/**
 * The options for updating a room.
 */
@Fluent
public final class UpdateRoomOptions {

    /*
     * The timestamp from when the room is open for joining. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     */
    private OffsetDateTime validFrom;

    /*
     * The timestamp from when the room can no longer be joined. The timestamp is in RFC3339 format:
     * `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     */
    private OffsetDateTime validUntil;

    /**
     * The default constructor of CreateRoomOptions
     */
    public UpdateRoomOptions() {
    }

    /**
     * Set the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     *
     * @param validFrom The starting time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public UpdateRoomOptions setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
        return this;
    }

     /**
     * Set the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     *
     * @param validUntil The end time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public UpdateRoomOptions setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Get the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

     /**
     * Get the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`. The default value is the current date time plus 180 days.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return validUntil;
    }
}
