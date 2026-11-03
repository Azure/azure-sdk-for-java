// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import java.io.IOException;

/**
 * Decodes a protocol event received by either WebSocket transport.
 * @param <T> the decoded event type.
 */
@FunctionalInterface
public interface WebSocketEventDecoder<T> {
    T decode(String json) throws IOException;
}
