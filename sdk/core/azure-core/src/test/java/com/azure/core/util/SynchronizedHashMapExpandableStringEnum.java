// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class SynchronizedHashMapExpandableStringEnum<T extends SynchronizedHashMapExpandableStringEnum<T>> {
    private static final Map<Class<?>, ExpandableStringEnumSubType<?>> VALUES = new HashMap<>();

    private static final Object CREATE_SUBTYPE_LOCK = new Object();

    String name;
    Class<T> clazz;

    /**
     * Creates a new instance of {@link SynchronizedHashMapExpandableStringEnum} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link SynchronizedHashMapExpandableStringEnum} which
     * doesn't have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String, Class)} factory method.
     */
    @Deprecated
    public SynchronizedHashMapExpandableStringEnum() {
    }

    /**
     * Creates an instance of the specific expandable string enum from a String.
     *
     * @param name The value to create the instance from.
     * @param clazz The class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return The expandable string enum instance.
     */
    protected static <T extends SynchronizedHashMapExpandableStringEnum<T>> T fromString(String name, Class<T> clazz) {
        if (name == null) {
            return null;
        }

        return getOrCreateSubType(clazz).getOrCreate(name);
    }

    /**
     * Gets a collection of all known values to an expandable string enum type.
     *
     * @param clazz the class of the expandable string enum.
     * @param <T> the class of the expandable string enum.
     * @return A collection of all known values for the given {@code clazz}.
     */
    protected static <T extends SynchronizedHashMapExpandableStringEnum<T>> Collection<T> values(Class<T> clazz) {
        ExpandableStringEnumSubType<T> subType = getOrCreateSubType(clazz);

        return new ArrayList<>(subType.values());
    }

    @SuppressWarnings("unchecked")
    private static <T extends SynchronizedHashMapExpandableStringEnum<T>> ExpandableStringEnumSubType<T> getOrCreateSubType(Class<T> enumType) {
        ExpandableStringEnumSubType<T> subType = (ExpandableStringEnumSubType<T>) VALUES.get(enumType);
        if (subType == null) {
            synchronized (CREATE_SUBTYPE_LOCK) {
                subType = (ExpandableStringEnumSubType<T>) VALUES.computeIfAbsent(enumType,
                    c -> new ExpandableStringEnumSubType<>(enumType));
            }
        }

        return subType;
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SynchronizedHashMapExpandableStringEnum<?>)) {
            return false;
        }

        SynchronizedHashMapExpandableStringEnum<?> other = (SynchronizedHashMapExpandableStringEnum<?>) obj;
        return Objects.equals(clazz, other.clazz) && Objects.equals(name, other.name);
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    static final class ExpandableStringEnumSubType<T extends SynchronizedHashMapExpandableStringEnum<T>> {
        private final Object createEnumLock = new Object();

        private final Class<T> enumType;
        private final Supplier<T> enumCreator;
        private final Map<String, T> enums = new HashMap<>();

        ExpandableStringEnumSubType(Class<T> enumType) {
            this.enumType = enumType;

            Supplier<T> enumCreator;
            try {
                // Attempt to use a MethodHandle.
                MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(enumType);
                Constructor<?> emptyConstructor = enumType.getDeclaredConstructor();
                MethodHandle handle = lookup.unreflectConstructor(emptyConstructor);
                enumCreator = (Supplier<T>) LambdaMetafactory.metafactory(MethodHandles.lookup(), "get",
                        MethodType.methodType(Supplier.class), MethodType.methodType(Object.class), handle,
                        handle.type())
                    .getTarget()
                    .invoke();
            } catch (Throwable throwable) {
                if (throwable instanceof Error && throwable.getClass() != LinkageError.class) {
                    throw (Error) throwable;
                }

                // Fallback to using Class.newInstance
                enumCreator = () -> {
                    try {
                        return enumType.newInstance();
                    } catch (ReflectiveOperationException ignored) {
                        return null;
                    }
                };
            }

            this.enumCreator = enumCreator;
        }

        T getOrCreate(String name) {
            T value = enums.get(name);
            if (value == null) {
                synchronized (createEnumLock) {
                    value = enums.computeIfAbsent(name, n -> {
                        T v = enumCreator.get();
                        if (v == null) {
                            return null;
                        }

                        v.name = name;
                        v.clazz = enumType;

                        return v;
                    });
                }
            }

            return value;
        }

        Collection<T> values() {
            return enums.values();
        }
    }
}
