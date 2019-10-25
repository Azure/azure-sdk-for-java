// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base implementation for expandable, single string enums.
 *
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    private static final ConcurrentMap<String,
        ? extends ExpandableStringEnum<?>> VALUES_BY_NAME = new ConcurrentHashMap<>();

    private String name;
    private Class<T> clazz;

    private static String uniqueKey(Class<?> clazz, String name) {
        if (clazz != null) {
            return (clazz.getName() + "#" + name).toLowerCase(Locale.ROOT);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    T nameValue(String name, T value, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
        ((ConcurrentMap<String, T>) VALUES_BY_NAME).put(uniqueKey(clazz, name), value);
        return (T) this;
    }

    /**
     * Creates an instance of the specific expandable string enum from a String.
     *
     * @param name The value to create the instance from.
     * @param clazz The class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return The expandable string enum instance.
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ExpandableStringEnum<T>> T fromString(String name, Class<T> clazz) {
        if (name == null) {
            return null;
        } else {
            T value = (T) VALUES_BY_NAME.get(uniqueKey(clazz, name));
            if (value != null) {
                return value;
            }
        }

        try {
            T value = clazz.newInstance();
            return value.nameValue(name, value, clazz);
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
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
        // Make a copy of all values
        Collection<? extends ExpandableStringEnum<?>> values = new ArrayList<>(VALUES_BY_NAME.values());

        Collection<T> list = new HashSet<T>();
        for (ExpandableStringEnum<?> value : values) {
            if (value.getClass().isAssignableFrom(clazz)) {
                list.add((T) value);
            }
        }

        return list;
    }

    @Override
    @JsonValue
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return uniqueKey(this.clazz, this.name).hashCode();
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
