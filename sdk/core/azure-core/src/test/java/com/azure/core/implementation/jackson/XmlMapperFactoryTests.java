// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlMapperFactoryTests {
    @Test
    public void instantiate() {
        // This should be true for running with Core only as we use Jackson 2.12+.
        assertTrue(XmlMapperFactory.INSTANCE.useReflectionToSetCoercion);
    }
}
