// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.ParticipantRole;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.Role} and
 * {@link Role}.
 */
public final class RoleTypeConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.Role} to {@link Role}.
     */
    public static ParticipantRole convert(com.azure.communication.rooms.implementation.models.ParticipantRole roleType) {
        if (roleType == null) {
            return null;
        }

        ParticipantRole role = ParticipantRole.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = ParticipantRole.ATTENDEE;
                break;
            case "Consumer":
                role = ParticipantRole.CONSUMER;
                break;
            case "Presenter":
                role = ParticipantRole.PRESENTER;
                break;
            default:
                role = ParticipantRole.ATTENDEE;
                break;
        }
        return role;
    }

    /**
     * Maps from {@link Role} to {com.azure.communication.rooms.implementation.models.ParticipantRole}.
    */
    public static com.azure.communication.rooms.implementation.models.ParticipantRole convert(ParticipantRole roleType) {
        if (roleType == null) {
            return null;
        }

        com.azure.communication.rooms.implementation.models.ParticipantRole role = com.azure.communication.rooms.implementation.models.ParticipantRole.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = com.azure.communication.rooms.implementation.models.ParticipantRole.ATTENDEE;
                break;
            case "Consumer":
                role = com.azure.communication.rooms.implementation.models.ParticipantRole.CONSUMER;
                break;
            case "Presenter":
                role = com.azure.communication.rooms.implementation.models.ParticipantRole.PRESENTER;
                break;
            default:
                role = com.azure.communication.rooms.implementation.models.ParticipantRole.ATTENDEE;
                break;
        }
        return role;
    }

}
