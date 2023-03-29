// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
import java.util.List;
import com.azure.core.annotation.Immutable;

/** The CommunicationRoom model. */
@Immutable
public final class CommunicationRoom {
    private final String roomId;
    private final OffsetDateTime validFrom;
    private final OffsetDateTime validUntil;
    private final OffsetDateTime createdAt;

    /**
     * The default constructor of CommunicationRoom.
     *
     * @param roomId The Room Id.
     * @param validFrom The starting time point of the room.
     * @param validUntil The ending time point of the room.
     * @param createdAt The created time point of the room.
     */
    public CommunicationRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, OffsetDateTime createdAt) {
        this.roomId = roomId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

    /**
     * Get the Room Id.
     *
     * @return Room Id.
     */
    public String getRoomId() {
        return this.roomId;
    }

    /**
     * Get the valid starting time point of a room.
     *
     * @return The starting time of the room.
     */
    public OffsetDateTime getValidFrom() {
        return this.validFrom;
    }

    /**
     * Get the ending time point of a room.
     *
     * @return The end time of the room.
     */
    public OffsetDateTime getValidUntil() {
        return this.validUntil;
    }

    /**
     * Get the created time of the room.
     *
     * @return The created time of the room.
     */
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }
}

