// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Uri;

public class OpenConnectionResponse {
    private final boolean connected;
    private final Throwable exception;
    private final Uri uri;

    public OpenConnectionResponse(Uri uri, boolean connected) {
        this(uri, connected, null);
    }

    public OpenConnectionResponse(Uri uri, boolean connected, Throwable exception) {
        this.uri = uri;
        this.connected = connected;
        this.exception = exception;
    }

    public boolean isConnected() {
        return connected;
    }

    public Throwable getException() {
        return exception;
    }

    public Uri getUri() {
        return uri;
    }
}
