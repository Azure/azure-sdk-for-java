// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import com.azure.core.util.logging.ClientLogger;

import java.lang.reflect.ParameterizedType;

/**
 * This class represents a generic Java type, retaining information about generics.
 *
 * @param <T> The type being represented.
 */
public abstract class TypeReference<T> {
    private static final ClientLogger LOGGER = new ClientLogger(TypeReference.class);
    private static final String MISSING_TYPE = "Type constructed without type information.";

    private final java.lang.reflect.Type javaType;

    /**
     * Constructs a new {@link TypeReference} which maintains generic information.
     *
     * @throws IllegalArgumentException If the reference is constructed without type information.
     */
    public TypeReference() {
        java.lang.reflect.Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(MISSING_TYPE));
        } else {
            this.javaType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }

    /**
     * Return class T type
     * @return type
     */
    public java.lang.reflect.Type getJavaType() {
        return javaType;
    }
}
