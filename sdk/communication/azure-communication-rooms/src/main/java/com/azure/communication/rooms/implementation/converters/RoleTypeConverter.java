// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms.implementation.converters;

import com.azure.communication.rooms.models.RoleType;

/**
 * A converter between {@link com.azure.communication.rooms.implementation.models.RoleType} and
 * {@link RoleType}.
 */
public final class RoleTypeConverter {
    /**
     * Maps from {com.azure.communication.rooms.implementation.models.RoleType} to {@link RoleType}.
     */
    public static RoleType convert(com.azure.communication.rooms.implementation.models.RoleType roleType) {
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
     * Maps from {@link RoleType} to {com.azure.communication.rooms.implementation.models.RoleType}.
    */
    public static com.azure.communication.rooms.implementation.models.RoleType convert(RoleType roleType) {
        if (roleType == null) {
            return null;
        }

        com.azure.communication.rooms.implementation.models.RoleType role = com.azure.communication.rooms.implementation.models.RoleType.ATTENDEE;

        switch (roleType.toString()) {
            case "Attendee":
                role = com.azure.communication.rooms.implementation.models.RoleType.ATTENDEE;
                break;
            case "Consumer":
                role = com.azure.communication.rooms.implementation.models.RoleType.CONSUMER;
                break;
            case "Presenter":
                role = com.azure.communication.rooms.implementation.models.RoleType.PRESENTER;
                break;
            default:
                role = com.azure.communication.rooms.implementation.models.RoleType.ATTENDEE;
                break;
        }
        return role;
    }

}
