// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.api.Test;

import javax.lang.model.type.TypeKind;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests ResponseBodyModeGeneration methods.
 */
public class ResponseBodyModeGenerationTest {

    @Test
    void generateResponseBodyModeWithRequestOptions() {
        BlockStmt body = new BlockStmt();
        ResponseBodyModeGeneration.generateResponseBodyMode(body);
        assertTrue(body.toString()
            .contains(
                "ResponseBodyMode responseBodyMode = getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());"));
    }

    @Test
    void generateResponseBodyModeWithoutRequestOptions() {
        BlockStmt body = new BlockStmt();
        ResponseBodyModeGeneration.generateResponseBodyMode(body);
        assertTrue(body.toString()
            .contains(
                "ResponseBodyMode responseBodyMode = getOrDefaultResponseBodyMode(httpRequest.getRequestOptions());"));
    }

    @Test
    void generateResponseHandlingWithVoidReturnType() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.DELETE);
        MockDeclaredType returnType = new MockDeclaredType(TypeKind.VOID, "void");
        context.setMethodReturnType(returnType);
        ResponseBodyModeGeneration.generateResponseHandling(body, returnType, context);
        assertFalse(body.toString().contains("return"));
    }
}
