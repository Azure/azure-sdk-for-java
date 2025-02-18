// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponse;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.models.binarydata.BinaryData;

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
     */
    public static void generateResponseBodyMode(BlockStmt body, String returnTypeName) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(RequestOptions.class);
        body.addStatement(
            StaticJavaParser.parseStatement("RequestOptions requestOptions = httpRequest.getRequestOptions();"));

        body.addStatement(StaticJavaParser.parseStatement("ResponseBodyMode responseBodyMode = null;"));

        IfStmt ifStmt = new IfStmt()
            .setCondition(StaticJavaParser
                .parseExpression("requestOptions != null && requestOptions.getResponseBodyMode() != null"))
            .setThenStmt(StaticJavaParser.parseBlock("{ responseBodyMode = requestOptions.getResponseBodyMode(); }"))
            .setElseStmt(StaticJavaParser
                .parseBlock("{ responseBodyMode = ResponseBodyMode." + (returnTypeName.contains("InputStream")
                    ? "STREAM"
                    : returnTypeName.contains("byte[]")
                        ? "BYTES"
                        : returnTypeName.contains("BinaryData") ? "IGNORE" : "DESERIALIZE")
                    + "; }"));
        body.addStatement(ifStmt);
    }

    /**
     * Handles deserialization response mode logic.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     */
    public static void handleResponseBody(BlockStmt body, String returnTypeName, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(HttpResponse.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseAccessHelper.class);
        body.addStatement(StaticJavaParser.parseStatement("String returnTypeName = \"" + returnTypeName + "\";"));

        if (method.getHttpMethod() == HttpMethod.HEAD && returnTypeName.contains("Boolean")
            || returnTypeName.contains("boolean")) {
            body.addStatement("Object result = (responseStatusCode / 100) == 2");
        } else if (returnTypeName.contains("byte[]")) {
            body.addStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;");

            // if (returnValueWireType == Base64Uri.class) {
            // responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            // }

            body.addStatement(
                "Object result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;");
        } else if (returnTypeName.contains("InputStream")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement("Object result = responseBody.toStream();");
        } else if (returnTypeName.contains("BinaryData")) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            body.addStatement("Object result = response.getBody();");
        } else {
            body.addStatement(
                "Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnTypeName);");
        }
        body.addStatement(StaticJavaParser.parseStatement("if (responseBodyMode == ResponseBodyMode.DESERIALIZE)"
            + "{ HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result); } else {"
            + "HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result); }"));
    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param method whether request options are used.
     */
    public static void generateResponseHandling(BlockStmt body, String returnTypeName, HttpRequestContext method) {
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
                createResponseIfNecessary(body);
            } else {
                generateResponseBodyMode(body, returnTypeName);
                handleResponseBody(body, returnTypeName, method);
                createResponseIfNecessary(body);
            }
        } else {
            handleRestResponseReturnType(body, returnTypeName, method);
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
     * @param body the method builder to append generated code.
     */
    public static void createResponseIfNecessary(BlockStmt body) {
        body.addStatement(StaticJavaParser.parseStatement("return response;"));
    }

    /**
     * Handles different response processing modes based on the return type and method.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param method the HttpMethod context
     */
    public static void handleRestResponseReturnType(BlockStmt body, String returnTypeName, HttpRequestContext method) {
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
            handleResponseBody(body, returnTypeName, method);
        }
    }

    private ResponseBodyModeGeneration() {
    }
}
