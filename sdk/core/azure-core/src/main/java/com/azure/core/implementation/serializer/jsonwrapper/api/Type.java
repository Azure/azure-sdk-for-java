// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.api;

import java.lang.reflect.ParameterizedType;

public abstract class Type<T> {

    private final java.lang.reflect.Type javaType;

    /**
     * Constructor
     * @throws IllegalArgumentException if the reference is constructed without actual type information
     */
    public Type() {
        java.lang.reflect.Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException(
                "Internal error: TypeReference constructed without actual type information");
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

    /**
     * Returns is the type a ParameterizedType (collection, string etc.)
     * @return boolean value
     */
    public boolean isParameterizedType() {
        if (!(javaType instanceof ParameterizedType)) {
            return false;
        }

        return true;
    }

    /**
     * Get the type of the actual type of the Type object
     * @return ava.lang.reflect.Type
     */
    public java.lang.reflect.Type getListType() {
        assert isParameterizedType();
        ParameterizedType parameterizedType = (ParameterizedType) javaType;
        return parameterizedType.getActualTypeArguments()[0];
    }
}
