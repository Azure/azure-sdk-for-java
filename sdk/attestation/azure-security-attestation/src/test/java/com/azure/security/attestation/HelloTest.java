// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.azure.security.attestation.Hello;
import org.junit.jupiter.api.Test;

public class HelloTest {
    @Test
    public void testMessage() {
        assertEquals("hello", (new Hello()).getMessage());
    }
}

