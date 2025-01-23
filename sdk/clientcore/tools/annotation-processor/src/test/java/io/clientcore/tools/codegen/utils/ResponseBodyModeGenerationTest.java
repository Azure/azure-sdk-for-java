// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.utils;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.clientcore.core.http.models.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests ResponseBodyModeGeneration methods.
 */
public class ResponseBodyModeGenerationTest {

    private MethodSpec.Builder methodBuilder;

    @BeforeEach
    void setUp() {
        methodBuilder = MethodSpec.methodBuilder("testMethod");
    }

    @Test
    void generateResponseBodyMode_withRequestOptions() {
        TypeName returnTypeName = TypeName.get(String.class);
        ResponseBodyModeGeneration.generateResponseBodyMode(methodBuilder, returnTypeName, true);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("responseBodyMode = requestOptions.getResponseBodyMode()"));
    }

    @Test
    void generateResponseBodyMode_withoutRequestOptions() {
        TypeName returnTypeName = TypeName.get(String.class);
        ResponseBodyModeGeneration.generateResponseBodyMode(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("responseBodyMode = io.clientcore.core.http.models" +
            ".ResponseBodyMode.DESERIALIZE"));
    }

    @Test
    void generateResponseHandling_withVoidReturnType() {
        TypeName returnTypeName = TypeName.VOID;
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("return"));
    }

    @Test
    void generateResponseHandling_withResponseReturnType() {
        TypeName returnTypeName = TypeName.get(HttpResponse.class);
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString()
            .contains("io.clientcore.core.implementation.http.HttpResponseAccessHelper.setValue"));
    }

    @Test
    void generateResponseHandling_withNonDeserializeMode() {
        TypeName returnTypeName = TypeName.get(HttpResponse.class);
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains(
            "io.clientcore.core.implementation.http.HttpResponseAccessHelper.setBodyDeserializer"));
    }
}
