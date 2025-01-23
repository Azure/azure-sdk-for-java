// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.util.binarydata.BinaryData;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Utility class to generate response body mode assignment and response handling based on the response body mode.
 */
public class ResponseBodyModeGeneration {

    /**
     * Generates response body mode assignment based on request options and return type.
     *
     * @param methodBuilder the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param useRequestOptions whether request options are used.
     */
    public static void generateResponseBodyMode(MethodSpec.Builder methodBuilder, TypeName returnTypeName,
                                                boolean useRequestOptions) {
        methodBuilder.addStatement("$T responseBodyMode = null", ResponseBodyMode.class);

        // Assign responseBodyMode based on request options.
        // TODO: Temporary fix for TestInterface
        if (useRequestOptions) {
            methodBuilder.beginControlFlow(
                    "if (requestOptions != null && requestOptions.getResponseBodyMode() != null)")
                .addStatement("responseBodyMode = requestOptions.getResponseBodyMode()")
                .endControlFlow();
        }

        // Fallback to assignment based on return type if responseBodyMode is still null.
        methodBuilder.beginControlFlow("if (responseBodyMode == null)");
        if (returnTypeName.toString().contains("InputStream")) {
            methodBuilder.addStatement("responseBodyMode = $T.STREAM", ResponseBodyMode.class);
        } else if (returnTypeName.toString().contains("byte[]")) {
            methodBuilder.addStatement("responseBodyMode = $T.BYTES", ResponseBodyMode.class);
        } else if (returnTypeName.toString().contains("BinaryData")) {
            methodBuilder.addStatement("responseBodyMode = $T.IGNORE", ResponseBodyMode.class);
        } else {
            methodBuilder.addStatement("responseBodyMode = $T.DESERIALIZE", ResponseBodyMode.class);
        }
        methodBuilder.endControlFlow();
    }

    /**
     * Handles deserialization response mode logic.
     *
     * @param methodBuilder the method builder to append generated code.
     */
    public static void handleDeserializeResponse(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("if (responseBodyMode == $T.DESERIALIZE)", ResponseBodyMode.class)
            .addStatement("$T responseBody = response.getBody()", BinaryData.class)
            .addStatement("$T.setValue(($T<?>) response, responseBody)", HttpResponseAccessHelper.class,
                HttpResponse.class);
    }

    /**
     * Handles non-deserialization response mode logic.
     *
     * @param methodBuilder the method builder to append generated code.
     */
    public static void handleNonDeserializeResponse(MethodSpec.Builder methodBuilder) {
        methodBuilder.nextControlFlow("else")
            .addStatement("$T responseBody = response.getBody()", BinaryData.class)
            .addStatement("$T.setBodyDeserializer(($T<?>) response, (body) -> responseBody)",
                HttpResponseAccessHelper.class, HttpResponse.class)
            .endControlFlow();
    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param methodBuilder the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param useRequestOptions whether request options are used.
     */
    public static void generateResponseHandling(MethodSpec.Builder methodBuilder, TypeName returnTypeName,
                                                boolean useRequestOptions) {
        if (returnTypeName.toString().equals("void")) {
            closeResponse(methodBuilder);
            methodBuilder.addStatement("return");
        } else if (returnTypeName.toString().equals("java.lang.Void")) {
            closeResponse(methodBuilder);
            // TODO: Fix whole namespace return type
            methodBuilder.addStatement("return null");
        } else if (returnTypeName.toString().contains("Response")) {
            if (returnTypeName.toString().contains("Void")) {
                closeResponse(methodBuilder);
                createResponseIfNecessary(returnTypeName, methodBuilder);
            } else {
                generateResponseBodyMode(methodBuilder, returnTypeName, useRequestOptions);
                handleDeserializeResponse(methodBuilder);
                handleNonDeserializeResponse(methodBuilder);
                createResponseIfNecessary(returnTypeName, methodBuilder);
            }
        } else {
            handleResponseModeToCreateResponse(returnTypeName, methodBuilder);
        }
    }

    private static void closeResponse(MethodSpec.Builder methodBuilder) {
        methodBuilder.beginControlFlow("try")
            .addStatement("response.close()")
            .nextControlFlow("catch ($T e)", IOException.class)
            .addStatement("throw LOGGER.logThrowableAsError(new $T(e))", UncheckedIOException.class)
            .endControlFlow();
    }

    /**
     * Adds a return statement for response handling when necessary.
     *
     * @param returnTypeName the return type of the method.
     * @param methodBuilder  the method builder to append generated code.
     */
    public static void createResponseIfNecessary(TypeName returnTypeName, MethodSpec.Builder methodBuilder) {
        methodBuilder.addStatement("return ($T) response", returnTypeName);
    }

    /**
     * Handles different response processing modes based on the return type and method.
     *
     * @param returnTypeName the return type of the method.
     * @param methodBuilder  the method builder to append generated code.
     */
    public static void handleResponseModeToCreateResponse(TypeName returnTypeName, MethodSpec.Builder methodBuilder) {
        if (returnTypeName.toString().contains("Boolean") || returnTypeName.toString().contains("boolean")) {
            methodBuilder.addStatement("return (response.getStatusCode() / 100) == 2");
        } else if (returnTypeName.toString().contains("byte[]")) {
            methodBuilder.addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null")
                .addStatement(
                    "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null");
        } else if (returnTypeName.toString().contains("InputStream")) {
            methodBuilder.addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("return responseBody.toStream()");
        } else if (returnTypeName.toString().contains("BinaryData")) {
            methodBuilder.addStatement("$T responseBody = response.getBody()", BinaryData.class);
        } else {
            methodBuilder.addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("return decodeByteArray(responseBody.toBytes(), response, serializer, methodParser)");
        }
    }
}

