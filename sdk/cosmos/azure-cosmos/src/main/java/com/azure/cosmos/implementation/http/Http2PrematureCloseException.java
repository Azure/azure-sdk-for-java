// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import java.nio.channels.ClosedChannelException;

/**
 * Early H2 connection close, before a stream can send a request.
 * Uses a recognized network-failure type in place of Reactor's non-constructible PrematureCloseException.
 */
final class Http2PrematureCloseException extends ClosedChannelException {
    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        return "Connection prematurely closed BEFORE response (before receiving initial HTTP/2 SETTINGS)";
    }
}
