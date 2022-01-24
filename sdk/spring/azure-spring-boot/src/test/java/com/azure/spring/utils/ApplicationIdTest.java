// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ApplicationIdTest {

    @Test
    public void testPomVersion() {
        Assertions.assertNotNull(ApplicationId.VERSION);
        Assertions.assertNotEquals("unknown", ApplicationId.VERSION);
    }
}
