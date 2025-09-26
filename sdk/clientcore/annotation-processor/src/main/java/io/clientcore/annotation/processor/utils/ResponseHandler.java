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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
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
     * @param serializationFormatSet indicates if a serialization format is set.
     */
    public static void generateResponseHandling(BlockStmt body, TypeMirror returnType, HttpRequestContext method,
        boolean serializationFormatSet) {
        java.lang.reflect.Type entityType = TypeConverter.getEntityType(returnType);

        boolean usingTryWithResources = useTryWithResources(entityType, method);
        if (usingTryWithResources) {
            TryStmt statement = StaticJavaParser
                .parseStatement("try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {}")
                .asTryStmt();
            statement.setLineComment("\n Send the request through the httpPipeline");

            body.addStatement(statement);
            body = statement.getTryBlock();
        } else {
            Statement statement = StaticJavaParser
                .parseStatement("Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);");
            statement.setLineComment("\n Send the request through the httpPipeline");
            body.addStatement(statement);
        }

        validateResponseStatus(body, method, usingTryWithResources);

        handleRequestReturn(body, returnType, entityType, method, serializationFormatSet);
    }

    private static boolean useTryWithResources(java.lang.reflect.Type entityType, HttpRequestContext method) {
        // Use try-with-resources, where the Response<BinaryData> is the resource, if one of the following are true:
        // - Return type is a Void.class, exclude void.class as that will be handled separately.
        // - The request used method HEAD and return type boolean.
        // - Return type is byte[], which will consume the entire network response eagerly.
        // - Return type isn't InputStream or BinaryData, both will need to have the network response remain open.
        if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)
            || TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            return false;
        }

        return entityType == Void.class
            || (method.getHttpMethod() == HttpMethod.HEAD && isBooleanType(entityType))
            || TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class);
    }

    private static void validateResponseStatus(BlockStmt body, HttpRequestContext method,
        boolean usingTryWithResources) {
        addStatusCodeCheck(body, method);
        addExceptionHandling(body, method, usingTryWithResources);
    }

    private static void addStatusCodeCheck(BlockStmt body, HttpRequestContext method) {
        body.addStatement(StaticJavaParser.parseStatement("int responseCode = networkResponse.getStatusCode();"));
        String expectedResponseCheck
            = AnnotationProcessorUtils.generateExpectedResponseCheck(method.getExpectedStatusCodes());
        body.addStatement(StaticJavaParser.parseStatement("boolean expectedResponse = " + expectedResponseCheck + ";"));
    }

    private static void addExceptionHandling(BlockStmt body, HttpRequestContext method, boolean usingTryWithResources) {
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
        if (!usingTryWithResources) {
            closeResponse(errorBlock);
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
        HttpRequestContext method, boolean serializationFormatSet) {
        boolean returnIsResponse = TypeConverter.isResponseType(returnType);

        // TODO (alzimmer): Base64Uri needs to be handled. Determine how this will show up in code generation and then
        //  add support for it.
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
            // Return is a byte[]. Convert the network response body into a byte[].
            body.addStatement(StaticJavaParser.parseStatement("BinaryData responseBody = networkResponse.getValue();"));
            // If the wire type is Base64Uri, decode it accordingly.
            boolean isBase64Uri = false;
            TypeMirror wireType = method.getReturnValueWireType();
            if (wireType != null && wireType.getKind() == TypeKind.DECLARED) {
                DeclaredType declaredWireType = (DeclaredType) wireType;
                TypeElement wireTypeElement = (TypeElement) declaredWireType.asElement();
                isBase64Uri = Base64Uri.class.getCanonicalName().equals(wireTypeElement.getQualifiedName().toString());
            }
            String returnExpr;
            if (isBase64Uri) {
                body.tryAddImportToParentCompilationUnit(Base64Uri.class);
                returnExpr = "responseBody != null ? new Base64Uri(responseBody.toBytes()).decodedBytes() : null";
            } else {
                returnExpr = "responseBody != null ? responseBody.toBytes() : null";
            }

            // Return responseBody.toBytes(), or null if it was null, as-is which will have the behavior of
            // null -> null, empty -> empty, and data -> data, which offers three unique states for knowing information
            // about the network response shape, as nullness != emptiness.
            addReturnStatement(body, returnIsResponse, returnExpr);
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
                        handleDeclaredTypes(body, returnType, serializationFormatSet, true, true);
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
            handleDeclaredTypes(body, returnType, serializationFormatSet, returnIsResponse, false);
        }
    }

    private static void handleDeclaredTypes(BlockStmt body, TypeMirror returnType, boolean serializationFormatSet,
        boolean returnIsResponse, boolean closeResponse) {
        String typeCast = determineTypeCast(returnType, body);

        // Initialize the variable that will be used in the return statement.
        body.addStatement(StaticJavaParser.parseStatement(typeCast + " deserializedResult;"));
        handleDeclaredTypeResponse(body, (DeclaredType) returnType, serializationFormatSet, typeCast);
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

    private static String determineTypeCast(TypeMirror returnType, BlockStmt body) {
        if (returnType instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            body.tryAddImportToParentCompilationUnit(CoreUtils.class);

            if (!declaredType.getTypeArguments().isEmpty()) {
                TypeMirror firstGenericType = declaredType.getTypeArguments().get(0);
                if (firstGenericType.getKind() == TypeKind.ARRAY) {
                    ArrayType arrayType = (ArrayType) firstGenericType;
                    String componentTypeName = arrayType.getComponentType().toString();
                    return componentTypeName + "[]";
                } else if (firstGenericType instanceof DeclaredType) {
                    DeclaredType genericDeclaredType = (DeclaredType) firstGenericType;
                    TypeElement genericTypeElement = (TypeElement) genericDeclaredType.asElement();
                    body.findCompilationUnit()
                        .ifPresent(compilationUnit -> compilationUnit
                            .addImport(genericTypeElement.getQualifiedName().toString()));
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
            }
            return typeElement.getSimpleName().toString();
        }
        return returnType.toString();
    }

    private static boolean isBooleanType(java.lang.reflect.Type entityType) {
        return TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class);
    }

    private static void handleDeclaredTypeResponse(BlockStmt body, DeclaredType returnType,
        boolean serializationFormatSet, String typeCast) {
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);
        body.tryAddImportToParentCompilationUnit(ParameterizedType.class);

        if (!returnType.getTypeArguments().isEmpty()) {
            body.addStatement(StaticJavaParser.parseStatement("ParameterizedType returnType = "
                + AnnotationProcessorUtils.createParameterizedTypeStatement(returnType, body) + ";"));
        } else {
            body.addStatement(
                "ParameterizedType returnType = CoreUtils.createParameterizedType(" + typeCast + ".class);");
        }

        if (serializationFormatSet) {
            addSerializationFormatResponseBodyStatements(body);
        } else {
            body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
            body.addStatement(
                "SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(networkResponse.getHeaders());");
            addSerializationFormatResponseBodyStatements(body);
        }
    }

    private static void closeResponse(BlockStmt body) {
        body.addStatement(StaticJavaParser.parseStatement("networkResponse.close();"));
    }

    private static void addSerializationFormatResponseBodyStatements(BlockStmt body) {
        body.addStatement("if (jsonSerializer.supportsFormat(serializationFormat)) { "
            + "    deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType); "
            + "} else if (xmlSerializer.supportsFormat(serializationFormat)) { "
            + "    deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType); "
            + "} else { "
            + "    throw LOGGER.throwableAtError().addKeyValue(\"serializationFormat\", serializationFormat.name())\n"
            + "                .log(\"None of the provided serializers support the format.\", UnsupportedOperationException::new);"
            + "}");
    }

    private ResponseHandler() {
    }
}
