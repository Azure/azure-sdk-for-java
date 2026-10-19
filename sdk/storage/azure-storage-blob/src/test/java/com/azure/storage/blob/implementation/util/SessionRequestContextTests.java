// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionRequestContextTests {
    @Test
    public void requiresContainerAndAcquirer() {
        SessionAcquirer acquirer
            = new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "testaccount", null);

        assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext(null, acquirer));
        assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext("", acquirer));
        assertThrows(NullPointerException.class, () -> new SessionRequestContext("container", null));

        SessionRequestContext context = new SessionRequestContext("container", acquirer);
        assertEquals("container", context.getContainerName());
        assertSame(acquirer, context.getAcquirer());
    }

    @Test
    public void acquirerRequiresResolvedAccountName() {
        assertThrows(IllegalArgumentException.class,
            () -> new SessionAcquirer("https://testaccount.blob.core.windows.net", null, null, null));
        assertThrows(IllegalArgumentException.class,
            () -> new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "", null));
    }
}
