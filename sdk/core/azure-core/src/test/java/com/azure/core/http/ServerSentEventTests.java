// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerSentEventTests {
    @Test
    public void constructorCreatesEvent() {
        ServerSentEvent<String> event
            = new ServerSentEvent<>("42", "stockUpdate", "payload", "comment", Duration.ofSeconds(2));

        assertEquals("42", event.getId());
        assertEquals("stockUpdate", event.getEvent());
        assertEquals("payload", event.getData());
        assertEquals("comment", event.getComment());
        assertEquals(Duration.ofSeconds(2), event.getRetryAfter());
    }
}
