// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.exception.UnexpectedLengthException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link LengthValidatingInputStream}.
 */
public class LengthValidatingInputStreamTests {
    @Test
    public void nullInnerInputStreamThrows() {
        assertThrows(NullPointerException.class, () -> new LengthValidatingInputStream(null, 0));
    }

    @Test
    public void negativeExpectedReadSizeThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new LengthValidatingInputStream(new ByteArrayInputStream(new byte[0]), -1));
    }

    @Test
    public void innerStreamIsTooSmall() {
        InputStream inner = new ByteArrayInputStream(new byte[4095]);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        assertThrows(UnexpectedLengthException.class, () -> validatorStream.read(new byte[4096]));
    }

    @Test
    public void innerStreamIsTooLarge() {
        InputStream inner = new ByteArrayInputStream(new byte[4097]);
        InputStream validatorStream = new LengthValidatingInputStream(inner, 4096);

        assertThrows(UnexpectedLengthException.class, () -> validatorStream.read(new byte[4096]));
    }
}
