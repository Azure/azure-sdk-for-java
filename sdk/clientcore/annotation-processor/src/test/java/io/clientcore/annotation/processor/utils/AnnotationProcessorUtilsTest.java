// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationProcessorUtilsTest {
    @Test
    public void createsNestedParameterizedType() {
        DeclaredType modelType = declaredType("com.example.Model");
        DeclaredType mapType = declaredType("java.util.Map", declaredType("java.lang.String"), modelType);
        DeclaredType responseType = declaredType("io.clientcore.core.http.models.Response", mapType);

        assertEquals(
            "CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, "
                + "CoreUtils.createParameterizedType(java.util.Map.class, java.lang.String.class, "
                + "com.example.Model.class))",
            AnnotationProcessorUtils.createParameterizedTypeStatement(responseType, new BlockStmt()));
    }

    @Test
    public void determinesNestedResponseEntityType() {
        DeclaredType mapType
            = declaredType("java.util.Map", declaredType("java.lang.String"), declaredType("com.example.Model"));
        DeclaredType responseType = declaredType("io.clientcore.core.http.models.Response", mapType);

        assertEquals("java.util.Map<java.lang.String, com.example.Model>",
            ResponseHandler.determineTypeCast(responseType));
    }

    private static DeclaredType declaredType(String name, DeclaredType... typeArguments) {
        return new MockDeclaredType(TypeKind.DECLARED, name, typeArguments);
    }
}
