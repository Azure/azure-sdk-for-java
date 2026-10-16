// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SessionRequestContextTests {
    @Test
    public void requiresContainerAndAcquirerButAllowsUnresolvedAccount() {
        SessionAcquirer acquirer
            = new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "testaccount", null);

        assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext(null, null, acquirer));
        assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext("", null, acquirer));
        assertThrows(NullPointerException.class, () -> new SessionRequestContext("container", null, null));

        SessionRequestContext context = new SessionRequestContext("container", null, acquirer);
        assertEquals("container", context.getContainerName());
        assertNull(context.getAccountName());
        assertSame(acquirer, context.getAcquirer());
    }
}
