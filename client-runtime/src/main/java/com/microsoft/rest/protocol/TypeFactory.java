package com.microsoft.rest.protocol;

import java.lang.reflect.Type;

public interface TypeFactory {
    Type create(Type type);

    Type create(Class<?> baseType, Type genericType);

    Type create(Class<?> baseType, Type[] genericTypes);
}
