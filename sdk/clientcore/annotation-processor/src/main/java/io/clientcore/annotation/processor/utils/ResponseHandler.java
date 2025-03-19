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
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.CoreUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to generate response body mode assignment and response handling based on the response body mode.
 */
public final class ResponseHandler {

    /**
     * Handles the generation of the complete response processing flow based on the return type.
     *
     * @param body the method builder to append generated code.
     * @param returnType the return type of the method.
     * @param method whether request options are used.
     * @param serializationFormatSet indicates if serialization format is set.
     */
    public static void generateResponseHandling(BlockStmt body, TypeMirror returnType, HttpRequestContext method,
        boolean serializationFormatSet) {
        if (isVoidReturnType(returnType)) {
            closeResponse(body);
            if (returnType.toString().equals("java.lang.Void")) {
                body.addStatement(new ReturnStmt("null"));
            }
        } else if (TypeConverter.isResponseType(returnType)) {
            handleResponseType(body, returnType, method, serializationFormatSet);
        } else {
            handleNonResponseType(body, returnType, method, serializationFormatSet);
        }
    }

    private static boolean isVoidReturnType(TypeMirror returnType) {
        return returnType.getKind() == TypeKind.VOID || returnType.toString().equals("java.lang.Void");
    }

    private static void handleResponseType(BlockStmt body, TypeMirror returnType, HttpRequestContext method,
        boolean serializationFormatSet) {
        java.lang.reflect.Type bodyType = TypeConverter.getEntityType(returnType);
        if (returnType.toString().contains("Void")) {
            closeResponse(body);
            body.addStatement(StaticJavaParser.parseStatement(
                "return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);"));
        } else {
            handleResponseBody(body, returnType, bodyType, method, serializationFormatSet);
            createResponseIfNecessary(body);
        }
    }

    private static void handleResponseBody(BlockStmt body, TypeMirror returnType, java.lang.reflect.Type entityType,
        HttpRequestContext method, boolean serializationFormatSet) {

        String typeCast = determineTypeCast(returnType, entityType, method, body);

        body.addStatement("Object result = " + determineResultExpression(entityType, method));

        if (returnType instanceof DeclaredType) {
            handleDeclaredTypeResponse(body, (DeclaredType) returnType, serializationFormatSet);
            if (!((DeclaredType) returnType).getTypeArguments().isEmpty()) {
                body.addStatement(StaticJavaParser.parseStatement("return new Response<>("
                    + "networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (" + typeCast
                    + ") result);"));
            } else {
                closeResponse(body);
                CastExpr castExpr
                    = new CastExpr(StaticJavaParser.parseType(returnType.toString()), new NameExpr("result"));

                body.addStatement(new ReturnStmt(castExpr));
            }
        }
    }

