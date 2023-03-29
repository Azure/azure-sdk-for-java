// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.Role;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.Role} and
 * {@link Role}.
 */
public final class RoleTypeConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.Role} to {@link Role}.
     */
    public static Role convert(com.azure.communication.rooms.implementation.models.Role roleType) {
        if (roleType == null) {
            return null;
        }

        Role role = Role.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = Role.ATTENDEE;
                break;
            case "Consumer":
                role = Role.CONSUMER;
                break;
            case "Presenter":
                role = Role.PRESENTER;
                break;
            default:
                role = Role.ATTENDEE;
                break;
        }
        return role;
    }

    /**
     * Maps from {@link Role} to {com.azure.communication.rooms.implementation.models.Role}.
    */
    public static com.azure.communication.rooms.implementation.models.Role convert(Role roleType) {
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
