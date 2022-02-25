// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdOpenConnectionRequestArgs;

import java.util.concurrent.CompletableFuture;

public class RntbdOpenConnectionRequestRecord extends CompletableFuture<RntbdOpenConnectionResponse>  {
    private final RntbdOpenConnectionRequestArgs requestArgs;

    public RntbdOpenConnectionRequestRecord(RntbdOpenConnectionRequestArgs args) {
        this.requestArgs = args;
    }

    public RntbdOpenConnectionRequestArgs getRequestArgs() {
        return requestArgs;
    }
}
