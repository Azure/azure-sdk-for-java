// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeConverterTest {

    @Test
    public void toReflectTypePrimitiveBoolean() {
        TypeMirror type = mockPrimitiveType(TypeKind.BOOLEAN);
        Type result = TypeConverter.toReflectType(type);
        assertEquals(boolean.class, result);
    }

    @Test
    public void toReflectTypePrimitiveInt() {
        TypeMirror type = mockPrimitiveType(TypeKind.INT);
        Type result = TypeConverter.toReflectType(type);
        assertEquals(int.class, result);
    }

    @Test
    public void toReflectTypeVoid() {
        TypeMirror type = mockPrimitiveType(TypeKind.VOID);
        Type result = TypeConverter.toReflectType(type);
        assertEquals(void.class, result);
    }

    @Test
    public void toReflectTypeArray() {
        TypeMirror componentType = mockPrimitiveType(TypeKind.INT);
        ArrayType arrayType = mockArrayType(componentType);

        Type result = TypeConverter.toReflectType(arrayType);
        assertEquals("int[]", result.getTypeName());
    }

    @Test
    public void toReflectTypeDeclaredType() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "java.util.List");
        Type result = TypeConverter.toReflectType(declaredType);
        assertNotNull(result);
    }

    @Test
    public void getPrimitiveClassPrimitiveLong() {
        TypeMirror type = mockPrimitiveType(TypeKind.LONG);
        Class<?> result = TypeConverter.getPrimitiveClass(type);
        assertEquals(long.class, result);
    }

    @Test
    public void getEntityTypePrimitiveFloat() {
        TypeMirror type = mockPrimitiveType(TypeKind.FLOAT);
        Type result = TypeConverter.getEntityType(type);
        assertEquals(float.class, result);
    }

    @Test
    public void isResponseTypeResponseType() {
        DeclaredType responseType = new MockDeclaredType(TypeKind.DECLARED, "io.clientcore.core.http.models.Response");
        boolean result = TypeConverter.isResponseType(responseType);
        assertTrue(result);
    }

    @Test
    public void isResponseTypeNonResponseType() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.ARRAY, "java.util.List");
        boolean result = TypeConverter.isResponseType(declaredType);
        assertFalse(result);
    }

    @Test
    public void getAstTypePrimitiveDouble() {
        TypeMirror type = mockPrimitiveType(TypeKind.DOUBLE);
        com.github.javaparser.ast.type.Type result = TypeConverter.getAstType(type);
        assertEquals("double", result.toString());
    }

    @Test
    public void getAstTypeArray() {
        TypeMirror componentType = new MockDeclaredType(TypeKind.DECLARED, "java.lang.String");
        ArrayType arrayType = mockArrayType(componentType);
        com.github.javaparser.ast.type.Type result = TypeConverter.getAstType(arrayType);
        assertEquals("java.lang.String[]", result.toString());
    }

    @Test
    public void getAstTypeDeclaredType() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "java.util.List",
            new MockDeclaredType(TypeKind.DECLARED, "java.lang.String"));
        com.github.javaparser.ast.type.Type result = TypeConverter.getAstType(declaredType);
        assertTrue(result.toString().contains("java.util.List"));
    }

    @Test
    public void handleArrayType() {
        ArrayType arrayType = mockArrayType(mockPrimitiveType(TypeKind.INT));
        Type result = TypeConverter.handleArrayType(arrayType);
        assertInstanceOf(Class.class, result);
        assertTrue(((Class<?>) result).isArray());
    }

    @Test
    public void handleDeclaredType() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "java.util.List");
        Type result = TypeConverter.handleDeclaredType(declaredType);
        assertEquals(List.class, result);
    }

    @Test
    public void handleDeclaredTypeThatCannotBeLoaded() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "com.class.that.cannot.be.Loaded");
        Type result = TypeConverter.handleDeclaredType(declaredType);
        assertEquals(Object.class, result);
    }

    @Test
    public void handleDeclaredTypeGeneric() {
        DeclaredType declaredType = new MockDeclaredType(TypeKind.DECLARED, "java.util.Map",
            new MockDeclaredType(TypeKind.DECLARED, "java.lang.String"), new MockDeclaredType(TypeKind.INT, "int"));
        Type result = TypeConverter.handleDeclaredType(declaredType);
        assertNotNull(result);
    }

    @Test
    public void toReflectTypeUnsupportedType() {
        TypeMirror unsupportedType = new MockTypeMirror(TypeKind.NONE, null);
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> TypeConverter.toReflectType(unsupportedType));
        assertEquals("Unsupported type: " + unsupportedType, exception.getMessage());
    }

    static PrimitiveType mockPrimitiveType(TypeKind kind) {
        return new PrimitiveType() {
            @Override
            public TypeKind getKind() {
                return kind;
            }

            @Override
            public List<? extends AnnotationMirror> getAnnotationMirrors() {
                return Collections.emptyList();
            }

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
                return (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
            }

            @Override
            public <R, P> R accept(TypeVisitor<R, P> v, P p) {
                return null;
            }
        };
    }

    static ArrayType mockArrayType(TypeMirror componentType) {
        return new ArrayType() {
            @Override
            public List<? extends AnnotationMirror> getAnnotationMirrors() {
                return Collections.emptyList();
            }

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return null;
            }

            @SuppressWarnings("unchecked")
            public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
                return (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
            }

            @Override
            public TypeMirror getComponentType() {
                return componentType;
            }

            @Override
            public TypeKind getKind() {
                return TypeKind.ARRAY;
            }

            @Override
            public <R, P> R accept(TypeVisitor<R, P> v, P p) {
                return null;
            }
        };
    }
}
