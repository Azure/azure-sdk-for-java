// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base implementation for expandable, single string enums.
 *
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    private static final Map<Class<?>, ConcurrentHashMap<String, ? extends ExpandableStringEnum<?>>> VALUES
        = new ConcurrentHashMap<>();

    private String name;
    private Class<T> clazz;

    /**
     * Creates an instance of the specific expandable string enum from a String.
     *
     * @param name The value to create the instance from.
     * @param clazz The class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return The expandable string enum instance.
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    protected static <T extends ExpandableStringEnum<T>> T fromString(String name, Class<T> clazz) {
        if (name == null) {
            return null;
        }

        ConcurrentHashMap<String, ?> clazzValues = VALUES.computeIfAbsent(clazz, key -> new ConcurrentHashMap<>());
        T value = (T) clazzValues.get(name);

        if (value != null) {
            return value;
        } else {
            try {
                value = clazz.newInstance();
                return value.nameAndAddValue(name, value, clazz);
            } catch (IllegalAccessException | InstantiationException ex) {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    T nameAndAddValue(String name, T value, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;

        ((ConcurrentHashMap<String, T>) VALUES.get(clazz)).put(name, value);
        return (T) this;
    }

    /**
     * Gets a collection of all known values to an expandable string enum type.
     *
     * @param clazz the class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return A collection of all known values for the given {@code clazz}.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ExpandableStringEnum<T>> Collection<T> values(Class<T> clazz) {
        return new ArrayList<T>((Collection<T>) VALUES.getOrDefault(clazz, new ConcurrentHashMap<>()).values());
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.clazz, this.name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (clazz == null || !clazz.isAssignableFrom(obj.getClass())) {
            return false;
        } else if (obj == this) {
            return true;
        } else if (this.name == null) {
            return ((ExpandableStringEnum<T>) obj).name == null;
        } else {
            return this.name.equals(((ExpandableStringEnum<T>) obj).name);
        }
    }
}
