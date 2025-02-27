// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.api.Test;

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
        ClassOrInterfaceType returnType = StaticJavaParser.parseClassOrInterfaceType("Void");
        context.setMethodReturnType(returnType);
        ResponseBodyModeGeneration.generateResponseHandling(body, returnType, context);
        assertTrue(body.toString().contains("return"));
    }

    @Test
    void generateResponseHandlingWithResponseReturnType() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.GET);
        ClassOrInterfaceType returnType = StaticJavaParser.parseClassOrInterfaceType("Response<Foo>");
        context.setMethodReturnType(returnType);
        ResponseBodyModeGeneration.generateResponseHandling(body, returnType, context);

        assertTrue(body.toString().contains("HttpResponseAccessHelper.setValue"));
    }

    @Test
    void generateResponseHandlingWithNonDeserializeMode() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.GET);
        ClassOrInterfaceType returnType = StaticJavaParser.parseClassOrInterfaceType("Response<Foo>");
        context.setMethodReturnType(returnType);
        ResponseBodyModeGeneration.generateResponseHandling(body, returnType, context);
        assertTrue(body.toString().contains("HttpResponseAccessHelper.setBodyDeserializer"));
    }
}
