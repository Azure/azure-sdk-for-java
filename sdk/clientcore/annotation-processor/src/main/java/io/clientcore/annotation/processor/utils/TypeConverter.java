// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.TypeUtil;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting JavaParser AST types to Java reflection types.
 */
public final class TypeConverter {

    /**
     * Converts JavaParser's AST Type to java.lang.reflect.Type.
     * Supports primitives, generic types, and void types.
     *
     * @param typeMirror the type to convert
     * @return the corresponding Java reflection type
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static Type toReflectType(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case DECLARED:
                return handleDeclaredType(typeMirror);

            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return getPrimitiveClass(typeMirror);

            case VOID:
                return void.class;

            case ARRAY:
                return handleArrayType(typeMirror);

            default:
                throw new IllegalArgumentException("Unsupported type: " + typeMirror);
        }
    }

    /**
     * Gets the primitive class for a given PrimitiveType.
     *
     * @param type the PrimitiveType to convert
     * @return the corresponding primitive class
     */
    public static Class<?> getPrimitiveClass(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
                return boolean.class;

            case BYTE:
                return byte.class;

            case CHAR:
                return char.class;

            case DOUBLE:
                return double.class;

            case FLOAT:
                return float.class;

            case INT:
                return int.class;

            case LONG:
                return long.class;

            case SHORT:
                return short.class;

            case VOID:
                return void.class;

            default:
                return Object.class;
        }
    }

    /**
     * Gets the entity type for a given AST type.
     *
     * @param returnType the AST type to convert
     * @return the corresponding Java reflection type
     */
    public static Type getEntityType(TypeMirror returnType) {
        switch (returnType.getKind()) {
            case ARRAY:
                return handleArrayType(returnType);

            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                // Convert JavaParser's PrimitiveType to Java Reflection Type
                return getPrimitiveClass(returnType);

            case DECLARED:
                return handleDeclaredType(returnType);

            default:
                return Object.class; // Fallback for unknown types
        }
    }

    /**
     * Checks if the given return type is a Response type.
     *
     * @param returnType the return type to check
     * @return true if the return type is a Response type, false otherwise
     */
    public static boolean isResponseType(TypeMirror returnType) {
        if (returnType instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            return typeElement.getQualifiedName().contentEquals(Response.class.getCanonicalName());
        }
        return false;
    }

    /**
     * Converts a DeclaredType to a ClassOrInterfaceType with type arguments.
     *
     * @param returnType the DeclaredType to convert
     * @return the corresponding ClassOrInterfaceType
     * @throws IllegalArgumentException if the type is unsupported
     */
    public static com.github.javaparser.ast.type.Type getAstType(TypeMirror returnType) {
        switch (returnType.getKind()) {
            case VOID:
                return StaticJavaParser.parseType("void");

            case BOOLEAN:
                return StaticJavaParser.parseType("boolean");

            case BYTE:
                return StaticJavaParser.parseType("byte");

            case SHORT:
                return StaticJavaParser.parseType("short");

            case INT:
                return StaticJavaParser.parseType("int");

            case LONG:
                return StaticJavaParser.parseType("long");

            case CHAR:
                return StaticJavaParser.parseType("char");

            case FLOAT:
                return StaticJavaParser.parseType("float");

            case DOUBLE:
                return StaticJavaParser.parseType("double");

            case ARRAY:
                if (returnType instanceof ArrayType) {
                    ArrayType arrayType = (ArrayType) returnType;
                    return StaticJavaParser.parseType(getAstType(arrayType.getComponentType()).toString() + "[]");
                } else {
                    return StaticJavaParser.parseType("Object[]"); // Fallback
                }
            case DECLARED:
                // converting DeclaredType to ClassOrInterfaceType with type arguments
                if (returnType instanceof DeclaredType) {
                    DeclaredType declaredType = (DeclaredType) returnType;
                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                    ClassOrInterfaceType astType
                        = new ClassOrInterfaceType(null, typeElement.getSimpleName().toString());
                    if (!declaredType.getTypeArguments().isEmpty()) {
                        List<com.github.javaparser.ast.type.Type> typeArguments = declaredType.getTypeArguments()
                            .stream()
                            .map(TypeConverter::getAstType)
                            .collect(Collectors.toList());
                        NodeList<com.github.javaparser.ast.type.Type> nodeListTypeArguments
                            = new NodeList<>(typeArguments);
                        astType.setTypeArguments(nodeListTypeArguments);
                    }
                    return astType;
                } else {
                    return StaticJavaParser.parseType("Object"); // Fallback
                }

            default:
                throw new IllegalArgumentException("Unsupported return type: " + returnType);
        }
    }

    static java.lang.reflect.Type handleArrayType(TypeMirror type) {
        if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            java.lang.reflect.Type componentType = toReflectType(arrayType.getComponentType());
            return java.lang.reflect.Array.newInstance((Class<?>) componentType, 0).getClass();
        }
        return Object.class;
    }

    static java.lang.reflect.Type handleDeclaredType(TypeMirror declaredType) {
        if (declaredType instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) declaredType;

            // Ensure type arguments exist before accessing them
            if (classType.getTypeArguments().isPresent()) {
                NodeList<com.github.javaparser.ast.type.Type> typeArguments = classType.getTypeArguments().get();

                if (!typeArguments.isEmpty()) {
                    com.github.javaparser.ast.type.Type innerType = typeArguments.get(0); // First generic type (e.g., List<Foo> or Foo<String>)
                    return TypeUtil.createParameterizedType(innerType.toString().getClass());
                } else {
                    // No generic type on this RestResponse subtype, so we go up to parent
                    return TypeConverter.toReflectType(declaredType);
                }

            }
        }
        return Object.class; // Fallback
    }

    // Private constructor
    private TypeConverter() {
    }
}
