// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The event when re-join group failed, after reconnect when autoRestoreGroup is true.
 */
@Immutable
public final class RejoinGroupFailedEvent {

    private final String group;
    private final Exception exception;

    /**
     * Creates a new instance of RejoinGroupFailedEvent.
     *
     * @param group the group name.
     * @param exception the exception.
     */
    public RejoinGroupFailedEvent(String group, Exception exception) {
        this.group = group;
        this.exception = exception;
    }

    /**
     * Gets the group name.
     *
     * @return the group name.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Gets the exception.
     *
     * @return the exception.
     */
    public Exception getException() {
        return exception;
    }
}
