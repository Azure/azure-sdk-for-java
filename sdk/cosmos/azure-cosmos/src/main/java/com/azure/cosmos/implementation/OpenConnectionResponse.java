// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Uri;

public class OpenConnectionResponse {
    private final boolean connected;
    private final Throwable exception;
    private final Uri uri;
    private final boolean openConnectionAttempted;
    private final int openConnectionCountToEndpoint;

    public OpenConnectionResponse(Uri uri, boolean connected, Throwable exception, int openConnectionCountToEndpoint) {
        this(uri, connected, exception, true, openConnectionCountToEndpoint);
    }

    public OpenConnectionResponse(Uri uri, boolean connected, Throwable exception, boolean openConnectionAttempted, int openConnectionCountToEndpoint) {
        this.uri = uri;
        this.connected = connected;
        this.exception = exception;
        this.openConnectionAttempted = openConnectionAttempted;
        this.openConnectionCountToEndpoint = openConnectionCountToEndpoint;
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

    public boolean isOpenConnectionAttempted() {
        return openConnectionAttempted;
    }

    public int getOpenConnectionCountToEndpoint() {
        return openConnectionCountToEndpoint;
    }
}
