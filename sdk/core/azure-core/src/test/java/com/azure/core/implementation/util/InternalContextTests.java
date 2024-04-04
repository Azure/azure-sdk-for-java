// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for {@link InternalContext}.
 */
public class InternalContextTests {
    private static final ClientLogger LOGGER = new ClientLogger(InternalContextTests.class);

    @ParameterizedTest
    @MethodSource("missingKeyReturnsSentinelSuppler")
    public void missingKeyReturnsSentinel(InternalContext context) {
        assertSame(InternalContext.SENTINEL, context.getInternal(new Object()));
    }

    private static Stream<InternalContext> missingKeyReturnsSentinelSuppler() {
        return Stream.of(InternalContext.empty(), InternalContext.of(new Object(), new Object()),
            InternalContext.of(new Object(), new Object(), new Object(), new Object()),
            InternalContext.of(new Object(), new Object(), new Object(), new Object(), new Object(), new Object()),
            InternalContext.of(new Object(), new Object(), new Object(), new Object(), new Object(), new Object(),
                new Object(), new Object()),
            InternalContext.of(Collections.singletonMap(new Object(), new Object()), LOGGER));
    }
}
