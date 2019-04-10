/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

/**
 * A container for a generic type. Serves a similar purpose as pointers in C/C++. It's a workaround
 * for the fact that Java doesn't allow mutation of local variables in closure.
 * @param <T>
 */
class Value<T> {
    private T value;

    /**
     * Create a new Value with inner value.
     */
    Value() {
    }

    /**
     * Create a new Value with the provided inner value.
     * @param value
     */
    Value(T value) {
        set(value);
    }

    /**
     * Get the inner value of this Value.
     * @return The inner value of this Value.
     */
    public T get() {
        return value;
    }

    /**
     * Set the inner value of this Value.
     * @param value The new inner value of this Value.
     */
    public void set(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}