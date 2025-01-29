// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.utils;

import com.squareup.javapoet.MethodSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseBodyModeGenerationTest {

    private MethodSpec.Builder methodBuilder;

    @BeforeEach
    void setUp() {
        methodBuilder = MethodSpec.methodBuilder("testMethod");
    }

    @Test
    void generateResponseBodyModeAssignment_withRequestOptions() {
        ResponseBodyModeGeneration.generateResponseBodyModeAssignment(methodBuilder);
        MethodSpec methodSpec = methodBuilder.build();
        assertTrue(methodSpec.toString().contains("responseBodyMode = requestOptions.getResponseBodyMode()"));
    }

    @Test
    void generateResponseHandling_withDeserializeMode() {
        ResponseBodyModeGeneration.generateDeserializeResponseHandling(methodBuilder);
        MethodSpec methodSpec = methodBuilder.build();
        // verify generation calls HttpResponseAccessHelper.setValue() with the correct parameters;
        assertTrue(methodSpec.toString().contains("HttpResponseAccessHelper.setValue((io.clientcore.core.http.models.HttpResponse<?>) response, responseBody);"));
    }

    //@Test
    //void generateResponseHandling_withNonDeserializeMode() {
    //    ResponseBodyModeGeneration.generateNonDeserializeResponseHandling(methodBuilder);
    //    MethodSpec methodSpec = methodBuilder.build();
    //    // verify generation calls HttpResponseAccessHelper.setValue() with the correct parameters;
    //    assertTrue(methodSpec.toString().contains("HttpResponseAccessHelper.setValue((io.clientcore.core.http.models.HttpResponse<?>) response, responseBody);"));
    //}
}
