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

    static void setPropertiesFromGetters(TableEntity entity, ClientLogger logger) {
        Class<?> myClass = entity.getClass();
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

            int prefixLength = m.getName().startsWith("get") ? 3 : 2;
            String propName = m.getName().substring(prefixLength);

            try {
                entity.getProperties().put(propName, m.invoke(entity));
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                logger.logThrowableAsWarning(new ReflectiveOperationException(String.format(
                    "Failed to get property '%s' on type '%s'", propName, myClass.getName()), e));
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends TableEntity> T convertToSubclass(TableEntity entity, Class<T> clazz, ClientLogger logger) {
        if (TableEntity.class == clazz) {
            return (T) entity;
        }

        T result;
        try {
            result = clazz.getDeclaredConstructor(String.class, String.class).newInstance(entity.getPartitionKey(),
                entity.getRowKey());
        } catch (ReflectiveOperationException | SecurityException e) {
            logger.logThrowableAsWarning(new ReflectiveOperationException(String.format(
                "Failed to instantiate type '%s'", clazz.getName()), e));
            return null;
        }

        result.addProperties(entity.getProperties());

        for (Method m : clazz.getMethods()) {
            // Skip any non-setter methods
            if (m.getName().length() < 4
                || !m.getName().startsWith("set")
                || m.getParameterTypes().length != 1
                || !void.class.equals(m.getReturnType())) {
                continue;
            }

            String propName = m.getName().substring(3);
            Object value = result.getProperties().get(propName);
            if (value == null) {
                continue;
            }

            Class<?> paramType = m.getParameterTypes()[0];
            if (paramType.isEnum() && value instanceof String) {
                try {
                    value = Enum.valueOf(paramType.asSubclass(Enum.class), (String) value);
                } catch (IllegalArgumentException e) {
                    logger.logThrowableAsWarning(new IllegalArgumentException(String.format(
                        "Failed to convert '%s' to value of enum '%s'", propName, paramType.getName()), e));
                }
            }

            try {
                m.invoke(result, value);
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                logger.logThrowableAsWarning(new ReflectiveOperationException(String.format(
                    "Failed to set property '%s' on type '%s'", propName, clazz.getName()), e));
            }
        }

        return result;
    }
}
