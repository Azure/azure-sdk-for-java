package com.azure.cosmos.implementation.guava25.hash;

import com.azure.cosmos.implementation.guava25.base.Supplier;

/**
 * Explicitly named subinterface of {@link Supplier} that can be marked {@literal @}{@link
 * Immutable}.
 */
interface ImmutableSupplier<T> extends Supplier<T> {}
