// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.mocks;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of {@link DeclaredType}.
 */
public class MockDeclaredType extends MockTypeMirror implements DeclaredType {

    /**
     * Creates a mock {@link DeclaredType}.
     *
     * @param kind the {@link DeclaredType}
     * @param toString the {@link DeclaredType#toString()} value
     */
    public MockDeclaredType(TypeKind kind, String toString) {
        super(kind, toString);
    }

    @Override
    public Element asElement() {
        return null;
    }

    @Override
    public TypeMirror getEnclosingType() {
        return null;
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments() {
        return Collections.emptyList();
    }
}
