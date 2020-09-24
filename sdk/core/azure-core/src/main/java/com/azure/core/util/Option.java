// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * The Option type, every Option is either has a value present including null,
 * or has no value present.
 *
 * @param <T> The value type.
 */
public final class Option<T> {
    private static final Option<?> UNSET = new Option<>();
    private final boolean isSet;
    private final T value;

    /**
     * Returns an {@link Option} instance with no value.
     *
     * @param <T> The value type.
     * @return An Option type with no value present.
     */
    public static <T> Option<T> unset() {
        @SuppressWarnings("unchecked")
        Option<T> none = (Option<T>) UNSET;
        return none;
    }

    /**
     * Returns an {@link Option} with an empty value.
     *
     * @param <T> The value type.
     * @return an {@link Option} with the empty value present.
     */
    public static <T> Option<T> empty() {
        return new Option<>(null);
    }

    /**
     * Returns an {@link Option} with the specified non-null value.
     *
     * @param <T> The value type.
     * @param value the value, which must be non-null.
     * @return an {@link Option} with the non-null value present.
     * @throws NullPointerException if value is null.
     */
    public static <T> Option<T> of(T value) {
        Objects.requireNonNull(value);
        return new Option<>(value);
    }

    /**
     * Returns an {@link Option} with the specified nullable value.
     *
     * @param <T> The value type.
     * @param value the null or non-null value.
     * @return an {@link Option} with the value present.
     */
    public static <T> Option<T> ofNullable(T value) {
        return new Option<>(value);
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isSet() {
        return this.isSet;
    }

    /**
     * Gets the value in the {@link Option}.
     *
     * @return The value.
     * @throws NoSuchElementException thrown if the {@link Option} has no value.
     */
    public T getValue() {
        if (!this.isSet) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    private Option() {
        this.isSet = false;
        this.value = null;
    }

    private Option(T value) {
        this.isSet = true;
        this.value = value;
    }
}
