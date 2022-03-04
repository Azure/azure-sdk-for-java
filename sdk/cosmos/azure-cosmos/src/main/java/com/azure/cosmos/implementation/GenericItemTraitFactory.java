// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class GenericItemTraitFactory {
    private static ConcurrentMap<Class<?>, Function<ObjectNode, GenericItemTrait<?>>> factoryMethods =
        new ConcurrentHashMap<>();

    /**
     * Registers a factory method for a GenericItemTrait
     *
     * @param classOfT - the concrete type implementing GenericItemTrait
     * @param factoryMethod - The factory method used to create new items
     * @return true if teh registration was successful, false if a previous registration exists
     */
    public static <T extends GenericItemTrait<?>> boolean registerFactoryMethod(
        Class<T> classOfT,
        Function<ObjectNode, GenericItemTrait<?>> factoryMethod
    ) {
        checkNotNull(classOfT, "Argument 'classOfT' must not be null");
        checkNotNull(factoryMethod, "Argument 'factoryMethod' must not be null");
        return factoryMethods.putIfAbsent(classOfT, factoryMethod) == null;
    }

    @SuppressWarnings("unchecked")
    public static <T extends GenericItemTrait<?>> T createInstance(ObjectNode objectNode, Class<T> classOfT) {
        checkNotNull(objectNode, "Argument 'objectNode' must not be null");
        checkNotNull(classOfT, "Argument 'classOfT' must not be null");
        Function<ObjectNode, GenericItemTrait<?>> factoryMethod = factoryMethods.get(classOfT);
        if (factoryMethod != null) {
            return (T) factoryMethod.apply(objectNode);
        } else if (Resource.class.isAssignableFrom(classOfT)) {
            return (T) JsonSerializable.instantiateFromObjectNodeAndType(objectNode, classOfT);
        } else {
            throw new IllegalArgumentException("No factory method exists for class " + classOfT.getName());
        }
    }
}
