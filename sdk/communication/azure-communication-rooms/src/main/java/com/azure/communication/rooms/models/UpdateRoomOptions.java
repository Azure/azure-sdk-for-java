// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;

/**
 * The options for updating a room.
 */
public class UpdateRoomOptions {

    /**
     * The Room Id.
     */
    private String roomId;

    /**
     * The starting time point of the room.
     */
    private OffsetDateTime validFrom;

    /**
     * The ending time point of the room.
     */
    private OffsetDateTime validUntil;

    /**
     * The default constructor of CreateRoomOptions
     */
    public UpdateRoomOptions() {
    }


    /**
     * Set the Room Id.
     *
     * @param roomId The starting time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public UpdateRoomOptions setRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    /**
     * Set the valid starting time point of a room.
     *
     * @param validFrom The starting time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public UpdateRoomOptions setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
        return this;
    }

     /**
     * Set the ending time point of a room.
     *
     * @param validUntil The end time of the room.
     * @return The CreateRoomOptions object itself.
     */
    public UpdateRoomOptions setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }


    /**
     * Get the Room Id.
     *
     * @return Room Id.
     */
    public String getRoomId() {
        return roomId;
    }


    /**
     * Get the valid starting time point of a room.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return validFrom;
    }

     /**
     * Get the ending time point of a room.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return validUntil;
    }
}
