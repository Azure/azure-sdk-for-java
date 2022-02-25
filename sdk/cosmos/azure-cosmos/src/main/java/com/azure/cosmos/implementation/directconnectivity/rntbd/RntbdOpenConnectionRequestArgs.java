// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.RxOpenConnectionRequest;

import java.net.URI;

public class RntbdOpenConnectionRequestArgs {
    private final URI physicalAddress;
    private final RxOpenConnectionRequest openConnectionRequest;

    public RntbdOpenConnectionRequestArgs(RxOpenConnectionRequest openConnectionRequest, URI physicalAddress) {
        this.physicalAddress = physicalAddress;
        this.openConnectionRequest = openConnectionRequest;
    }

    public URI getPhysicalAddress() {
        return physicalAddress;
    }

    public RxOpenConnectionRequest getOpenConnectionRequest() {
        return openConnectionRequest;
    }
}
