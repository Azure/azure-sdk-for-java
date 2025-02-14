// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

import io.clientcore.core.instrumentation.InstrumentationAttributes;

import java.util.Objects;

/**
 * Noop implementation of {@link InstrumentationAttributes}.
 */
public final class NoopAttributes implements InstrumentationAttributes {
    public static final NoopAttributes INSTANCE = new NoopAttributes();

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationAttributes put(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");

        return this;
    }

    private NoopAttributes() {
    }
}
