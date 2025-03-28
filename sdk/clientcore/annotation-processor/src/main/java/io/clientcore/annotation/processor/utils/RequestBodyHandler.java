// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility class for handling request bodies in HTTP requests.
 */
public class RequestBodyHandler {
    private static final Map<String, String> TYPE_TO_CONTENT_TYPE;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("byte[]", ContentType.APPLICATION_OCTET_STREAM);
        map.put("java.lang.String", ContentType.APPLICATION_JSON);
        map.put("java.nio.ByteBuffer", ContentType.APPLICATION_OCTET_STREAM);
        map.put("java.io.InputStream", ContentType.APPLICATION_OCTET_STREAM);
        map.put("java.util.Map", ContentType.APPLICATION_JSON); // Common for JSON objects
        map.put("java.util.List", ContentType.APPLICATION_JSON); // Array-like structures
        map.put("java.util.Set", ContentType.APPLICATION_JSON);
        TYPE_TO_CONTENT_TYPE = Collections.unmodifiableMap(map);
    }

    /**
     * Checks if the given parameter type is BinaryData.
     *
     * @param parameterType The type of the parameter.
     * @param elementUtils Utility methods for operating on program elements.
     * @param typeUtils Utility methods for operating on types.
     * @return true if the parameter type is BinaryData, false otherwise.
     */
    public static boolean isBinaryDataType(TypeMirror parameterType, Elements elementUtils, Types typeUtils) {
        return typeUtils.isSameType(parameterType,
            elementUtils.getTypeElement("io.clientcore.core.models.binarydata.BinaryData").asType());
    }

    /**
     * Adds a BinaryData request body to the HTTP request.
     *
     * @param body The block statement to which the request body is added.
     * @param parameterName The name of the parameter.
     */
    public static void addBinaryDataRequestBody(BlockStmt body, String parameterName) {
        body.tryAddImportToParentCompilationUnit(BinaryData.class);
        body.addStatement(StaticJavaParser.parseStatement(String.format("BinaryData binaryData = %s;", parameterName)));
        body.addStatement(StaticJavaParser.parseStatement("if (binaryData.getLength() != null) { "
            + "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength())); "
            + "httpRequest.setBody(binaryData); }"));
    }

    /**
     * Adds a JSON request body to the HTTP request.
     *
     * @param body The block statement to which the request body is added.
     * @param parameterName The name of the parameter.
     */
    public static void addJsonRequestBody(BlockStmt body, String parameterName) {
        body.addStatement(StaticJavaParser.parseStatement(
            String.format("httpRequest.setBody(BinaryData.fromObject(%s, jsonSerializer));", parameterName)));
    }

    /**
     * Checks if the given parameter type is a byte array.
     *
     * @param parameterType The type of the parameter.
     * @return true if the parameter type is a byte array, false otherwise.
     */
    public static boolean isByteArray(TypeMirror parameterType) {
        return parameterType.getKind() == TypeKind.ARRAY
            && "byte".equals(((ArrayType) parameterType).getComponentType().toString());
    }

    /**
     * Adds a byte array request body to the HTTP request.
     *
     * @param body The block statement to which the request body is added.
     * @param parameterName The name of the parameter.
     */
    public static void addByteArrayRequestBody(BlockStmt body, String parameterName) {
        body.addStatement(StaticJavaParser
            .parseStatement(String.format("httpRequest.setBody(BinaryData.fromBytes(%s));", parameterName)));
    }

    /**
     * Checks if the given parameter type is a String.
     *
     * @param parameterType The type of the parameter.
     * @param elementUtils Utility methods for operating on program elements.
     * @param typeUtils Utility methods for operating on types.
     * @return true if the parameter type is a String, false otherwise.
     */
    public static boolean isStringType(TypeMirror parameterType, Elements elementUtils, Types typeUtils) {
        return typeUtils.isSameType(parameterType, elementUtils.getTypeElement("java.lang.String").asType());
    }

    /**
     * Adds a String request body to the HTTP request.
     *
     * @param body The block statement to which the request body is added.
     * @param parameterName The name of the parameter.
     */
    public static void addStringRequestBody(BlockStmt body, String parameterName) {
        body.addStatement(StaticJavaParser
            .parseStatement(String.format("httpRequest.setBody(BinaryData.fromString(%s));", parameterName)));
    }

    /**
     * Checks if the given parameter type is a ByteBuffer.
     *
     * @param parameterType The type of the parameter.
     * @param elementUtils Utility methods for operating on program elements.
     * @param typeUtils Utility methods for operating on types.
     * @return true if the parameter type is a ByteBuffer, false otherwise.
     */
    public static boolean isByteBufferType(TypeMirror parameterType, Elements elementUtils, Types typeUtils) {
        return typeUtils.isSameType(parameterType, elementUtils.getTypeElement("java.nio.ByteBuffer").asType());
    }

    /**
     * Adds a ByteBuffer request body to the HTTP request.
     *
     * @param body The block statement to which the request body is added.
     * @param parameterName The name of the parameter.
     */
    public static void addByteBufferRequestBody(BlockStmt body, String parameterName) {
        body.tryAddImportToParentCompilationUnit(ByteBuffer.class);
        body.addStatement(StaticJavaParser
            .parseStatement(String.format("httpRequest.setBody(BinaryData.fromBytes(%s.array()));", parameterName)));
    }

    /**
     * Sets an empty body for the HTTP request.
     *
     * @param body The block statement to which the empty body is added.
     */
    public static void setEmptyBody(BlockStmt body) {
        body.addStatement(
            StaticJavaParser.parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"));
    }

    /**
     * Sets the Content-Type header for the HTTP request.
     *
     * @param body The block statement to which the Content-Type header is added.
     * @param contentType The content type to set.
     */
    public static void setContentTypeHeader(BlockStmt body, String contentType) {
        body.addStatement(StaticJavaParser.parseStatement(
            String.format("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, \"%s\");", contentType)));
    }

    /**
     * Determines the content type for the HTTP request based on the parameter type.
     *
     * @param explicitContentType The explicit content type provided.
     * @param parameterType The type of the parameter.
     * @param typeUtils Utility methods for operating on types.
     * @return The determined content type.
     */
    public static String determineContentType(String explicitContentType, TypeMirror parameterType, Types typeUtils) {
        if (explicitContentType != null && !explicitContentType.isEmpty()) {
            return explicitContentType;
        }

        // Try mapping by known types
        String typeName = parameterType.toString();
        if (TYPE_TO_CONTENT_TYPE.containsKey(typeName)) {
            return TYPE_TO_CONTENT_TYPE.get(typeName);
        }

        // Additional checks for Form Data (Common in APIs)
        //if (typeName.contains("Multipart") || typeName.contains("FormData") || typeName.contains("FileUpload")) {
        //    return ContentType.MULTIPART_FORM_DATA;
        //}

        // Default to JSON only if it looks like an object
        if (typeUtils.asElement(parameterType) != null) {
            return ContentType.APPLICATION_JSON;
        }

        // Otherwise, fall back to octet-stream
        return ContentType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Handles the serialization of the request body based on the content type.
     * It checks the serialization format from the HTTP request headers and sets the request body
     * using the appropriate serializer (XML or JSON).
     *
     * @param body The BlockStmt to which the serialization statements are added.
     * @param parameterName The name of the parameter to be serialized.
     */
    public static void handleRequestBodySerialization(BlockStmt body, String parameterName) {
        body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
        body.addStatement(StaticJavaParser.parseStatement(
            "SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());"));
        body.addStatement(StaticJavaParser.parseStatement(String.format(
            "if (xmlSerializer.supportsFormat(serializationFormat)) {"
                + "httpRequest.setBody(BinaryData.fromObject(%s, xmlSerializer));" + "} else {"
                + "httpRequest.setBody(BinaryData.fromObject(%s, jsonSerializer));" + "}",
            parameterName, parameterName)));
    }
}
