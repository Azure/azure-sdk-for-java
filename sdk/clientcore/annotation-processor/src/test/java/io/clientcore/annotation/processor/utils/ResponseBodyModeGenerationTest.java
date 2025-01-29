// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
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
    void generateResponseBodyModeWithRequestOptions() {
        TypeName returnTypeName = TypeName.get(String.class);
        ResponseBodyModeGeneration.generateResponseBodyMode(methodBuilder, returnTypeName, true);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("responseBodyMode = requestOptions.getResponseBodyMode()"));
    }

    @Test
    void generateResponseBodyModeWithoutRequestOptions() {
        TypeName returnTypeName = TypeName.get(String.class);
        ResponseBodyModeGeneration.generateResponseBodyMode(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString()
            .contains("responseBodyMode = io.clientcore.core.http.models" + ".ResponseBodyMode.DESERIALIZE"));
    }

    @Test
    void generateResponseHandlingWithVoidReturnType() {
        TypeName returnTypeName = TypeName.VOID;
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("return"));
    }

    @Test
    void generateResponseHandlingWithResponseReturnType() {
        TypeName returnTypeName = TypeName.get(HttpResponse.class);
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(
            methodSpec.toString().contains("io.clientcore.core.implementation.http.HttpResponseAccessHelper.setValue"));
    }

    @Test
    void generateResponseHandlingWithNonDeserializeMode() {
        TypeName returnTypeName = TypeName.get(HttpResponse.class);
        ResponseBodyModeGeneration.generateResponseHandling(methodBuilder, returnTypeName, false);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString()
            .contains("io.clientcore.core.implementation.http.HttpResponseAccessHelper.setBodyDeserializer"));
    }
}
