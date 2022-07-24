// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

/** The base event interface. */
public interface CallingServerBaseEvent {
    /**
     * Get the type property: The event type.
     *
     * @return the type value.
     */
    AcsEventType getType();
}
