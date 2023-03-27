// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The event for GroupDataMessage.
 */
@Immutable
public final class GroupMessageEvent {

    private final GroupDataMessage message;

    /**
     * Creates a new instance of GroupMessageEvent.
     *
     * @param message the GroupDataMessage.
     */
    public GroupMessageEvent(GroupDataMessage message) {
        this.message = message;
    }

    /**
     * Gets the GroupDataMessage.
     *
     * @return the GroupDataMessage.
     */
    public GroupDataMessage getMessage() {
        return message;
    }
}
