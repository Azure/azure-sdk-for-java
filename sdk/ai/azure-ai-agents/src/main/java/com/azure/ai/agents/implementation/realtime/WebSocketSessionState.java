// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

/**
 * Connection lifecycle shared by the blocking and asynchronous WebSocket transports.
 */
public enum WebSocketSessionState {
    NEW, CONNECTING, OPEN, CLOSING, CLOSED
}
