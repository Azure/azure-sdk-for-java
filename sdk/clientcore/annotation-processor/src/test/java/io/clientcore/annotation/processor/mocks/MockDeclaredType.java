// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Mock implementation of {@link DeclaredType}.
 */
public class MockDeclaredType extends MockTypeMirror implements DeclaredType {
    private final String qualifiedName;
    private final List<? extends TypeMirror> typeArguments;

    /**
     * Creates a mock {@link DeclaredType}.
     *
     * @param kind the {@link DeclaredType}
     * @param toString the {@link DeclaredType#toString()} value
     */
    public MockDeclaredType(TypeKind kind, String toString, TypeMirror... typeArguments) {
        super(kind, toString);
        this.qualifiedName = toString;
        this.typeArguments = Arrays.asList(typeArguments);
    }

    @Override
    public Element asElement() {
        return new TypeElement() {
            @Override
            public List<? extends Element> getEnclosedElements() {
                return Collections.emptyList();
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
                return (A[]) java.lang.reflect.Array.newInstance(annotationType, 0);
            }

            @Override
            public <R, P> R accept(ElementVisitor<R, P> v, P p) {
                return null;
            }

            @Override
            public NestingKind getNestingKind() {
                return NestingKind.TOP_LEVEL;
            }

            @Override
            public Name getQualifiedName() {
                return new Name() {
                    @Override
                    public int length() {
                        return qualifiedName.length();
                    }

                    @Override
                    public char charAt(int index) {
                        return qualifiedName.charAt(index);
                    }

                    @Override
                    public CharSequence subSequence(int start, int end) {
                        return qualifiedName.subSequence(start, end);
                    }

                    @Override
                    public boolean contentEquals(CharSequence cs) {
                        return qualifiedName.contentEquals(cs);
                    }

                    @Override
                    public String toString() {
                        return qualifiedName;
                    }
                };
            }

            @Override
            public TypeMirror asType() {
                return MockDeclaredType.this;
            }

            @Override
            public ElementKind getKind() {
                return ElementKind.CLASS;
            }

            @Override
            public Set<Modifier> getModifiers() {
                return Collections.singleton(Modifier.PUBLIC);
            }

            @Override
            public Name getSimpleName() {
                return new Name() {
                    @Override
                    public boolean contentEquals(CharSequence cs) {
                        return qualifiedName.contentEquals(cs);
                    }

                    @Override
                    public int length() {
                        return qualifiedName.length();
                    }

                    @Override
                    public char charAt(int index) {
                        return qualifiedName.charAt(index);
                    }

                    @Override
                    public CharSequence subSequence(int start, int end) {
                        return qualifiedName.subSequence(start, end);
                    }

                    @Override
                    public String toString() {
                        return qualifiedName;
                    }
                };
            }

            @Override
            public TypeMirror getSuperclass() {
                return null;
            }

            @Override
            public List<? extends TypeMirror> getInterfaces() {
                return Collections.emptyList();
            }

            @Override
            public List<? extends TypeParameterElement> getTypeParameters() {
                return Collections.emptyList();
            }

            @Override
            public Element getEnclosingElement() {
                return null;
            }
        };
    }

    @Override
    public TypeMirror getEnclosingType() {
        return null;
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        return typeArguments;
    }

}
