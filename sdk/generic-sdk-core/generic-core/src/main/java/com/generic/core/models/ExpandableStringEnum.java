// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import com.generic.core.implementation.ReflectionUtils;
import com.generic.core.implementation.ReflectiveInvoker;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.logging.LogLevel;

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
    private static final Map<Class<?>, ReflectiveInvoker> CONSTRUCTORS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ConcurrentHashMap<String, ? extends ExpandableStringEnum<?>>> VALUES
        = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(ExpandableStringEnum.class);
    private String name;
    private Class<T> clazz;

    /**
     * Creates an instance of the specific expandable string enum from a String.
     *
     * @param name The value to create the instance from.
     * @param clazz The class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return The expandable string enum instance.
     *
     * @throws RuntimeException wrapping implementation class constructor exception (if any is thrown).
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
            if (CONSTRUCTORS.size() > 10000) {
                CONSTRUCTORS.clear();
            }

            ReflectiveInvoker ctor = CONSTRUCTORS.computeIfAbsent(clazz, ExpandableStringEnum::getDefaultConstructor);

            if (ctor == null) {
                // logged in ExpandableStringEnum::getDefaultConstructor
                return null;
            }

            try {
                value = (T) ctor.invokeWithArguments(null);
            } catch (Exception e) {
                LOGGER.log(LogLevel.WARNING,
                    () -> "Failed to create " + clazz.getName() + ", default constructor threw exception", e);
                return null;
            }

            return value.nameAndAddValue(name, value, clazz);
        }
    }

    private static <T> ReflectiveInvoker getDefaultConstructor(Class<T> clazz) {
        try {
            return ReflectionUtils.getConstructorInvoker(clazz, clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Can't find or access default constructor for " + clazz.getName()
                + ", make sure corresponding package is open to azure-core", e);
        } catch (Exception e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to get default constructor for " + clazz.getName(), e);
        }

        return null;
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
