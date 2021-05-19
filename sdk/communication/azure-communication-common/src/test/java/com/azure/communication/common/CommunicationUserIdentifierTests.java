// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CommunicationUserIdentifierTests {

    final String id = "some id";

    @Test
    public void constructWithNullOrEmptyIdShouldThrow() {
        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationUserIdentifier(null), "The initialization parameter [id] cannot be null");

        assertThrows(IllegalArgumentException.class, () ->
            new CommunicationUserIdentifier(""), "The initialization parameter [id] cannot be empty");
    }

    @Test
    public void compareEqualUserIdentifiers() {
        CommunicationUserIdentifier identifier1 =  new CommunicationUserIdentifier(id);
        CommunicationUserIdentifier identifier2 =  new CommunicationUserIdentifier(id);

        assertTrue(identifier1.equals(identifier1));
        assertTrue(identifier1.equals(identifier2));
    }

    @Test
    public void compareWithNonUserIdentifier() {
        CommunicationUserIdentifier identifier1 =  new CommunicationUserIdentifier(id);
        Object identifier2 =  new Object();

        assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void constructWithValidId() {
        CommunicationUserIdentifier result =  new CommunicationUserIdentifier(id);

        assertNotNull(result.getId());
        assertNotNull(result.hashCode());
    }
}
