// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.http.HttpResponse;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.IOException;
import java.io.UncheckedIOException;

import static io.clientcore.annotation.processor.templating.JavaParserTemplateProcessor.isPrimitiveOrWrapper;

/**
 * Utility class to generate response body mode assignment and response handling based on the response body mode.
 */
public final class ResponseBodyModeGeneration {

    /**
     * Generates response body mode assignment based on request options and return type.
     *
     * @param body the method builder to append generated code.
     */
    public static void generateResponseBodyMode(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(RequestOptions.class);
        body.addStatement(StaticJavaParser
            .parseStatement("ResponseBodyMode responseBodyMode = CodegenUtil.getOrDefaultResponseBodyMode"
                + "(httpRequest.getRequestOptions());"));
    }

    /**
     * Handles deserialization response mode logic.
     *
     * @param body the method builder to append generated code.
     * @param returnTypeName the return type of the method.
     * @param method The Http method request context
     */
    public static void handleResponseBody(BlockStmt body, String returnTypeName, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(HttpResponse.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseAccessHelper.class);

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
            body.addStatement(StaticJavaParser.parseStatement("String returnTypeName = \"" + returnTypeName + "\";"));
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
                generateResponseBodyMode(body);
                handleResponseBody(body, returnTypeName, method);
                createResponseIfNecessary(body);
            }
        } else {
            handleRestResponseReturnType(body, returnTypeName, method);
            if (!isPrimitiveOrWrapper(returnTypeName)) {
                closeResponse(body);
                CastExpr castExpr
                    = new CastExpr(StaticJavaParser.parseClassOrInterfaceType(returnTypeName), new NameExpr("result"));

                // Add the cast statement to the method body
                body.addStatement(new ReturnStmt(castExpr));
            }
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
        if (method.getHttpMethod() == HttpMethod.HEAD
            && (returnTypeName.contains("Boolean") || returnTypeName.contains("boolean"))) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("expectedResponse"));
        } else if (returnTypeName.contains("byte[]")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser
                .parseStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;"));
            closeResponse(body);
            body.addStatement(StaticJavaParser.parseStatement(
                "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;"));
        } else if (returnTypeName.contains("InputStream")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser.parseStatement("return responseBody.toStream();"));
        } else if (returnTypeName.contains("BinaryData")) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            closeResponse(body);
        } else {
            handleResponseBody(body, returnTypeName, method);
        }
    }

    private ResponseBodyModeGeneration() {
    }
}
