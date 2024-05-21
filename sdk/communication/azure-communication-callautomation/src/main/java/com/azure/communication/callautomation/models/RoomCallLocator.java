// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Immutable;

/** The locator used for joining or taking action on a call. */
@Immutable
public final class RoomCallLocator extends CallLocator {
    /*
     * The room id
     */
    private final String roomId;

    /*
     * Defines the kind of call locator
     */
    private CallLocatorKind kind;

    /**
     * Room call locator
     *
     * @param roomId Specify the room id.
     */
    public RoomCallLocator(String roomId) {
        this.kind = CallLocatorKind.ROOM_CALL_LOCATOR;
        this.roomId = roomId;
    }

    /**
     * Get the roomId property: The room id.
     *
     * @return the roomId value.
     */
    public String getRoomId() {
        return this.roomId;
    }

    /**
     * Get the kind property: The call locator kind.
     *
     * @return the kind value.
     */
    public CallLocatorKind getKind() {
        return this.kind;
    }
}
