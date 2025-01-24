// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.utils;

import com.squareup.javapoet.MethodSpec;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.util.binarydata.BinaryData;

/*
 * Utility class to generate response body mode assignment and response handling based on the response body mode.
 */
public class ResponseBodyModeGeneration {
    public static void generateResponseBodyModeAssignment(MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("$T responseBodyMode = null", ResponseBodyMode.class)
            .beginControlFlow("if (requestOptions != null)")
            .addStatement("responseBodyMode = requestOptions.getResponseBodyMode()")
            .endControlFlow();
    }

    public static void generateDeserializeResponseHandling(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("if (responseBodyMode == $T.DESERIALIZE)", ResponseBodyMode.class)
            .addStatement("$T responseBody = response.getBody()", BinaryData.class)
            .addStatement("$T.setValue(($T<?>) response, responseBody)",
                HttpResponseAccessHelper.class, HttpResponse.class)
            .endControlFlow();
    }

    public static void generateNonDeserializeResponseHandling(MethodSpec.Builder methodBuilder) {
        methodBuilder.nextControlFlow("else")
            .addStatement("$T responseBody = response.getBody()", BinaryData.class)
            .addStatement("$T.setBodyDeserializer(($T<?>) response, (body) -> responseBody)",
                HttpResponseAccessHelper.class, HttpResponse.class)
            .endControlFlow();
    }
}

