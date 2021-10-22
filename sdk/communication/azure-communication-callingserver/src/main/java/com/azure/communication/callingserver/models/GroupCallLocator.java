// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The group call locator. */
@Immutable
public final class GroupCallLocator extends CallLocator {
    /**
     * The group call id.
     */
    private final String groupCallId;

    /**
     * Get the group call id.
     *
     * @return the group call id of the GroupCallLocator object itself
     */
    public String getGroupCallId() {
        return groupCallId;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     *
     * @param groupCallId The group call id.
     * @throws IllegalArgumentException if either parameter is null.
     */
    public GroupCallLocator(String groupCallId) {
        if (groupCallId == null) {
            throw new IllegalArgumentException("serverCallId cannot be null");
        }
        this.groupCallId = groupCallId;
    }
}
