// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.utils.binarydata.BinaryData;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Utility class to generate response body mode assignment and response handling based on the response body mode.
 */
public final class ResponseBodyModeGeneration {

    /**
     * Generates response body mode assignment based on request options and return type.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param useRequestOptions whether request options are used.
     */
    public static void generateResponseBodyMode(BlockStmt body, String returnTypeName, boolean useRequestOptions) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.addStatement(StaticJavaParser.parseStatement("ResponseBodyMode responseBodyMode = null;"));

        // Assign responseBodyMode based on request options.
        // TODO: Temporary fix for TestInterface
        if (useRequestOptions) {
            IfStmt ifStmt = new IfStmt()
                .setCondition(StaticJavaParser
                    .parseExpression("requestOptions != null && requestOptions.getResponseBodyMode() != null"))
                .setThenStmt(
                    StaticJavaParser.parseBlock("{ responseBodyMode = requestOptions.getResponseBodyMode(); }"));
            body.addStatement(ifStmt);
        }

        // Fallback to assignment based on return type if responseBodyMode is still null.
        String enumName;
        if (returnTypeName.contains("InputStream")) {
            enumName = "STREAM";
        } else if (returnTypeName.contains("byte[]")) {
            enumName = "BYTES";
        } else if (returnTypeName.contains("BinaryData")) {
            enumName = "IGNORE";
        } else {
            enumName = "DESERIALIZE";
        }
        body.addStatement(StaticJavaParser.parseStatement(
            "if (responseBodyMode == null)" + "{ responseBodyMode = ResponseBodyMode." + enumName + "; }"));
    }

    /**
     * Handles deserialization response mode logic.
     *
     * @param body the method builder to append generated code.
     */
    public static void handleDeserializeResponse(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(HttpResponse.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseAccessHelper.class);

        body.addStatement(StaticJavaParser.parseStatement("if (responseBodyMode == ResponseBodyMode.DESERIALIZE)"
            + "{ BinaryData responseBody = response.getBody();"
            + "HttpResponseAccessHelper.setValue((HttpResponse<?>) response, responseBody); } else {"
            + "BinaryData responseBody = response.getBody();"
            + "HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> responseBody); }"));
    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param useRequestOptions whether request options are used.
     */
    public static void generateResponseHandling(BlockStmt body, String returnTypeName, boolean useRequestOptions) {
        if (returnTypeName.equals("void")) {
            closeResponse(body);
            body.addStatement(new ReturnStmt());
        } else if (returnTypeName.equals("java.lang.Void")) {
            closeResponse(body);
            // TODO: Fix whole namespace return type
            body.addStatement(new ReturnStmt("null"));
        } else if (returnTypeName.contains("Response")) {
            if (returnTypeName.contains("Void")) {
                closeResponse(body);
                createResponseIfNecessary(returnTypeName, body);
            } else {
                generateResponseBodyMode(body, returnTypeName, useRequestOptions);
                handleDeserializeResponse(body);
                createResponseIfNecessary(returnTypeName, body);
            }
        } else {
            handleResponseModeToCreateResponse(returnTypeName, body);
        }
    }

    private static void closeResponse(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(IOException.class);
        body.tryAddImportToParentCompilationUnit(UncheckedIOException.class);

        body.addStatement(StaticJavaParser.parseStatement("try { response.close(); }"
            + "catch (IOException e) { throw LOGGER.logThrowableAsError(new UncheckedIOException(e)); }"));
    }

    /**
     * Adds a return statement for response handling when necessary.
     *
     * @param returnTypeName the return type of the method.
     * @param body the method builder to append generated code.
     */
    public static void createResponseIfNecessary(String returnTypeName, BlockStmt body) {
        body.addStatement(new ReturnStmt("(" + returnTypeName + ") response"));
    }

    /**
     * Handles different response processing modes based on the return type and method.
     *
     * @param returnTypeName the return type of the method.
     * @param body the method builder to append generated code.
     */
    public static void handleResponseModeToCreateResponse(String returnTypeName, BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(BinaryData.class);
        body.tryAddImportToParentCompilationUnit(InputStream.class);
        if (returnTypeName.contains("Boolean") || returnTypeName.contains("boolean")) {
            body.addStatement(new ReturnStmt("(response.getStatusCode() / 100) == 2"));
        } else if (returnTypeName.contains("byte[]")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser
                .parseStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;"));
            body.addStatement(StaticJavaParser.parseStatement(
                "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;"));
        } else if (returnTypeName.contains("InputStream")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser.parseStatement("return responseBody.toStream();"));
        } else if (returnTypeName.contains("BinaryData")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
        } else {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser
                .parseStatement("return decodeByteArray(responseBody.toBytes(), response, serializer, methodParser);"));
        }
    }

    private ResponseBodyModeGeneration() {
    }
}
