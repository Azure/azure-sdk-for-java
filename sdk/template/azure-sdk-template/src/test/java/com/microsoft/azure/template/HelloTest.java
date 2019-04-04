// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.template;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloTest {
    @Test
    public void testMessage() {
        assertEquals("hello", (new Hello()).getMessage());
    }
}
