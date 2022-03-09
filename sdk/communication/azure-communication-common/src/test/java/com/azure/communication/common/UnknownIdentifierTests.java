// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnknownIdentifierTests {

    final String id = "some id";

    @Test
    public void constructWithNullOrEmptyIdShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new UnknownIdentifier(null), "The initialization parameter [id] cannot be null");

        assertThrows(IllegalArgumentException.class, () ->
            new UnknownIdentifier(""), "The initialization parameter [id] cannot be empty");
    }

    @Test
    public void compareEqualUnknownIdentifiers() {
        UnknownIdentifier identifier1 =  new UnknownIdentifier(id);
        UnknownIdentifier identifier2 =  new UnknownIdentifier(id);

        assertTrue(identifier1.equals(identifier1));
        assertTrue(identifier1.equals(identifier2));
    }

    @Test
    public void compareWithNonUnknownIdentifier() {
        UnknownIdentifier identifier1 =  new UnknownIdentifier(id);
        Object identifier2 =  new Object();

        assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void constructWithValidId() {
        UnknownIdentifier result =  new UnknownIdentifier(id);

        assertNotNull(result.getId());
        assertNotNull(result.hashCode());
    }
}
