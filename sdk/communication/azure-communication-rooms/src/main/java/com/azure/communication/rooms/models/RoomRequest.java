// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

import java.time.OffsetDateTime;
import java.util.Map;

/** The Room Request model. */
public class RoomRequest {
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private Map<String, Object> participants;

    /**
     * Get the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @return the validUntil value.
     */
    public OffsetDateTime getValidFrom() {
        return this.validFrom;
    }


    /**
     * Set the validFrom property: The timestamp from when the room is open for joining. The timestamp is in RFC3339
     * format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param validFrom the validFrom value to set.
     * @return the UpdateRoomRequest object itself.
     */
    public RoomRequest setValidFrom(OffsetDateTime validFrom) {
        this.validFrom = validFrom;
        return this;
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
     * Set the validUntil property: The timestamp from when the room can no longer be joined. The timestamp is in
     * RFC3339 format: `yyyy-MM-ddTHH:mm:ssZ`.
     *
     * @param validUntil the validUntil value to set.
     * @return the UpdateRoomRequest object itself.
     */
    public RoomRequest setValidUntil(OffsetDateTime validUntil) {
        this.validUntil = validUntil;
        return this;
    }

    /**
     * Get the participants property: (Optional) Collection of identities invited to the room.
     *
     * @return the participants value.
     */
    //@JsonInclude(content = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, Object> getParticipants() {
        return this.participants;
    }

    /**
     * Set the participants property: (Optional) Collection of identities invited to the room.
     *
     * @param participants the participants value to set.
     * @return the UpdateRoomRequest object itself.
     */
    public RoomRequest setParticipants(Map<String, Object> participants) {
        this.participants = participants;
        return this;
    }
}
