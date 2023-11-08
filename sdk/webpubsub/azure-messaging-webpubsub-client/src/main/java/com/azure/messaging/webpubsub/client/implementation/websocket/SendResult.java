// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.websocket;

public final class SendResult {

    private final Throwable throwable;

    public SendResult() {
        this.throwable = null;
    }

    public SendResult(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getException() {
        return throwable;
    }

    public boolean isOK() {
        return throwable == null;
    }
}