    private static String determineTypeCast(TypeMirror returnType, java.lang.reflect.Type entityType,
        HttpRequestContext method, BlockStmt body) {
        if (method.getHttpMethod() == HttpMethod.HEAD && isBooleanType(entityType)) {
            return "Boolean";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            return "byte[]";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            return "InputStream";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            return "BinaryData";
        } else if (returnType instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            body.tryAddImportToParentCompilationUnit(CoreUtils.class);

            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeMirror firstGenericType = declaredType.getTypeArguments().get(0);
                DeclaredType genericDeclaredType = (DeclaredType) firstGenericType;
                TypeElement genericTypeElement = (TypeElement) genericDeclaredType.asElement();
                body.findCompilationUnit()
                    .ifPresent(
                        compilationUnit -> compilationUnit.addImport(genericTypeElement.getQualifiedName().toString()));
                if (genericTypeElement.getQualifiedName().contentEquals(List.class.getCanonicalName())) {
                    String typeArgs = genericDeclaredType.getTypeArguments()
                        .stream()
                        .map(arg -> ((DeclaredType) arg).asElement().getSimpleName().toString())
                        .collect(Collectors.joining(", "));

                    return ((DeclaredType) firstGenericType).asElement().getSimpleName().toString() + "<" + typeArgs
                        + ">";
                } else {
                    return genericTypeElement.getSimpleName().toString();
                }
            }
            return typeElement.getSimpleName().toString();
        }
        return returnType.toString();
    }

    private static String determineResultExpression(java.lang.reflect.Type entityType, HttpRequestContext method) {
        if (method.getHttpMethod() == HttpMethod.HEAD && isBooleanType(entityType)) {
            return "(responseStatusCode / 100) == 2;";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // TODO: Add support for Base64Uri
            // if (returnValueWireType == Base64Uri.class) {
            // responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            // }
            return "responseBody != null ? responseBody.toBytes() : null;";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            return "responseBody.toStream();";
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            return "networkResponse.getValue();";
        }
        return "null;";
    }

    private static boolean isBooleanType(java.lang.reflect.Type entityType) {
        return TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class);
    }

    private static void handleDeclaredTypeResponse(BlockStmt body, DeclaredType returnType,
        boolean serializationFormatSet) {
        TypeElement typeElement = (TypeElement) returnType.asElement();
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);

        if (!returnType.getTypeArguments().isEmpty()) {
            TypeMirror firstGenericType = returnType.getTypeArguments().get(0);

            if (firstGenericType instanceof DeclaredType) {
                DeclaredType genericDeclaredType = (DeclaredType) firstGenericType;
                TypeElement genericTypeElement = (TypeElement) genericDeclaredType.asElement();

                body.findCompilationUnit()
                    .ifPresent(
                        compilationUnit -> compilationUnit.addImport(genericTypeElement.getQualifiedName().toString()));

                if (genericTypeElement.getQualifiedName().contentEquals(List.class.getCanonicalName())) {
                    if (!genericDeclaredType.getTypeArguments().isEmpty()) {
                        String innerType = ((DeclaredType) genericDeclaredType.getTypeArguments().get(0)).asElement()
                            .getSimpleName()
                            .toString();
                        body.addStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                            + genericTypeElement.getSimpleName() + ".class, " + innerType + ".class);");
                    }
                } else {
                    String genericType
                        = ((DeclaredType) returnType.getTypeArguments().get(0)).asElement().getSimpleName().toString();
                    body.addStatement("ParameterizedType returnType = CoreUtils.createParameterizedType("
                        + typeElement.getSimpleName() + ".class, " + genericType + ".class);");
                }
            }
        } else {
            body.addStatement(
                "ParameterizedType returnType = CoreUtils.createParameterizedType(" + returnType + ".class);");
        }

        if (serializationFormatSet) {
            addSerializationFormatResponseBodyStatements(body);
        } else {
            body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
            body.addStatement(
                "SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());");
            addSerializationFormatResponseBodyStatements(body);
        }
    }

    private static void addSerializationFormatResponseBodyStatements(BlockStmt body) {
        body.addStatement("if (jsonSerializer.supportsFormat(serializationFormat)) { "
            + "    result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType); "
            + "} else if (xmlSerializer.supportsFormat(serializationFormat)) { "
            + "    result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType); "
            + "} else { " + "    throw new RuntimeException(new UnsupportedOperationException("
            + "        \"None of the provided serializers support the format: \" + serializationFormat + \".\")); "
            + "}");
    }

    private static void handleNonResponseType(BlockStmt body, TypeMirror returnType, HttpRequestContext method,
        boolean serializationFormatSet) {
        java.lang.reflect.Type entityType = TypeConverter.getEntityType(returnType);
        processEntityReturnType(body, returnType, entityType, method, serializationFormatSet);
        if (!returnType.getKind().isPrimitive()
            && returnType.getKind() != TypeKind.ARRAY
            && returnType.getKind() != TypeKind.DECLARED) {
            closeResponse(body);
            CastExpr castExpr = new CastExpr(StaticJavaParser.parseType(returnType.toString()), new NameExpr("result"));
            body.addStatement(new ReturnStmt(castExpr));
        }
    }

    private static void processEntityReturnType(BlockStmt body, TypeMirror returnType,
        java.lang.reflect.Type entityType, HttpRequestContext method, boolean serializationFormatSet) {
        if (isHeadRequestWithBooleanResponse(method, entityType)) {
            closeResponse(body);
            body.addStatement(new ReturnStmt("expectedResponse"));
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            handleByteArrayResponse(body);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            handleInputStreamResponse(body);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            body.tryAddImportToParentCompilationUnit(BinaryData.class);
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
            closeResponse(body);
        } else {
            handleResponseBody(body, returnType, entityType, method, serializationFormatSet);
        }
    }

    private static boolean isHeadRequestWithBooleanResponse(HttpRequestContext method,
        java.lang.reflect.Type entityType) {
        return method.getHttpMethod() == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class));
    }

    private static void handleByteArrayResponse(BlockStmt body) {
        body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
        body.addStatement(StaticJavaParser
            .parseStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;"));
        body.addStatement(StaticJavaParser.parseStatement(
            "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;"));
    }

    private static void handleInputStreamResponse(BlockStmt body) {
        body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
        body.addStatement(StaticJavaParser.parseStatement("return responseBody.toStream();"));
    }

    private static void closeResponse(BlockStmt body) {
        body.tryAddImportToParentCompilationUnit(IOException.class);
        body.tryAddImportToParentCompilationUnit(UncheckedIOException.class);
        body.addStatement(StaticJavaParser.parseStatement("try { networkResponse.close(); } catch (IOException e) { "
            + "throw LOGGER.logThrowableAsError(new UncheckedIOException(e)); }"));
    }

    /**
     * Adds a return statement for response handling when necessary.
     *
     * @param body the method builder to append generated code.
     */
    private static void createResponseIfNecessary(BlockStmt body) {
        if (body.getStatements().get(body.getStatements().size() - 1).toString().contains("return")) {
            return;
        }
        body.addStatement(StaticJavaParser.parseStatement("return response;"));
    }

    private ResponseHandler() {
    }
}
