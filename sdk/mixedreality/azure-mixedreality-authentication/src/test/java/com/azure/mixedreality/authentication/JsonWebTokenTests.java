// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class JsonWebTokenTests {
    @Test
    public void retrieveExpiration() {
        // Note: The trailing "." on the end indicates an empty signature indicating that this JWT is not signed.
        final String jwtValue =
            "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJlbWFpbCI6IkJvYkBjb250b3NvLmNvbSIsImdpdmVuX25hbWUiOiJCb2IiLCJpc3MiOiJodHRwOi8vRGVmYXVsdC5Jc3N1ZXIuY29tIiwiYXVkIjoiaHR0cDovL0RlZmF1bHQuQXVkaWVuY2UuY29tIiwiaWF0IjoiMTYxMDgxMjI1MCIsIm5iZiI6IjE2MTA4MTI1NTAiLCJleHAiOiIxNjEwODk4NjUwIn0.";
        final long expectedExpirationTimestamp = 1610898650; // 1/17/2021 3:50:50 PM UTC

        OffsetDateTime actual = JsonWebToken.retrieveExpiration(jwtValue);

        assertNotNull(actual);

        long actualTimestamp = actual.toEpochSecond();

        assertEquals(expectedExpirationTimestamp, actualTimestamp);
    }

    @Test
    public void retrieveExpirationWithBadJwt() {
        OffsetDateTime actual = JsonWebToken.retrieveExpiration("asdfasdfasdf");

        assertNull(actual);
    }
}
