// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import io.clientcore.core.implementation.TypeUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for converting JavaParser AST types to Java reflection types.
 */
public final class TypeConverter {

    // Map to store primitive type mappings
    private static final Map<String, Class<?>> PRIMITIVE_TYPES = new HashMap<>();

    // Static block to initialize the primitive types map
    static {
        PRIMITIVE_TYPES.put("boolean", boolean.class);
        PRIMITIVE_TYPES.put("byte", byte.class);
        PRIMITIVE_TYPES.put("char", char.class);
        PRIMITIVE_TYPES.put("double", double.class);
        PRIMITIVE_TYPES.put("float", float.class);
        PRIMITIVE_TYPES.put("int", int.class);
        PRIMITIVE_TYPES.put("short", short.class);
        PRIMITIVE_TYPES.put("void", void.class);
        PRIMITIVE_TYPES.put("Void", void.class);
    }

    /**
     * Converts JavaParser's AST Type to java.lang.reflect.Type.
     * Supports primitives, generic types, and void types.
     *
     * @param astType the AST type to convert
     * @return the corresponding Java reflection type
     * @throws IllegalArgumentException if the AST type is unsupported
     */
    public static Type toReflectType(com.github.javaparser.ast.type.Type astType) {
        if (astType instanceof ClassOrInterfaceType) {
            return handleClassOrInterfaceType((ClassOrInterfaceType) astType);
        } else if (astType instanceof PrimitiveType) {
            return getPrimitiveClass((PrimitiveType) astType);
        } else if (astType instanceof VoidType) {
            return void.class;
        }
        throw new IllegalArgumentException("Unsupported type: " + astType);
    }

    /**
     * Gets the primitive class for a given PrimitiveType.
     *
     * @param type the PrimitiveType to convert
     * @return the corresponding primitive class
     */
    static Class<?> getPrimitiveClass(PrimitiveType type) {
        return PRIMITIVE_TYPES.getOrDefault(type.asString(), Object.class);
    }

    /**
     * Gets the entity type for a given AST type.
     *
     * @param returnType the AST type to convert
     * @return the corresponding Java reflection type
     */
    static Type getEntityType(com.github.javaparser.ast.type.Type returnType) {
        if (returnType.isArrayType()) {
            // Extract base type (e.g., for `byte[]`, it should return `byte.class`)
            ArrayType arrayType = returnType.asArrayType();
            Type baseType = getEntityType(arrayType.getComponentType());

            if (baseType instanceof Class<?>) {
                return Array.newInstance((Class<?>) baseType, 0).getClass();
            }
        } else if (returnType.isPrimitiveType()) {
            // Convert JavaParser's PrimitiveType to Java Reflection Type
            return getPrimitiveClass(returnType.asPrimitiveType());
        } else if (returnType.isClassOrInterfaceType()) {
            try {
                return Class.forName(returnType.asClassOrInterfaceType().getNameAsString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unknown type: " + returnType, e);
            }
        }
        return Object.class; // Fallback for unknown types
    }

    /**
     * Handles conversion of ClassOrInterfaceType to Java reflection Type.
     *
     * @param classType the ClassOrInterfaceType to convert
     * @return the corresponding Java reflection type
     */
    private static Type handleClassOrInterfaceType(ClassOrInterfaceType classType) {
        if (classType.getTypeArguments().isPresent()) {
            NodeList<com.github.javaparser.ast.type.Type> typeArgs = classType.getTypeArguments().get();
            if (!typeArgs.isEmpty()) {
                Type[] reflectTypeArgs = typeArgs.stream().map(TypeConverter::toReflectType).toArray(Type[]::new);
                return TypeUtil.createParameterizedType(TypeConverter.toReflectType(classType), reflectTypeArgs);
            }
        }
        return resolveClassByName(classType.getNameAsString());
    }

    /**
     * Resolves a class by its name.
     *
     * @param className the name of the class to resolve
     * @return the corresponding Class object
     * @throws IllegalArgumentException if the class cannot be found
     */
    private static Class<?> resolveClassByName(String className) {
        if (PRIMITIVE_TYPES.containsKey(className)) {
            return PRIMITIVE_TYPES.get(className);
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "Unknown class: " + className + ". Ensure it's fully qualified or in common packages.", e);
        }
    }

    // Private constructor
    private TypeConverter() {
    }
}
