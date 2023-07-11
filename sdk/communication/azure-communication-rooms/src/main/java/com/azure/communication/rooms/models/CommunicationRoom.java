// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
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
     * @param roomId Unique identifier of a room. This id is server generated.
     * @param validFrom The timestamp from when the room is open for joining. The timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     * @param validUntil The timestamp from when the room can no longer be joined. The timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     * @param createdAt The timestamp when the room was created at the server. The timestamp is in RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     */
    public CommunicationRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, OffsetDateTime createdAt) {
        this.roomId = roomId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

     /**
     * Get the id property: Unique identifier of a room. This id is server generated.
     *
     * @return the id value.
     */
    public String getRoomId() {
        return this.roomId;
    }

    /**
     * Get the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the validFrom value.
     */
    public OffsetDateTime getValidFrom() {
        return this.validFrom;
    }

    /**
     * Get the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the validUntil value.
     */
    public OffsetDateTime getValidUntil() {
        return this.validUntil;
    }

    /**
     * Get the createdAt property: The timestamp when the room was created at the server. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the createdAt value.
     */
    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }
}

