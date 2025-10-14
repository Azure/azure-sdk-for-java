// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link TypeMirror}.
 */
public class MockTypeMirror implements TypeMirror, ArrayType {
    private final TypeKind kind;
    private final String toString;

    /**
     * Creates a mock {@link TypeMirror}.
     *
     * @param kind the {@link TypeKind}
     * @param toString the {@link TypeMirror#toString()} value
     */
    public MockTypeMirror(TypeKind kind, String toString) {
        this.kind = kind;
        this.toString = toString;
    }

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

    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        return (A[]) Array.newInstance(annotationType, 0);
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
        return null;
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public TypeMirror getComponentType() {
        if (kind == TypeKind.ARRAY) {
            return new MockTypeMirror(TypeKind.BYTE, "byte");
        }
        return null;
    }
}
