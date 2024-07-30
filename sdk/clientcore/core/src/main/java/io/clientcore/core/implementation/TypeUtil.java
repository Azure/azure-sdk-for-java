// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility type exposing methods to deal with {@link Type}.
 */
public final class TypeUtil {
    private static final Map<Type, Type> SUPER_TYPE_MAP = new ConcurrentHashMap<>();

    /**
     * Find all super classes including provided class.
     *
     * @param clazz The raw class to find super types for.
     *
     * @return The list of super classes.
     */
    public static List<Class<?>> getAllClasses(Class<?> clazz) {
        List<Class<?>> types = new ArrayList<>();

        while (clazz != null) {
            types.add(clazz);

            clazz = clazz.getSuperclass();
        }

        return types;
    }

    /**
     * Get the generic arguments for a type.
     *
     * @param type The type to get arguments.
     *
     * @return The generic arguments, empty if type is not parameterized.
     */
    public static Type[] getTypeArguments(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return new Type[0];
        }

        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * Get the generic argument, or the first if the type has more than one.
     *
     * @param type The type to get arguments.
     *
     * @return The generic argument, null if type is not parameterized.
     */
    public static Type getTypeArgument(Type type) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    /**
     * Get the raw class for a given type.
     *
     * @param type The input type.
     *
     * @return The raw class.
     */
    public static Class<?> getRawClass(Type type) {
        if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            return (Class<?>) type;
        }
    }

    /**
     * Get the super type for a given type.
     *
     * @param type The input type.
     *
     * @return The direct super type.
     */
    public static Type getSuperType(final Type type) {
        Type superType = SUPER_TYPE_MAP.get(type);

        if (superType != null) {
            return superType;
        }

        return SUPER_TYPE_MAP.computeIfAbsent(type, _type -> {
            if (type instanceof ParameterizedType) { // Like Response<?>
                final ParameterizedType parameterizedType = (ParameterizedType) type;
                final Type genericSuperClass = ((Class<?>) parameterizedType.getRawType()).getGenericSuperclass();

                if (genericSuperClass instanceof ParameterizedType) {
                    /*
                     * Find erased generic types for the super class and replace
                     * with actual type arguments from the parameterized type
                     */
                    final Type[] superTypeArguments = getTypeArguments(genericSuperClass);
                    final Type[] typeParameters =
                        ((GenericDeclaration) parameterizedType.getRawType()).getTypeParameters();
                    int k = 0;

                    for (int i = 0; i != superTypeArguments.length; i++) {
                        for (int j = 0; i < typeParameters.length; j++) {
                            if (typeParameters[j].equals(superTypeArguments[i])) {
                                superTypeArguments[i] = parameterizedType.getActualTypeArguments()[k++];

                                break;
                            }
                        }
                    }

                    return createParameterizedType(((ParameterizedType) genericSuperClass).getRawType(),
                        superTypeArguments);
                } else {
                    return genericSuperClass;
                }
            } else if (((Class<?>) type).isInterface()) {
                Type[] types = ((Class<?>) type).getGenericInterfaces();

                for (Type t : types) {
                    if (t instanceof ParameterizedType) { // Look if there's something like Response<?>
                        return t;
                    }
                }

                return null; // Return null for this interface
            } else {
                return ((Class<?>) type).getGenericSuperclass(); // Returns null for an interface
            }
        });
    }

    /**
     * Returns whether the {@code type}, or its generic raw type, implements the interface.
     *
     * @param type Type to check if it implements an interface.
     * @param interfaceClass The interface.
     *
     * @return Whether the type implements the interface.
     */
    public static boolean typeImplementsInterface(Type type, Class<?> interfaceClass) {
        if (getRawClass(type) == interfaceClass) {
            return true; // Type is already the interface.
        } else if (type instanceof ParameterizedType) {
            return typeImplementsInterface(((ParameterizedType) type).getRawType(), interfaceClass);
        } else {
            Class<?> clazz = (Class<?>) type;

            return Arrays.stream(clazz.getInterfaces())
                .anyMatch(implementedInterface -> implementedInterface == interfaceClass);
        }
    }

    /**
     * Get the super type for a type in its super type chain, which has a raw class that matches the specified class.
     *
     * @param subType The subtype to find super type for.
     * @param rawSuperType The raw class for the super type.
     *
     * @return The super type that matches the requirement.
     */
    public static Type getSuperType(Type subType, Class<?> rawSuperType) {
        while (subType != null && getRawClass(subType) != rawSuperType) {
            subType = getSuperType(subType);
        }

        return subType;
    }

    /**
     * Determines if a type is the same or a subtype for another type.
     *
     * @param subType The supposed subtype.
     * @param superType The supposed super type.
     *
     * @return {@code true} if the first type is the same or a subtype for the second type.
     */
    public static boolean isTypeOrSubTypeOf(Type subType, Type superType) {
        return getRawClass(superType).isAssignableFrom(getRawClass(subType));
    }

    /**
     * Create a parameterized type from a raw class and its type arguments.
     *
     * @param rawClass The raw class to construct the parameterized type.
     * @param genericTypes The generic arguments.
     *
     * @return The parameterized type.
     */
    public static ParameterizedType createParameterizedType(Type rawClass, Type... genericTypes) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return genericTypes;
            }

            @Override
            public Type getRawType() {
                return rawClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * Returns the body type expected in the REST response.
     *
     * @param restResponseReturnType The REST response subtype containing the type arguments we are inspecting.
     *
     * @return The type of the body.
     */
    public static Type getRestResponseBodyType(Type restResponseReturnType) {
        // If this type has type arguments, then we look at the last one to determine if it expects a body
        final Type[] restResponseTypeArguments = TypeUtil.getTypeArguments(restResponseReturnType);

        if (restResponseTypeArguments != null && restResponseTypeArguments.length > 0) {
            return restResponseTypeArguments[restResponseTypeArguments.length - 1];
        } else {
            // No generic type on this RestResponse subtype, so we go up to parent
            return getRestResponseBodyType(TypeUtil.getSuperType(restResponseReturnType));
        }
    }

    // Private constructor
    private TypeUtil() {
    }
}
