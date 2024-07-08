// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.implementation;

import io.clientcore.core.util.ClientLogger;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * The Option type to describe tri-state. Every Option instance is in one of the three states: a state representing a
 * non-null-value, null-value, or no-value.
 *
 * <p>
 * <strong>Code sample</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.Option -->
 * <pre>
 * &#47;&#47; An Option with non-null-value.
 * Option&lt;String&gt; skuOption = Option.of&#40;&quot;basic&quot;&#41;;
 * if &#40;skuOption.isInitialized&#40;&#41;&#41; &#123;
 *     &#47;&#47; Option.isInitialized&#40;&#41; returns true because option is initialized with a non-null value.
 *     System.out.println&#40;skuOption.getValue&#40;&#41;&#41;; &#47;&#47; print: &quot;basic&quot;
 * &#125;
 *
 * &#47;&#47; An Option with null-value.
 * Option&lt;String&gt; descriptionOption = Option.of&#40;null&#41;;
 * if &#40;descriptionOption.isInitialized&#40;&#41;&#41; &#123;
 *     &#47;&#47; Option.isInitialized&#40;&#41; returns true because option is initialized with an explicit null-value.
 *     System.out.println&#40;skuOption.getValue&#40;&#41;&#41;; &#47;&#47; print: null
 * &#125;
 *
 * &#47;&#47; An Option with no-value.
 * Option&lt;String&gt; uninitializedOption = Option.uninitialized&#40;&#41;;
 * if &#40;!uninitializedOption.isInitialized&#40;&#41;&#41; &#123;
 *     &#47;&#47; Option.isInitialized&#40;&#41; returns false because option is uninitialized.
 *     System.out.println&#40;&quot;not initialized&quot;&#41;;
 * &#125;
 *
 * &#47;&#47; Attempting to access the value when an option has no-value will throw 'NoSuchElementException'
 * try &#123;
 *     uninitializedOption.getValue&#40;&#41;;
 * &#125; catch &#40;NoSuchElementException exception&#41; &#123;
 *     System.out.println&#40;exception.getMessage&#40;&#41;&#41;; &#47;&#47; print: 'No value initialized'
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.Option -->
 *
 * @param <T> The value type.
 */
public final class Option<T> {
    private static final ClientLogger LOGGER = new ClientLogger(Option.class);
    private static final Option<?> UNINITIALIZED = new Option<>();
    private static final Option<?> EMPTY = new Option<>(null);
    private final boolean isInitialized;
    private final T value;

    /**
     * Returns an {@link Option} with the specified null-value or non-null-value.
     *
     * @param <T> The value type.
     * @param value the value.
     * @return an {@link Option} with the value present.
     */
    public static <T> Option<T> of(T value) {
        return value == null ? empty() : new Option<>(value);
    }

    /**
     * Returns an {@link Option} with null-value.
     * <p>
     * {@code Option.empty()} is a syntactic sugar for {@code Option.of(null)}.
     * </p>
     *
     * @param <T> The value type.
     * @return an {@link Option} with a null-value.
     */
    public static <T> Option<T> empty() {
        @SuppressWarnings("unchecked")
        Option<T> empty = (Option<T>) EMPTY;
        return empty;
    }

    /**
     * Returns an {@link Option} instance with no-value.
     *
     * @param <T> Type of the non-existent value.
     * @return An Option type with no-value.
     */
    public static <T> Option<T> uninitialized() {
        @SuppressWarnings("unchecked")
        Option<T> uninitialized = (Option<T>) UNINITIALIZED;
        return uninitialized;
    }

    /**
     * Return {@code true} if this instance is initialized with a null-value or non-null-value, otherwise {@code
     * false}.
     *
     * @return {@code true} if a value has been initialized, otherwise {@code false}
     */
    public boolean isInitialized() {
        return this.isInitialized;
    }

    /**
     * Gets the value in the {@link Option}.
     *
     * @return The {@code null} (null-value) or non-null-value that the {@link Option} is initialized with.
     * @throws NoSuchElementException thrown if the {@link Option} is in no-value state.
     */
    public T getValue() {
        if (!this.isInitialized) {
            throw LOGGER.logThrowableAsError(new NoSuchElementException("No value initialized"));
        }
        return this.value;
    }

    /**
     * Indicates whether some other object is "equal to" this Option. The other object is considered equal if:
     * <ul>
     * <li>it is also an {@code Option} and;
     * <li>both instances are not initialized or;
     * <li>both instances are initialized and values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Option)) {
            return false;
        }
        Option<?> other = (Option<?>) obj;
        if (this.isInitialized ^ other.isInitialized) {
            // one is 'initialized' and the other one is not.
            return false;
        }
        // both are 'initialized' or 'not-initialized'.
        return Objects.equals(value, other.value);
    }

    /**
     * Returns hash code of the value this Option is initialized with or -1 if in uninitialized state.
     * <p>
     * The value 0 will be returned when initialized with {@code null}.
     * </p>
     *
     * @return hash code of the value this Option is initialized with or -1 if in uninitialized state.
     */
    @Override
    public int hashCode() {
        if (!this.isInitialized) {
            return -1;
        }
        return Objects.hashCode(value);
    }

    private Option() {
        this.isInitialized = false;
        this.value = null;
    }

    private Option(T value) {
        this.isInitialized = true;
        this.value = value;
    }
}
