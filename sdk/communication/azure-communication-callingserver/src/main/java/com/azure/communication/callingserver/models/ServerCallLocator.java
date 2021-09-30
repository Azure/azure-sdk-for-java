// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models;

import com.azure.core.annotation.Immutable;

/** The server call locator. */
@Immutable
public final class ServerCallLocator extends CallLocator
{
    private final String serverCallId;

    /// <summary> The server call id. </summary>
    public final String getServerCallId()
    {
        return serverCallId;
    }

    /**
     * Initializes a new instance of ServerCallLocator
     */
    public ServerCallLocator(String serverCallId)
    {
        this.serverCallId = serverCallId;
    }
}
