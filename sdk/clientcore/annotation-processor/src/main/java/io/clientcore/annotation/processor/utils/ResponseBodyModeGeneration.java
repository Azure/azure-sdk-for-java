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
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor8;
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
    public static void handleResponseBody(BlockStmt body, TypeMirror returnType, java.lang.reflect.Type entityType,
        HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(ResponseBodyMode.class);

        String typeCast = "Object";
        if (method.getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            body.addStatement("Object result = (responseStatusCode / 100) == 2");
            typeCast = "Boolean";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            body.addStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;");

            // TODO: Add support for Base64Uri
            // if (returnValueWireType == Base64Uri.class) {
            // responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            // }

            body.addStatement(
                "Object result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;");
            typeCast = "byte[]";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = response.getValue();"));
            body.addStatement("Object result = responseBody.toStream();");
            typeCast = "InputStream";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            body.addStatement("Object result = networkResponse.getValue();");
            typeCast = "BinaryData";
        } else if (returnType instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            body.tryAddImportToParentCompilationUnit(CoreUtils.class);

            // Ensure type arguments exist before accessing them
            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeMirror firstGenericType = declaredType.getTypeArguments().get(0);

                if (firstGenericType instanceof DeclaredType) {
                    DeclaredType genericDeclaredType = (DeclaredType) firstGenericType;
                    TypeElement genericTypeElement = (TypeElement) genericDeclaredType.asElement();

                    typeCast = genericTypeElement.getSimpleName().toString();
                    body.findCompilationUnit()
                        .ifPresent(compilationUnit -> compilationUnit
                            .addImport(genericTypeElement.getQualifiedName().toString()));

                    // Check if it's specifically a List<T>
                    if (genericTypeElement.getQualifiedName().contentEquals(List.class.getCanonicalName())) {
                        if (!genericDeclaredType.getTypeArguments().isEmpty()) {
                            TypeMirror innerType = genericDeclaredType.getTypeArguments().get(0);
                            body.addStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                                + genericTypeElement.getSimpleName() + ".class, " + innerType + ".class);");
                        }
                    } else {
                        String genericType = declaredType.getTypeArguments().get(0).toString();
                        body.addStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                            + typeElement.getSimpleName() + ".class, " + genericType + ".class);");
                    }
                }
            }
            body.addStatement(
                "Object result = decodeNetworkResponse(networkResponse.getValue(), serializer, returnType);");
        }

        body.addStatement(StaticJavaParser.parseStatement("return new Response<>("
            + "networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (" + typeCast + ") result);"));
    }

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnType the return type of the method.
     * @param method whether request options are used.
     */
    public static void generateResponseHandling(BlockStmt body, TypeMirror returnType, HttpRequestContext method) {
        java.lang.reflect.Type bodyType = null;

        if (returnType.getKind() == TypeKind.VOID) {
            closeResponse(body);
        } else if (returnType.toString().equals("java.lang.Void")) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("null"));
        } else if (TypeConverter.isResponseType(returnType)) {
            bodyType = TypeConverter.getEntityType(returnType);
            if (returnType.toString().contains("Void")) {
                closeResponse(body);
                body.addStatement(StaticJavaParser.parseStatement("return new Response<>("
                    + "networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);"));
            } else {
                // If this type has type arguments, then we look at the last one to determine if it expects a body
                generateResponseBodyMode(body);
                handleResponseBody(body, returnType, bodyType, method);
                createResponseIfNecessary(body);
            }
        } else {
            if (returnType.getKind().isPrimitive()) {
                bodyType = returnType.accept(new SimpleTypeVisitor8<java.lang.reflect.Type, Void>() {
                    @Override
                    public java.lang.reflect.Type visitPrimitive(PrimitiveType t, Void p) {
                        return TypeConverter.getPrimitiveClass(t);
                    }
                }, null);
            } else if (returnType.getKind() == TypeKind.ARRAY) {
                bodyType = TypeConverter.getEntityType(returnType);
            }
            handleRestResponseReturnType(body, returnType, bodyType, method);
            if (!returnType.getKind().isPrimitive() && returnType.getKind() != TypeKind.ARRAY) {
                closeResponse(body);
                CastExpr castExpr
                    = new CastExpr(StaticJavaParser.parseType(returnType.toString()), new NameExpr("result"));

                // Add the cast statement to the method body
                body.addStatement(new ReturnStmt(castExpr));
            }
        }
    }

    private static void closeResponse(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(IOException.class);
        body.tryAddImportToParentCompilationUnit(UncheckedIOException.class);

        body.addStatement(StaticJavaParser.parseStatement("try { networkResponse.close(); }"
            + "catch (IOException e) { throw LOGGER.logThrowableAsError(new UncheckedIOException(e)); }"));
    }

    /**
     * Adds a return statement for response handling when necessary.
     *
     * @param body the method builder to append generated code.
     */
    public static void createResponseIfNecessary(BlockStmt body) {
        if (body.getStatements().get(body.getStatements().size() - 1).toString().contains("return")) {
            return;
        }
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
    public static void handleRestResponseReturnType(BlockStmt body, TypeMirror returnTypeName,
        java.lang.reflect.Type entityType, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(BinaryData.class);
        if (method.getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("expectedResponse"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
            body.addStatement(StaticJavaParser
                .parseStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;"));
            body.addStatement(StaticJavaParser.parseStatement(
                "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
            body.addStatement(StaticJavaParser.parseStatement("return responseBody.toStream();"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
            closeResponse(body);
        } else {
            handleResponseBody(body, returnTypeName, entityType, method);
        }
    }

    private ResponseBodyModeGeneration() {
    }
}
