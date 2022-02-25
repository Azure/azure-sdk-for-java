// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

public class RntbdOpenConnectionResponse {

    public static RntbdOpenConnectionResponse SUCCESS = new RntbdOpenConnectionResponse(true, null);
    private final boolean succeeded;
    private final Throwable cause;

    public RntbdOpenConnectionResponse(boolean succeeded, Throwable cause) {
        this.succeeded = succeeded;
        this.cause = cause;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public Throwable getCause() {
        return cause;
    }
}
