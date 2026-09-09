// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.Base64Uri;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.GeneratedCodeUtils;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

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
     */
    public static void generateResponseHandling(BlockStmt body, TypeMirror returnType, HttpRequestContext method) {
        java.lang.reflect.Type entityType = TypeConverter.getEntityType(returnType);

        boolean usingTryWithResources = useTryWithResources(entityType);
        Statement sendStatement = StaticJavaParser
            .parseStatement("Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);");
        sendStatement.setLineComment("\n Send the request through the httpPipeline");
        body.addStatement(sendStatement);

        validateResponseStatus(body, method);

        TryStmt closingStatement = null;
        if (usingTryWithResources) {
            closingStatement = StaticJavaParser
                .parseStatement("try (Response<BinaryData> networkResponseToClose = networkResponse) {}")
                .asTryStmt();
            body.addStatement(closingStatement);
            body = closingStatement.getTryBlock();
        }

        handleRequestReturn(body, returnType, entityType, method);
        if (usingTryWithResources) {
            body.findAll(NameExpr.class)
                .stream()
                .filter(expression -> "networkResponse".equals(expression.getNameAsString()))
                .forEach(expression -> expression.setName("networkResponseToClose"));
            boolean responseIsReferenced = body.findAll(NameExpr.class)
                .stream()
                .anyMatch(expression -> "networkResponseToClose".equals(expression.getNameAsString()));
            if (!responseIsReferenced) {
                closingStatement.getResources().clear();
                closingStatement.setFinallyBlock(
                    new BlockStmt().addStatement(StaticJavaParser.parseStatement("networkResponse.close();")));
            }
        }
    }

    static boolean useTryWithResources(java.lang.reflect.Type entityType) {
        // Use try-with-resources, where the Response<BinaryData> is the resource, if one of the following are true:
        // - Return type is a Void.class, exclude void.class as that will be handled separately.
        // - The request used method HEAD and return type boolean.
        // - Return type is byte[], which will consume the entire network response eagerly.
        // - Return type isn't InputStream or BinaryData, both will need to have the network response remain open.
        return entityType != Void.TYPE
            && !TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)
            && !TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class);
    }

    private static void validateResponseStatus(BlockStmt body, HttpRequestContext method) {
        addStatusCodeCheck(body, method);
        addExceptionHandling(body, method);
    }

    private static void addStatusCodeCheck(BlockStmt body, HttpRequestContext method) {
        body.addStatement(StaticJavaParser.parseStatement("int responseCode = networkResponse.getStatusCode();"));
        String expectedResponseCheck
            = AnnotationProcessorUtils.generateExpectedResponseCheck(method.getExpectedStatusCodes());
        body.addStatement(StaticJavaParser.parseStatement("boolean expectedResponse = " + expectedResponseCheck + ";"));
    }

    static void addExceptionHandling(BlockStmt body, HttpRequestContext method) {
        BlockStmt errorBlock = new BlockStmt();
        body.tryAddImportToParentCompilationUnit(GeneratedCodeUtils.class);
        Map<Integer, HttpRequestContext.ExceptionBodyTypeInfo> mappings = method.getExceptionBodyMappings();
        if (!mappings.isEmpty() && method.getDefaultExceptionBodyType() != null) {
            // Both map and default
            getStatusCodeMapping(body, errorBlock, mappings);
            errorBlock.addStatement("java.lang.reflect.ParameterizedType defaultErrorBodyType = "
                + AnnotationProcessorUtils.createParameterizedTypeStatement(method.getDefaultExceptionBodyType(), body)
                + ";");
            errorBlock.addStatement(
                "GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, statusToExceptionTypeMap, LOGGER);");
        } else if (!mappings.isEmpty()) {
            // Only map
            getStatusCodeMapping(body, errorBlock, mappings);
            errorBlock.addStatement(
                "GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, statusToExceptionTypeMap, LOGGER);");
        } else if (method.getDefaultExceptionBodyType() != null) {
            // Only default
            errorBlock.addStatement("java.lang.reflect.ParameterizedType defaultErrorBodyType = "
                + AnnotationProcessorUtils.createParameterizedTypeStatement(method.getDefaultExceptionBodyType(), body)
                + ";");
            errorBlock.addStatement(
                "GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);");
        } else {
            // Neither
            Statement stmt = StaticJavaParser.parseStatement(
                "GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, "
                    + "xmlSerializer, null, null, LOGGER);");
            stmt.setLineComment("\n Handle unexpected response");
            errorBlock.addStatement(stmt);
        }
        IfStmt ifStmt = new IfStmt()
            .setCondition(new UnaryExpr(new NameExpr("expectedResponse"), UnaryExpr.Operator.LOGICAL_COMPLEMENT))
            .setThenStmt(errorBlock);
        body.addStatement(ifStmt);
    }

    private static void getStatusCodeMapping(BlockStmt body, BlockStmt errorBlock,
        Map<Integer, HttpRequestContext.ExceptionBodyTypeInfo> mappings) {
        body.tryAddImportToParentCompilationUnit(Map.class);
        body.tryAddImportToParentCompilationUnit(HashMap.class);
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);
        errorBlock.addStatement(
            "Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();");
        for (Map.Entry<Integer, HttpRequestContext.ExceptionBodyTypeInfo> entry : mappings.entrySet()) {
            if (entry.getValue().isDefaultObject() || entry.getValue().getTypeMirror() == null) {
                errorBlock.addStatement("statusToExceptionTypeMap.put(" + entry.getKey()
                    + ", CoreUtils.createParameterizedType(Object.class));");
            } else {
                errorBlock.addStatement("statusToExceptionTypeMap.put(" + entry.getKey() + ", "
                    + AnnotationProcessorUtils.createParameterizedTypeStatement(entry.getValue().getTypeMirror(), body)
                    + ");");
            }
        }
    }

    private static void handleRequestReturn(BlockStmt body, TypeMirror returnType, java.lang.reflect.Type entityType,
        HttpRequestContext method) {
        boolean returnIsResponse = TypeConverter.isResponseType(returnType);

        if (returnType.getKind() == TypeKind.VOID) {
            // This handles the case where the API returns 'void' itself. This will result in code such as
            // "networkResponse.close()" as 'void' return doesn't use try-with-resources as the compiler will complain
            // about an empty try block.
            closeResponse(body);
        } else if (entityType == Void.TYPE || entityType == Void.class) {
            // This handles the case where the API returns 'Response<Void>' or 'Void'. Unlike 'void' itself this will
            // use try-with-resources as "return null;" will be in the try-with-resources block and the compiler won't
            // complain about an empty try block.
            addReturnStatement(body, returnIsResponse, "null");
        } else if (method.getHttpMethod() == HttpMethod.HEAD && isBooleanType(entityType)) {
            // HTTP method was either HEAD or the return is a boolean. Use the status code to determine response value.
            addReturnStatement(body, returnIsResponse, "expectedResponse");
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            handleByteArrayReturn(body, returnIsResponse, method.getReturnValueWireType());
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            // Return type is an InputStream. Return the network response body as an InputStream.
            // DO NOT close the network response for this return as it will result in the InputStream either being
            // closed or invalid when it is returned.
            addReturnStatement(body, returnIsResponse, "networkResponse.getValue().toStream()");
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // Return type is a BinaryData. Return the network response body.
            // DO NOT close the network response for this return as it will result in the BinaryData either being closed or invalid when it is returned.
            if (returnIsResponse) {
                if (returnType instanceof DeclaredType) {
                    DeclaredType declaredType = (DeclaredType) returnType;
                    if (!declaredType.getTypeArguments().isEmpty()
                        && ((TypeElement) ((DeclaredType) declaredType.getTypeArguments().get(0)).asElement())
                            .getQualifiedName()
                            .contentEquals(List.class.getCanonicalName())) {
                        // Response<List<BinaryData>> or other generics
                        handleDeclaredTypes(body, returnType, true, true);
                        return;
                    }
                }
                // Raw Response or not a DeclaredType
                body.addStatement(StaticJavaParser.parseStatement("return networkResponse;"));
            } else {
                body.addStatement(StaticJavaParser.parseStatement("return networkResponse.getValue();"));
            }
        } else {
            // Fallback to a generalized code path that handles declared types as the entity, which uses deserialization
            // to create the return.
            handleDeclaredTypes(body, returnType, returnIsResponse, false);
        }
    }

    static void handleByteArrayReturn(BlockStmt body, boolean returnIsResponse, TypeMirror wireType) {
        body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
        boolean isBase64Uri = false;
        if (wireType != null && wireType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredWireType = (DeclaredType) wireType;
            TypeElement wireTypeElement = (TypeElement) declaredWireType.asElement();
            isBase64Uri = Base64Uri.class.getCanonicalName().equals(wireTypeElement.getQualifiedName().toString());
        }

        String returnExpression;
        if (isBase64Uri) {
            body.tryAddImportToParentCompilationUnit(Base64Uri.class);
            returnExpression = "responseBody != null ? new Base64Uri(responseBody.toBytes()).decodedBytes() : null";
        } else {
            body.tryAddImportToParentCompilationUnit(Arrays.class);
            body.tryAddImportToParentCompilationUnit(Base64.class);
            body.tryAddImportToParentCompilationUnit(CoreUtils.class);
            body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
            body.addStatement("byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;");
            body.addStatement("boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 "
                + "&& responseBytes[0] == '\"' && responseBytes[responseBytes.length - 1] == '\"' "
                + "&& GeneratedCodeUtils.isJsonContentType(networkResponse.getHeaders());");
            returnExpression = "quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, "
                + "responseBytes.length - 1)) : responseBytes";
        }

        addReturnStatement(body, returnIsResponse, returnExpression);
    }

    private static void handleDeclaredTypes(BlockStmt body, TypeMirror returnType, boolean returnIsResponse,
        boolean closeResponse) {
        String typeCast = determineTypeCast(returnType);

        // Initialize the variable that will be used in the return statement.
        body.addStatement(StaticJavaParser.parseStatement(typeCast + " deserializedResult;"));
        handleTypeResponse(body, returnType, typeCast);
        if (closeResponse) {
            body.addStatement(StaticJavaParser.parseStatement("networkResponse.close();"));
        }
        addReturnStatement(body, returnIsResponse, "deserializedResult");
    }

    // Helper method that creates the return statement as either Response<T> or T.
    private static void addReturnStatement(BlockStmt body, boolean returnIsResponse, String responseValue) {
        if (returnIsResponse) {
            body.addStatement(StaticJavaParser.parseStatement("return new Response<>(networkResponse.getRequest(), "
                + "responseCode, networkResponse.getHeaders(), " + responseValue + ");"));
        } else {
            body.addStatement(StaticJavaParser.parseStatement("return " + responseValue + ";"));
        }
    }

    static String determineTypeCast(TypeMirror returnType) {
        if (TypeConverter.isResponseType(returnType)) {
            DeclaredType responseType = (DeclaredType) returnType;
            if (!responseType.getTypeArguments().isEmpty()) {
                return TypeConverter.getAstType(responseType.getTypeArguments().get(0)).toString();
            }
        }

        return TypeConverter.getAstType(returnType).toString();
    }

    private static boolean isBooleanType(java.lang.reflect.Type entityType) {
        return TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class);
    }

    private static void handleTypeResponse(BlockStmt body, TypeMirror returnType, String typeCast) {
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);
        body.tryAddImportToParentCompilationUnit(ParameterizedType.class);
        body.addStatement(StaticJavaParser.parseStatement("ParameterizedType returnType = "
            + AnnotationProcessorUtils.createParameterizedTypeStatement(returnType, body) + ";"));

        body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
        body.addStatement(
            "SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());");
        addSerializationFormatResponseBodyStatements(body,
            "String".equals(typeCast) || String.class.getCanonicalName().equals(typeCast));
    }

    private static void closeResponse(BlockStmt body) {
        body.addStatement(StaticJavaParser.parseStatement("networkResponse.close();"));
    }

    static void addSerializationFormatResponseBodyStatements(BlockStmt body, boolean supportsText) {
        String textHandling = supportsText
            ? "if (responseSerializationFormat == SerializationFormat.TEXT) { "
                + "    BinaryData responseBody = networkResponse.getValue(); "
                + "    deserializedResult = responseBody == null ? null : responseBody.toString(); " + "} else "
            : "";
        String jsonCondition = supportsText
            ? "jsonSerializer.supportsFormat(responseSerializationFormat)"
            : "jsonSerializer.supportsFormat(responseSerializationFormat) "
                + "|| responseSerializationFormat == SerializationFormat.TEXT";
        body.addStatement(textHandling + "if (" + jsonCondition + ") { "
            + "    deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType); "
            + "} else if (xmlSerializer.supportsFormat(responseSerializationFormat)) { "
            + "    deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType); "
            + "} else { "
            + "    throw LOGGER.throwableAtError().addKeyValue(\"serializationFormat\", responseSerializationFormat.name())\n"
            + "                .log(\"None of the provided serializers support the format.\", UnsupportedOperationException::new);"
            + "}");
    }

    private ResponseHandler() {
    }
}
