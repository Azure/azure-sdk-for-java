// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.util;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Base implementation for expandable, single string enums.
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    private static ConcurrentMap<String, ? extends ExpandableStringEnum<?>> valuesByName = null;

    private String name;
    private Class<T> clazz;

    private static String uniqueKey(Class<?> clazz, String name) {
        if (clazz != null) {
            return (clazz.getName() + "#" + name).toLowerCase();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @SuppressWarnings("unchecked")
    T withNameValue(String name, T value, Class<T> clazz) {
        if (valuesByName == null) {
            valuesByName = new ConcurrentHashMap<String, T>();
        }
        this.name = name;
        this.clazz = clazz;
        ((ConcurrentMap<String, T>) valuesByName).put(uniqueKey(clazz, name), value);
        return (T) this;
    }

    /**
     * Creates an instance of the specific expandable string enum from a String.
     * @param name the value to create the instance from
     * @param clazz the class of the expandable string enum
     * @param <T> the class of the expandable string enum
     * @return the expandable string enum instance
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ExpandableStringEnum<T>> T fromString(String name, Class<T> clazz) {
        if (name == null) {
            return null;
        } else if (valuesByName != null) {
            T value = (T) valuesByName.get(uniqueKey(clazz, name));
            if (value != null) {
                return value;
            }
        }

        try {
            T value = clazz.newInstance();
            return value.withNameValue(name, value, clazz);
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    /**
     * Gets a collection of all known values to an expandable string enum type.
     * @param clazz the class of the expandable string enum
     * @param <T> the class of the expandable string enum
     * @return a collection of all known values
     */
    @SuppressWarnings("unchecked")
    protected static <T extends ExpandableStringEnum<T>> Collection<T> values(Class<T> clazz) {
        // Make a copy of all values
        Collection<? extends ExpandableStringEnum<?>> values = new ArrayList<>(valuesByName.values());

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
        if (!clazz.isAssignableFrom(obj.getClass())) {
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
