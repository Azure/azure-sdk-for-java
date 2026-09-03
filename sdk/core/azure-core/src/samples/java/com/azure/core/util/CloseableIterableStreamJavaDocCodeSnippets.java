// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.ServerSentEvent;

import java.util.Collections;

/**
 * Code snippets for {@link CloseableIterableStream}.
 */
public class CloseableIterableStreamJavaDocCodeSnippets {
    /**
     * Iterates over a server-sent event response and closes its associated resource.
     */
    public void iterateServerSentEventResponse() {
        CloseableIterableStream<ServerSentEvent<String>> response = new CloseableIterableStream<>(
            Collections.singletonList(new ServerSentEvent<>(null, "message", "event data", null, null)), () -> {
            });

        // BEGIN: com.azure.core.util.closeableIterableStream.iterate
        try (CloseableIterableStream<ServerSentEvent<String>> events = response) {
            for (ServerSentEvent<String> event : events) {
                System.out.printf("Event '%s': %s%n", event.getEvent(), event.getData());
            }
        }
        // END: com.azure.core.util.closeableIterableStream.iterate
    }
}
