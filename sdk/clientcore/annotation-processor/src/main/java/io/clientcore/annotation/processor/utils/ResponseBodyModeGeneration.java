// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.Type;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponse;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.http.serializer.HttpResponseBodyDecoder;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.models.binarydata.BinaryData;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

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
     * @param returnTypeName
     * @param httpMethod
     * @param unexpectedResponseExceptionDetails
     */
    public static void handleDeserializeResponse(BlockStmt body, String returnTypeName, HttpRequestContext httpMethod,
        List<UnexpectedResponseExceptionDetail> unexpectedResponseExceptionDetails) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(HttpResponse.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseAccessHelper.class);

        Type returnType = StaticJavaParser.parseType(returnTypeName); // Statically parse the return type

        if (httpMethod.getHttpMethod() == HttpMethod.HEAD && returnTypeName.contains("Boolean")) {
            body.addStatement(new ReturnStmt("return response.getStatusCode() / 100 == 2;"));
        } else if (returnType.isArrayType() && returnType.asArrayType().getComponentType().asString().equals("byte")) {
            handleByteArrayResponse(body, returnTypeName, httpMethod);
        } else if (returnTypeName.contains("InputStream")) {
            handleInputStreamResponse(body, returnTypeName, httpMethod);
        } else if (returnTypeName.equals("BinaryData")) {
            handleBinaryDataResponse(body, returnTypeName, httpMethod);
        } else {
            handleDeserialize(body, returnTypeName, unexpectedResponseExceptionDetails);
        }
    }

    private static void handleDeserialize(BlockStmt body, String returnTypeName,
        List<UnexpectedResponseExceptionDetail> unexpectedResponseExceptionDetails) {
        body.tryAddImportToParentCompilationUnit(HttpResponseDecodeData.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseBodyDecoder.class);
        body.addStatement(StaticJavaParser.parseStatement("String returnTypeName = \"" + returnTypeName + "\";"));
        // add statment that Object result = decodeByteArray(response.getBody().toBytes(), response, serializer, returnTypeName);
        body.addStatement(StaticJavaParser.parseStatement(
            "Object result = decodeByteArray(response.getBody().toBytes(), response, serializer, returnTypeName);"));
        body.addStatement(StaticJavaParser.parseStatement(
            "if (responseBodyMode == ResponseBodyMode.DESERIALIZE)" + "{ BinaryData responseBody = response.getBody();"
                + "HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result); } else {"
                + "BinaryData responseBody = response.getBody();"
                + "HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result); }"));
    }

    private static void handleByteArrayResponse(BlockStmt body, String returnTypeName, HttpRequestContext httpMethod) {
    }

    private static void handleInputStreamResponse(BlockStmt body, String returnTypeName,
        HttpRequestContext httpMethod) {

    }

    private static void handleBinaryDataResponse(BlockStmt body, String returnTypeName, HttpRequestContext httpMethod) {

    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param method whether request options are used.
     * @param unexpectedResponseExceptionDetails
     */
    public static void generateResponseHandling(BlockStmt body, String returnTypeName, HttpRequestContext method,
        List<UnexpectedResponseExceptionDetail> unexpectedResponseExceptionDetails) {
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
                generateResponseBodyMode(body, returnTypeName);
                handleDeserializeResponse(body, returnTypeName, method, unexpectedResponseExceptionDetails);
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
        body.addStatement(StaticJavaParser.parseStatement("return response;"));
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
