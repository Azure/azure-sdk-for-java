// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.mocks;

import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Mock implementation of {@link Elements}.
 */
public class MockElements implements Elements {
    @Override
    public TypeElement getTypeElement(CharSequence name) {
        return new TypeElement() {
            @Override
            public List<? extends Element> getEnclosedElements() {
                return Collections.emptyList();  // Avoid returning null
            }

            @Override
            public NestingKind getNestingKind() {
                return NestingKind.TOP_LEVEL;
            }

            @Override
            public Name getQualifiedName() {
                return new Name() {
                    @Override
                    public boolean contentEquals(CharSequence cs) {
                        return name.toString().contentEquals(cs);
                    }

                    @Override
                    public int length() {
                        return name.length();
                    }

                    @Override
                    public char charAt(int index) {
                        return name.charAt(index);
                    }

                    @Override
                    public CharSequence subSequence(int start, int end) {
                        return name.subSequence(start, end);
                    }

                    @Override
                    public String toString() {
                        return name.toString();
                    }
                };
            }

            @Override
            public Name getSimpleName() {
                return getQualifiedName();  // Simplification
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
            public TypeMirror asType() {
                return new MockTypeMirror(TypeKind.DECLARED, name.toString());
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
            public Element getEnclosingElement() {
                return null;
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
            public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
                return null;
            }

            @Override
            public <R, P> R accept(ElementVisitor<R, P> v, P p) {
                return null;
            }
        };
    }

    @Override
    public PackageElement getPackageElement(CharSequence name) {
        return null;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue>
        getElementValuesWithDefaults(AnnotationMirror a) {
        return null;
    }

    @Override
    public String getDocComment(Element e) {
        return null;
    }

    @Override
    public boolean isDeprecated(Element e) {
        return false;
    }

    @Override
    public Name getBinaryName(TypeElement type) {
        return null;
    }

    @Override
    public PackageElement getPackageOf(Element type) {
        return null;
    }

    @Override
    public List<? extends Element> getAllMembers(TypeElement type) {
        return null;
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
        return null;
    }

    @Override
    public boolean hides(Element hider, Element hidden) {
        return false;
    }

    @Override
    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
        return false;
    }

    @Override
    public String getConstantExpression(Object value) {
        return null;
    }

    @Override
    public void printElements(Writer w, Element... elements) {

    }

    @Override
    public Name getName(CharSequence cs) {
        return null;
    }

    @Override
    public boolean isFunctionalInterface(TypeElement type) {
        return false;
    }
}
