package com.microsoft.rest.serializer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.microsoft.rest.protocol.TypeFactory;
import org.omg.Dynamic.Parameter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class JacksonTypeFactory implements TypeFactory {
    private final com.fasterxml.jackson.databind.type.TypeFactory typeFactory;

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
            result = (JavaType)type;
        }
        else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Class<?> baseType = (Class<?>)parameterizedType.getRawType();
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            result = create(baseType, actualTypeArguments);
        }
        else {
            result = typeFactory.constructType(type);
        }
        return result;
    }

    @Override
    public JavaType create(Class<?> baseType, Type genericType) {
        return create(baseType, new Type[] { genericType });
    }

    @Override
    public JavaType create(Class<?> baseType, Type[] genericTypes) {
        final JavaType[] genericJavaTypes = new JavaType[genericTypes.length];
        for (int i = 0; i < genericJavaTypes.length; ++i) {
            genericJavaTypes[i] = create(genericTypes[i]);
        }
        final TypeBindings typeBindings = TypeBindings.create(baseType, genericJavaTypes);
        return typeFactory.constructType(baseType, typeBindings);
    }
}
