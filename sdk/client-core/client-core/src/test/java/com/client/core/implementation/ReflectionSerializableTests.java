// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.implementation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests {@link ReflectionSerializable}.
 * <p>
 * {@code client-core}, at the time this test class was created, doesn't have a runtime or test dependency on either
 * {@code client-json} or {@code client-xml}, so the expectation is that neither
 * {@link ReflectionSerializable#supportsJsonSerializable(Class)} or
 * {@link ReflectionSerializable#supportsXmlSerializable(Class)} should return true. On the other hand,
 * {@code client-core-experimental} has a test dependency on both libraries where these should return true when a class
 * implementing the specific serializable interface ({@code JsonSerializable} or {@code XmlSerializable}) is passed.
 * {@code client-core-experimental} contains tests for these code paths to ensure correct functionality.
 */
public class ReflectionSerializableTests {
    @Test
    public void supportsXmlSerializableIsFalse() {
        assertFalse(ReflectionSerializable.XML_SERIALIZABLE_SUPPORTED);
    }
}
