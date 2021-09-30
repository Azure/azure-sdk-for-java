// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The group call locator. */
@Immutable
public final class GroupCallLocator extends CallLocator
{
    private final String groupCallId;

    /// <summary> The group call id. </summary>
    public final String getGroupCallId()
    {
        return groupCallId;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     */
    public GroupCallLocator(String groupCallId)
    {
        this.groupCallId = groupCallId;
    }
}
