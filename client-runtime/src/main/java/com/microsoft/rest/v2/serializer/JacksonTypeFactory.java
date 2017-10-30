/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.serializer;

import com.fasterxml.jackson.databind.JavaType;
import com.microsoft.rest.v2.protocol.TypeFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A TypeFactory that creates Jackson-compatible JavaType types.
 */
public class JacksonTypeFactory implements TypeFactory {
    private final com.fasterxml.jackson.databind.type.TypeFactory typeFactory;

    /**
     * Create a new JacksonTypeFactory.
     * @param typeFactory The internal Jackson-specific TypeFactory that will be used.
     */
    public JacksonTypeFactory(com.fasterxml.jackson.databind.type.TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    @Override
    public JavaType create(Type type) {
        JavaType result;
        if (type == null) {
            result = null;
        }
        else if (type instanceof JavaType) {
            result = (JavaType) type;
        }
        else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            result = create(parameterizedType, actualTypeArguments);
        }
        else {
            result = typeFactory.constructType(type);
        }
        return result;
    }

    @Override
    public JavaType create(ParameterizedType baseType, Type genericType) {
        return create(baseType, new Type[]{genericType});
    }

    @Override
    public JavaType create(ParameterizedType baseType, Type[] genericTypes) {
        final Class<?> rawType = (Class<?>) baseType.getRawType();

        final JavaType[] genericJavaTypes = new JavaType[genericTypes.length];
        for (int i = 0; i < genericJavaTypes.length; ++i) {
            genericJavaTypes[i] = create(genericTypes[i]);
        }

        return typeFactory.constructParametricType(rawType, genericJavaTypes);
    }
}
