// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpLogOptionsTest {

    @Test
    public void testMaxApplicationId() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new HttpLogOptions().setApplicationId("AppId-0123456789012345678912345"));
    }

    @Test
    public void testApplicationIdContainsSpace() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> new HttpLogOptions().setApplicationId("AppId 78912345"));
    }

    @Test
    public void testSetApplicationId() {
        String expected = "AzCopy/10.0.4-Preview";
        assertEquals(expected, new HttpLogOptions().setApplicationId(expected).getApplicationId());
    }
}
