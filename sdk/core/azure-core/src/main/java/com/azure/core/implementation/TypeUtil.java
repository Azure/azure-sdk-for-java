// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Utility type exposing methods to deal with {@link Type}.
 */
public final class TypeUtil {
    private static final Map<Type, Type> SUPER_TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<Type, Boolean> RETURN_TYPE_DECODEABLE_MAP = new ConcurrentHashMap<>();

    /**
     * Find all super classes including provided class.
     *
     * @param clazz the raw class to find super types for
     * @return the list of super classes
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
     * @param type the type to get arguments
     * @return the generic arguments, empty if type is not parameterized
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
     * @param type the type to get arguments
     * @return the generic argument, null if type is not parameterized
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
     * @param type the input type
     * @return the raw class
     */
    @SuppressWarnings("unchecked")
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
     * @param type the input type
     * @return the direct super type
     */
    public static Type getSuperType(final Type type) {
        return SUPER_TYPE_MAP.computeIfAbsent(type, _type -> {
            if (type instanceof ParameterizedType) {
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
            } else {
                return ((Class<?>) type).getGenericSuperclass();
            }
        });
    }

    /**
     * Returns whether the {@code type}, or its generic raw type, implements the interface.
     *
     * @param type Type to check if it implements an interface.
     * @param interfaceClass The interface.
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
     * @param subType the subtype to find super type for
     * @param rawSuperType the raw class for the super type
     * @return the super type that matches the requirement
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
     * @param subType the supposed subtype
     * @param superType the supposed super type
     * @return true if the first type is the same or a subtype for the second type
     */
    public static boolean isTypeOrSubTypeOf(Type subType, Type superType) {
        return getRawClass(superType).isAssignableFrom(getRawClass(subType));
    }

    /**
     * Create a parameterized type from a raw class and its type arguments.
     *
     * @param rawClass the raw class to construct the parameterized type
     * @param genericTypes the generic arguments
     * @return the parameterized type
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
     * Returns whether the rest response expects to have a body (by checking if the body parameter type is set to Void,
     * in which case a body isn't expected).
     *
     * @param restResponseReturnType The RestResponse subtype containing the type arguments we are inspecting.
     * @return True if a body is expected, false if a Void body is expected.
     */
    public static boolean restResponseTypeExpectsBody(ParameterizedType restResponseReturnType) {
        return getRestResponseBodyType(restResponseReturnType) != Void.class;
    }

    /**
     * Returns the body type expected in the rest response.
     *
     * @param restResponseReturnType The RestResponse subtype containing the type arguments we are inspecting.
     * @return The type of the body.
     */
    public static Type getRestResponseBodyType(Type restResponseReturnType) {
        // if this type has type arguments, then we look at the last one to determine if it expects a body
        final Type[] restResponseTypeArguments = TypeUtil.getTypeArguments(restResponseReturnType);
        if (restResponseTypeArguments != null && restResponseTypeArguments.length > 0) {
            return restResponseTypeArguments[restResponseTypeArguments.length - 1];
        } else {
            // no generic type on this RestResponse subtype, so we go up to parent
            return getRestResponseBodyType(TypeUtil.getSuperType(restResponseReturnType));
        }
    }

    /**
     * Checks if the {@code returnType} is a decode-able type.
     * <p>
     * Types that aren't decode-able are the following (including sub-types):
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * <li>Void</li>
     * <li>void</li>
     * </ul>
     *
     * Reactive, {@link Mono} and {@link Flux}, and Response, {@link Response} and {@link ResponseBase}, generics are
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @param returnType The return type of the method.
     * @return Flag indicating if the return type is decode-able.
     */
    public static boolean isReturnTypeDecodable(Type returnType) {
        if (returnType == null) {
            return false;
        }

        return RETURN_TYPE_DECODEABLE_MAP.computeIfAbsent(returnType, type -> {
            type = unwrapReturnType(type);

            return !TypeUtil.isTypeOrSubTypeOf(type, BinaryData.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, byte[].class)
                && !TypeUtil.isTypeOrSubTypeOf(type, ByteBuffer.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, InputStream.class)
                && !TypeUtil.isTypeOrSubTypeOf(type, Void.TYPE)
                && !TypeUtil.isTypeOrSubTypeOf(type, Void.class);
        });
    }

