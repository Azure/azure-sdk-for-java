// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson.implementation;

import java.time.ZoneOffset;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TestHelpers {
    private TestHelpers() {
    }

    public static void assertDateEquals(Date expect, Date actual) {
        assertEquals(0, expect.toInstant().atOffset(ZoneOffset.UTC)
            .compareTo(actual.toInstant().atOffset(ZoneOffset.UTC)));
    }
}
