// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.models;

/** The RoomParticipant model. */
public class RoomParticipant {
    private final String identifier;
    private final String roleName;

    /**
     * The default constructor of RoomParticipant.
     *
     * @param identifier The Participant MRI Id.
     * @param roleName The role name of the participant.
     */
    public RoomParticipant(String identifier, String roleName) {
        this.identifier = identifier;
        this.roleName = roleName;
    }

    /**
     * Get the identifier of a participant.
     *
     * @return The mri identifier string of the participant.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the role name of a participant.
     *
     * @return The participant role name.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Compare the participant.
     *
     * @return The equality of participant.
     */
    @Override
    public boolean equals(Object p) {
        if (p == this) {
            return true;
        }

        if (!(p instanceof RoomParticipant)) {
            return false;
        }

        RoomParticipant o = (RoomParticipant) p;

        return this.identifier.equals(o.getIdentifier()) && this.roleName.equals(o.getRoleName());
    }

    /**
     * Hash the participant.
     *
     * @return The hashcode of participant.
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.identifier.hashCode();
        result = 31 * result + this.roleName.hashCode();
        return result;
    }
}
