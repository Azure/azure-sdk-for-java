// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.NoSuchElementException;

/**
 * The Option type, every Option is either Some and contains a value including null,
 * or None and does not contain any value.
 *
 * @param <T> The value type.
 */
public final class Option<T> {
    private static final Option<?> EMPTY = new Option<>();
    private final boolean isNone;
    private final T value;

    /**
     * An {@link Option} instance with no value.
     *
     * @param <T> The value type.
     * @return An Option type representing no value.
     */
    public static <T> Option<T> none() {
        @SuppressWarnings("unchecked")
        Option<T> none = (Option<T>) EMPTY;
        return none;
    }

    /**
     * An {@link Option} instance wrapping some value.
     *
     * @param value The value to wrap.
     * @param <T> The value type.
     * @return An Option with value wrapped.
     */
    public static <T> Option<T> some(T value) {
        return new Option<>(value);
    }

    /**
     * Check whether the {@link Option} is None.
     *
     * @return {@code true} if option is None, false otherwise.
     */
    public boolean isNone() {
        return this.isNone;
    }

    /**
     * Gets the value in the {@link Option}.
     *
     * @return The value.
     * @throws NoSuchElementException thrown if the {@link Option} is None.
     */
    public T getValue() {
        if (this.isNone) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    private Option() {
        this.isNone = true;
        this.value = null;
    }

    private Option(T value) {
        this.isNone = false;
        this.value = value;
    }
}
