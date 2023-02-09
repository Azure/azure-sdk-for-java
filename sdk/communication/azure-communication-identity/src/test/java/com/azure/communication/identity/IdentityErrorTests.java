// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;

import com.azure.communication.identity.models.IdentityError;

public class IdentityErrorTests {
    @Test
    public void communicationTokenScopeCanBeExtended() {
        // Arrange & Action
        IdentityError nestedIdentityError = new IdentityError("nestedMessage", "nestedCode", "nestedTarget", null);
        IdentityError actual = new IdentityError("message", "code", "target", new ArrayList<IdentityError>() {
            {
                add(nestedIdentityError);
            }
        });

        // Assert
        assertEquals("code", actual.getCode());
        assertEquals("message", actual.getMessage());
        assertEquals("target", actual.getTarget());
        assertEquals(1, actual.getDetails().size());
        assertEquals("nestedCode", actual.getDetails().get(0).getCode());
        assertEquals("nestedMessage", actual.getDetails().get(0).getMessage());
        assertEquals("nestedTarget", actual.getDetails().get(0).getTarget());
    }
}
