// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.HttpResponse;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

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
     */
    public static void generateResponseBodyMode(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(RequestOptions.class);
        body.addStatement(StaticJavaParser.parseStatement(
            "ResponseBodyMode responseBodyMode = getOrDefaultResponseBodyMode" + "(httpRequest.getRequestOptions());"));
    }

    /**
     * Handles deserialization response mode logic.
     *
     * @param body the method builder to append generated code.
     * @param returnType the return type of the method.
     * @param entityType the entity type of the method.
     * @param method The Http method request context
     */
    public static void handleResponseBody(BlockStmt body, Type returnType, java.lang.reflect.Type entityType,
        HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);
        body.tryAddImportToParentCompilationUnit(HttpResponse.class);
        body.tryAddImportToParentCompilationUnit(HttpResponseAccessHelper.class);

        if (method.getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            body.addStatement("Object result = (responseStatusCode / 100) == 2");
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            body.addStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;");

            // TODO: Add support for Base64Uri
            // if (returnValueWireType == Base64Uri.class) {
            // responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            // }

            body.addStatement(
                "Object result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;");
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement("Object result = responseBody.toStream();");
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            body.addStatement("Object result = response.getBody();");
        } else {

            if (returnType instanceof ClassOrInterfaceType) {
                ClassOrInterfaceType classType = (ClassOrInterfaceType) returnType;
                body.tryAddImportToParentCompilationUnit(CoreUtils.class);

                // Ensure type arguments exist before accessing them
                if (classType.getTypeArguments().isPresent()) {
                    NodeList<Type> typeArguments = classType.getTypeArguments().get();

                    if (!typeArguments.isEmpty()) {
                        Type firstGenericType = typeArguments.get(0);

                        if (firstGenericType instanceof ClassOrInterfaceType) {
                            ClassOrInterfaceType genericClassType = (ClassOrInterfaceType) firstGenericType;

                            // Check if it's specifically a List<T>
                            if ("List".equals(genericClassType.getNameAsString())
                                && genericClassType.getTypeArguments().isPresent()) {
                                String innerType = genericClassType.getTypeArguments().get().get(0).toString(); // Extract Foo

                                body.addStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                                    + genericClassType.getNameAsString() + ".class, " + innerType + ".class);");
                            } else {
                                String genericType = classType.getTypeArguments().get().get(0).toString(); // Extracts Foo
                                body.addStatement(
                                    "ParameterizedType returnType = " + "CoreUtils.createParameterizedType("
                                        + classType.getNameAsString() + ".class, " + genericType + ".class);");
                            }
                        }
                    }
                }
                body.addStatement(
                    "Object result = decodeByteArray(response.getBody().toBytes(), serializer, returnType);");
            }
        }
        body.addStatement(StaticJavaParser.parseStatement("if (responseBodyMode == ResponseBodyMode.DESERIALIZE)"
            + "{ HttpResponseAccessHelper.setValue((HttpResponse<?>) response, result); } else {"
            + "HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response, (body) -> result); }"));
    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnType the return type of the method.
     * @param method whether request options are used.
     */
    public static void generateResponseHandling(BlockStmt body, Type returnType, HttpRequestContext method) {
        java.lang.reflect.Type bodyType = null;
        if (returnType.isVoidType()) {
            closeResponse(body);
        } else if (returnType.toString().equals("Void")) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("null"));
        } else if (returnType.asString().contains("Response")) {
            bodyType = getBodyType(returnType);
            if (returnType.asString().contains("Void")) {
                closeResponse(body);
                createResponseIfNecessary(body);
            } else {
                // If this type has type arguments, then we look at the last one to determine if it expects a body
                generateResponseBodyMode(body);
                handleResponseBody(body, returnType, bodyType, method);
                createResponseIfNecessary(body);
            }
        } else {
            if (returnType.isPrimitiveType()) {
                if (returnType instanceof PrimitiveType) {
                    bodyType = TypeConverter.getPrimitiveClass((PrimitiveType) returnType);
                }
            } else if (returnType.isArrayType()) {
                bodyType = TypeConverter.getEntityType(returnType);
            }
            handleRestResponseReturnType(body, returnType, bodyType, method);
            if (!(returnType.isPrimitiveType()) && !returnType.isArrayType()) {
                closeResponse(body);
                CastExpr castExpr = new CastExpr(returnType, new NameExpr("result"));

                // Add the cast statement to the method body
                body.addStatement(new ReturnStmt(castExpr));
            }
        }
    }

    private static java.lang.reflect.Type getBodyType(Type returnType) {
        if (returnType instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType classType = (ClassOrInterfaceType) returnType;

            // Ensure type arguments exist before accessing them
            if (classType.getTypeArguments().isPresent()) {
                NodeList<Type> typeArguments = classType.getTypeArguments().get();

                if (!typeArguments.isEmpty()) {
                    Type innerType = typeArguments.get(0); // First generic type (e.g., List<Foo> or Foo<String>)
                    return TypeUtil.createParameterizedType(innerType.toString().getClass());
                } else {
                    // No generic type on this RestResponse subtype, so we go up to parent
                    return TypeConverter.toReflectType(returnType);
                }

            }
        }
        return Object.class; // Fallback
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
     * @param entityType the entity type of the method.
     * @param method the HttpMethod context
     */
    public static void handleRestResponseReturnType(BlockStmt body, Type returnTypeName,
        java.lang.reflect.Type entityType, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(BinaryData.class);
        if (method.getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("expectedResponse"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser
                .parseStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;"));
            body.addStatement(StaticJavaParser.parseStatement(
                "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            body.addStatement(StaticJavaParser.parseStatement("return responseBody.toStream();"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getBody();"));
            closeResponse(body);
        } else {
            handleResponseBody(body, returnTypeName, entityType, method);
        }
    }

    private ResponseBodyModeGeneration() {
    }
}
