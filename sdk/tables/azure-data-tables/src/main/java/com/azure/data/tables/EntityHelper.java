// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.models.TableEntity;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

final class EntityHelper {
    private static final HashSet<String> TABLE_ENTITY_METHODS = Arrays.stream(TableEntity.class.getMethods())
        .map(Method::getName).collect(Collectors.toCollection(HashSet::new));

    private EntityHelper() {
    }

    // Given a subclass of `TableEntity`, locate all getter methods (those that start with `get` or `is`, take no
    // parameters, and produce a non-void value) and add their values to the properties map
    static void setPropertiesFromGetters(TableEntity entity, ClientLogger logger) {
        Class<?> myClass = entity.getClass();

        // Do nothing if the entity is actually a `TableEntity` rather than a subclass
        if (myClass == TableEntity.class) {
            return;
        }

        for (Method m : myClass.getMethods()) {
            // Skip any non-getter methods
            if (m.getName().length() < 3
                || TABLE_ENTITY_METHODS.contains(m.getName())
                || (!m.getName().startsWith("get") && !m.getName().startsWith("is"))
                || m.getParameterTypes().length != 0
                || void.class.equals(m.getReturnType())) {
                continue;
            }

            // A method starting with `is` is only a getter if it returns a boolean
            if (m.getName().startsWith("is") && m.getReturnType() != Boolean.class
                && m.getReturnType() != boolean.class) {
                continue;
            }

            // Remove the `get` or `is` prefix to get the name of the property
            int prefixLength = m.getName().startsWith("is") ? 2 : 3;
            String propName = m.getName().substring(prefixLength);

            try {
                // Invoke the getter and store the value in the properties map
                entity.getProperties().put(propName, m.invoke(entity));
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                logger.logThrowableAsWarning(new ReflectiveOperationException(String.format(
                    "Failed to get property '%s' on type '%s'", propName, myClass.getName()), e));
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends TableEntity> T convertToSubclass(TableEntity entity, Class<T> clazz, ClientLogger logger) {
        // Do nothing if the entity is actually a `TableEntity` rather than a subclass
        if (TableEntity.class == clazz) {
            return (T) entity;
        }

        T result;
        try {
            // Create a new instance of the provided `TableEntity` subclass by calling its two-argument constructor that
            // accepts the partitionKey and rowKey. If the developer implemented their own custom constructor instead,
            // this will fail.
            result = clazz.getDeclaredConstructor(String.class, String.class).newInstance(entity.getPartitionKey(),
                entity.getRowKey());
        } catch (ReflectiveOperationException | SecurityException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "Failed to instantiate type '%s'. It must contain a constructor that accepts two arguments: "
                    + "the partition key and row key.", clazz.getName()), e));
        }

        // Copy all of the properties from the provided `TableEntity` into the new instance
        result.setProperties(entity.getProperties());

        for (Method m : clazz.getMethods()) {
            // Skip any non-setter methods
            if (m.getName().length() < 4
                || !m.getName().startsWith("set")
                || m.getParameterTypes().length != 1
                || !void.class.equals(m.getReturnType())) {
                continue;
            }

            // Remove the `set` prefix to get the name of the property
            String propName = m.getName().substring(3);

            // Skip this setter if the properties map doesn't contain a matching property
            Object value = result.getProperties().get(propName);
            if (value == null) {
                continue;
            }

            // If the setter accepts an enum parameter and the property's value is a string, attempt to convert the
            // value to an instance of that enum type. Enums are serialized as strings using their 'name' which is the
            // string representation of the enum value, regardless of whether they contain associated values or whether
            // their `toString` method has been overridden by the developer.
            Class<?> paramType = m.getParameterTypes()[0];
            if (paramType.isEnum() && value instanceof String) {
                try {
                    value = Enum.valueOf(paramType.asSubclass(Enum.class), (String) value);
                } catch (IllegalArgumentException e) {
                    logger.logThrowableAsWarning(new IllegalArgumentException(String.format(
                        "Failed to convert '%s' to value of enum '%s'", propName, paramType.getName()), e));
                    continue;
                }
            }

            try {
                // Invoke the setter with the value of the property
                m.invoke(result, value);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                logger.logThrowableAsWarning(new ReflectiveOperationException(String.format(
                    "Failed to set property '%s' on type '%s'", propName, clazz.getName()), e));
            }
        }

        return result;
    }
}
