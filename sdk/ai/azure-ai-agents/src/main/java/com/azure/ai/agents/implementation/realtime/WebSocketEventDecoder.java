// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.realtime;

import java.io.IOException;

/**
 * Decodes a raw message in a protocol session client.
 * @param <T> the decoded event type.
 */
@FunctionalInterface
public interface WebSocketEventDecoder<T> {
    T decode(String json) throws IOException;
}
