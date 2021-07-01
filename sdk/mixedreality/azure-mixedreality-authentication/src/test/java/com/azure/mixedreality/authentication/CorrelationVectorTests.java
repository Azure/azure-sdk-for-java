// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CorrelationVectorTests {
    @Test
    public void generateCvBase() {
        String actual = CorrelationVector.generateCvBase();

        assertNotNull(actual);
        assertEquals(22, actual.length());
    }

    @Test
    public void generateCvBaseFromUUID() {
        UUID seedUuid = UUID.fromString("0d0cddc7-4eb1-4791-9870-b1a7413cecdf");
        String actual = CorrelationVector.generateCvBaseFromUUID(seedUuid);

        assertEquals("DQzdx06xR5GYcLGnQTzs3w", actual);
    }
}
