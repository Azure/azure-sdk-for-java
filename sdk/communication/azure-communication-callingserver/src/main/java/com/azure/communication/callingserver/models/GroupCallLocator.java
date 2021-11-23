// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The group call locator. */
@Immutable
public final class GroupCallLocator extends CallLocator {

    /**
     * Get the group call id.
     *
     * @return the group call id of the GroupCallLocator object itself
     */
    public String getGroupCallId() {
        return id;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     *
     * @param id The group call id.
     * @throws IllegalArgumentException if either parameter is null.
     */
    public GroupCallLocator(String id) {
        if (id == null) {
            throw new IllegalArgumentException("serverCallId cannot be null");
        }
        this.id = id;
    }
}