    /**
     * Checks if the network response body should be eagerly read based on its {@code returnType}.
     * <p>
     * The following types, including sub-types, aren't eagerly read from the network:
     * <ul>
     * <li>BinaryData</li>
     * <li>byte[]</li>
     * <li>ByteBuffer</li>
     * <li>InputStream</li>
     * </ul>
     *
     * Reactive, {@link Mono} and {@link Flux}, and Response, {@link Response} and {@link ResponseBase}, generics are
     * cracked open and their generic types are inspected for being one of the types above.
     *
     * @param returnType The return type of the method.
     * @return Flag indicating if the network response body should be eagerly read.
     */
    public static boolean shouldEagerlyReadResponse(Type returnType) {
        if (returnType == null) {
            return false;
        }

        return isReturnTypeDecodable(returnType)
            || TypeUtil.isTypeOrSubTypeOf(returnType, Void.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class);
    }

    /**
     * Get the type that {@link HttpHeaders} will be deserialized to when returned.
     * <p>
     * {@code returnType} isn't required to have a headers type, in that case the {@link HttpHeaders} won't be
     * deserialized.
     * <p>
     * If the return type is a {@link Mono} its generic type will be inspected. Only return types that are or are a
     * subtype of {@link ResponseBase} will have a headers type.
     *
     * @return The {@code returnType} headers type if set, otherwise null.
     */
    public static Type getHeadersType(Type returnType) {
        Type headersType = null;

        // Crack open Mono<T> to the T type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = TypeUtil.getTypeArgument(returnType);
        }

        // Only ResponseBase will have a headers type, and it will be the first generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, ResponseBase.class)) {
            headersType = TypeUtil.getTypeArguments(TypeUtil.getSuperType(returnType, ResponseBase.class))[0];
        }

        return headersType;
    }

    private static Type unwrapReturnType(Type returnType) {
        // First check if the return type is assignable, is a subtype, to ResponseBase.
        // If it is, begin walking up the super type hierarchy until ResponseBase is the raw type.
        // Then unwrap the second generic type (body type).
        if (TypeUtil.isTypeOrSubTypeOf(returnType, ResponseBase.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == ResponseBase.class);

            return unwrapReturnType(TypeUtil.getTypeArguments(returnType)[1]);
        }

        // Then, like ResponseBase, check if the return type is assignable to Response.
        // If it is, begin walking up the super type hierarchy until the raw type implements Response.
        // Then unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Response.class)) {
            // Handling for Response is slightly different as it is an interface unlike ResponseBase which is a class.
            // The super class hierarchy needs be walked until the super class itself implements Response.
            returnType = walkSuperTypesUntil(returnType, type -> typeImplementsInterface(type, Response.class));

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Then check if the return type is a Mono or Flux and unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Mono.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Flux.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Finally, there is no more unwrapping to perform and return the type as-is.
        return returnType;
    }

    /*
     * Helper method that walks up the super types until the type is an instance of the Class.
     */
    private static Type walkSuperTypesUntil(Type type, Predicate<Type> untilChecker) {
        while (!untilChecker.test(type)) {
            type = TypeUtil.getSuperType(type);
        }

        return type;
    }

    /**
     * Gets the return entity type from the {@code returnType}.
     * <p>
     * The entity type is the {@code returnType} itself if it isn't {@code Mono<T>} or {@code Response<T>}. Otherwise,
     * if the return type is {@code Mono<T>} the {@code T} type will be extracted, then if {@code T} is {@code
     * Response<S>} the {@code S} type will be extracted and returned or if it isn't a {@code Response<S>} {@code T}
     * will be returned.
     *
     * @return The return type entity type.
     */
    public static Type getReturnEntityType(Type returnType) {
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = TypeUtil.getTypeArgument(returnType);
        }

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Response.class)) {
            returnType = TypeUtil.getRestResponseBodyType(returnType);
        }

        return returnType;
    }

    // Private Ctr
    private TypeUtil() {
    }
}
