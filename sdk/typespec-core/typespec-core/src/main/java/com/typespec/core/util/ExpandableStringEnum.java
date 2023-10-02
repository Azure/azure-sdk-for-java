// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.implementation.ReflectionUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.invoke.MethodType.methodType;

/**
 * Base implementation for expandable, single string enums.
 *
 * @param <T> a specific expandable enum type
 */
public abstract class ExpandableStringEnum<T extends ExpandableStringEnum<T>> {
    private static final Map<Class<?>, MethodHandle> CONSTRUCTORS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ConcurrentHashMap<String, ? extends ExpandableStringEnum<?>>> VALUES
        = new ConcurrentHashMap<>();

    private static final ClientLogger LOGGER = new ClientLogger(ExpandableStringEnum.class);
    private String name;
    private Class<T> clazz;

    /**
     * Creates a new instance of {@link ExpandableStringEnum} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link ExpandableStringEnum} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String, Class)} factory method.
     */
    @Deprecated
    public ExpandableStringEnum() {
    }

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
            MethodHandle ctor = CONSTRUCTORS.computeIfAbsent(clazz, ExpandableStringEnum::getDefaultConstructor);

            if (ctor == null) {
                // logged in ExpandableStringEnum::getDefaultConstructor
                return null;
            }

            try {
                value = (T) ctor.invoke();
            } catch (Throwable e) {
                LOGGER.warning("Failed to create {}, default constructor threw exception", clazz.getName(), e);
                return null;
            }

            return value.nameAndAddValue(name, value, clazz);
        }
    }

    private static <T> MethodHandle getDefaultConstructor(Class<T> clazz) {
        try {
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(clazz);
            return lookup.findConstructor(clazz, methodType(void.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            LOGGER.verbose("Can't find or access default constructor for {}, make sure corresponding package is open to azure-core", clazz.getName(), e);
        } catch (Exception e) {
            LOGGER.verbose("Failed to get lookup for {}", clazz.getName(), e);
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
