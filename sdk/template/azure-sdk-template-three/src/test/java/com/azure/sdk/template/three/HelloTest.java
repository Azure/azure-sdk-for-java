// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.sdk.template.three;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// This is a simple unit test for the Hello class, verifying that the getMessage method returns "hello".
public class HelloTest {
    @Test
    public void testMessage() {
        assertEquals("hello", (new Hello()).getMessage());
    }
}
