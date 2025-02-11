// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
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
        ResponseBodyModeGeneration.generateResponseBodyMode(body, "String");
        assertTrue(body.toString().contains("responseBodyMode = requestOptions.getResponseBodyMode()"));
    }

    @Test
    void generateResponseBodyModeWithoutRequestOptions() {
        BlockStmt body = new BlockStmt();
        ResponseBodyModeGeneration.generateResponseBodyMode(body, "String");
        assertTrue(body.toString().contains("responseBodyMode = ResponseBodyMode.DESERIALIZE"));
    }

    @Test
    void generateResponseHandlingWithVoidReturnType() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.DELETE);
        context.setMethodReturnType("void");
        ResponseBodyModeGeneration.generateResponseHandling(body, "void", context);
        assertTrue(body.toString().contains("return"));
    }

    @Test
    void generateResponseHandlingWithResponseReturnType() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.GET);
        context.setMethodReturnType("Response<Foo>");
        ResponseBodyModeGeneration.generateResponseHandling(body, "Response", context);
        assertTrue(body.toString().contains("HttpResponseAccessHelper.setValue"));
    }

    @Test
    void generateResponseHandlingWithNonDeserializeMode() {
        BlockStmt body = new BlockStmt();
        HttpRequestContext context = new HttpRequestContext();
        context.setHttpMethod(HttpMethod.GET);
        context.setMethodReturnType("Response<Foo>");
        ResponseBodyModeGeneration.generateResponseHandling(body, "Response", context);
        assertTrue(body.toString().contains("HttpResponseAccessHelper.setBodyDeserializer"));
    }
}
