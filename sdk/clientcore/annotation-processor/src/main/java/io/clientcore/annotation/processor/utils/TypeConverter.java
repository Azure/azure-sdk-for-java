// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
import io.clientcore.core.http.models.Response;

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
                return new VoidType();

            case BOOLEAN:
                return PrimitiveType.booleanType();

            case BYTE:
                return PrimitiveType.byteType();

            case SHORT:
                return PrimitiveType.shortType();

            case INT:
                return PrimitiveType.intType();

            case LONG:
                return PrimitiveType.longType();

            case CHAR:
                return PrimitiveType.charType();

            case FLOAT:
                return PrimitiveType.floatType();

            case DOUBLE:
                return PrimitiveType.doubleType();

            case ARRAY:
                if (returnType instanceof ArrayType) {
                    ArrayType arrayType = (ArrayType) returnType;
                    return new com.github.javaparser.ast.type.ArrayType(getAstType(arrayType.getComponentType()));
                } else {
                    // Fallback to Object[] when uncertain.
                    return new com.github.javaparser.ast.type.ArrayType(createObjectType());
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
                    return createObjectType();
                }

            default:
                throw new IllegalArgumentException("Unsupported return type: " + returnType);
        }
    }

    // Create this in a method as the constructor used is deprecated.
    // It's fine for this use but isn't always safe otherwise.
    @SuppressWarnings("deprecation")
    private static ClassOrInterfaceType createObjectType() {
        return new ClassOrInterfaceType("Object");
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
        if (declaredType instanceof DeclaredType) {
            DeclaredType classType = (DeclaredType) declaredType;

            // Ensure type arguments exist before accessing them
            List<? extends TypeMirror> typeArguments = classType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                // First generic type (e.g., List<Foo> or Foo<String>)
                return TypeConverter.toReflectType(typeArguments.get(0));
            } else {
                return tryLoadClass(classType.toString());
            }

        }
        return Object.class; // Fallback
    }

    private static Type tryLoadClass(String className) {
        try {
            return Class.forName(className, true, TypeConverter.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // Couldn't load the class, likely from the package we're attempting to process.
            // Fallback to Object in this case.
            return Object.class;
        }
    }

    // Private constructor
    private TypeConverter() {
    }
}
