// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpLogOptionsTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testMaxApplicationId() {
        assertThrows(IllegalArgumentException.class,
            () -> new HttpLogOptions().setApplicationId("AppId-0123456789012345678912345"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testApplicationIdContainsSpace() {
        assertThrows(IllegalArgumentException.class, () -> new HttpLogOptions().setApplicationId("AppId 78912345"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSetApplicationId() {
        String expected = "AzCopy/10.0.4-Preview";
        assertEquals(expected, new HttpLogOptions().setApplicationId(expected).getApplicationId());
    }

    @Test
    public void testSetPrettyPrintBody() {
        assertTrue(new HttpLogOptions().setPrettyPrintBody(true).isPrettyPrintBody());
    }
}
