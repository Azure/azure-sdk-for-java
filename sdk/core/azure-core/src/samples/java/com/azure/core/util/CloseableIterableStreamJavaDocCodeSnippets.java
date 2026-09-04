// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.io.BufferedReader;
import java.io.StringReader;

/**
 * Code snippets for {@link CloseableIterableStream}.
 */
public class CloseableIterableStreamJavaDocCodeSnippets {
    /**
     * Iterates over a server-sent event response and closes its associated resource.
     */
    public void iterateServerSentEventResponse() {
        // BEGIN: com.azure.core.util.closeableIterableStream.iterate
        BufferedReader responseBody = getResponseBody();
        Iterable<String> eventData = parseEventData(responseBody);

        try (CloseableIterableStream<String> events = new CloseableIterableStream<>(eventData, responseBody)) {
            for (String event : events) {
                System.out.printf("Event data: %s%n", event);
            }
        }
        // END: com.azure.core.util.closeableIterableStream.iterate
    }

    private BufferedReader getResponseBody() {
        return new BufferedReader(new StringReader("data: event data\n\n"));
    }

    private Iterable<String> parseEventData(BufferedReader responseBody) {
        return () -> responseBody.lines()
            .filter(line -> line.startsWith("data:"))
            .map(line -> line.substring("data:".length()).trim())
            .iterator();
    }
}
