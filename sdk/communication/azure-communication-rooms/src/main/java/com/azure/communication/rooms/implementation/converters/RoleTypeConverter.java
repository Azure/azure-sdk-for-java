// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.RoleType;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.Role} and
 * {@link RoleType}.
 */
public final class RoleTypeConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.Role} to {@link RoleType}.
     */
    public static RoleType convert(com.azure.communication.rooms.implementation.models.Role roleType) {
        if (roleType == null) {
            return null;
        }

        RoleType role = RoleType.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = RoleType.ATTENDEE;
                break;
            case "Consumer":
                role = RoleType.CONSUMER;
                break;
            case "Presenter":
                role = RoleType.PRESENTER;
                break;
            default:
                role = RoleType.ATTENDEE;
                break;
        }
        return role;
    }

    /**
     * Maps from {@link RoleType} to {com.azure.communication.rooms.implementation.models.Role}.
    */
    public static com.azure.communication.rooms.implementation.models.Role convert(RoleType roleType) {
        if (roleType == null) {
            return null;
        }

        com.azure.communication.rooms.implementation.models.Role role = com.azure.communication.rooms.implementation.models.Role.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = com.azure.communication.rooms.implementation.models.Role.ATTENDEE;
                break;
            case "Consumer":
                role = com.azure.communication.rooms.implementation.models.Role.CONSUMER;
                break;
            case "Presenter":
                role = com.azure.communication.rooms.implementation.models.Role.PRESENTER;
                break;
            default:
                role = com.azure.communication.rooms.implementation.models.Role.ATTENDEE;
                break;
        }
        return role;
    }

}
