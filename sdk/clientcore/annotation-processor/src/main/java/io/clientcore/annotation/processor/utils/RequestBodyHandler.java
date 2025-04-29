// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.CoreUtils;
import java.nio.ByteBuffer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Utility class for handling request bodies in HTTP requests.
 */
public final class RequestBodyHandler {

    /**
     * Configures the request with the body content and content type.
     * Determines the content type if not explicitly set, and adds the appropriate request body statements.
     *
     * @param body The BlockStmt to which the statements are added.
     * @param requestBody The request body context containing parameter type and content type.
     * @param processingEnv The processing environment providing utility methods for operating on program elements and types.
     * @return true if a serialization format is set and used in the request body, false otherwise.
     */
    public static boolean configureRequestBody(BlockStmt body, HttpRequestContext.Body requestBody,
        ProcessingEnvironment processingEnv) {
        if (requestBody == null) {
            return false;
        }
        TypeMirror parameterType = requestBody.getParameterType();

        if (parameterType == null) {
            // set content-length = 0
            setEmptyBody(body);
            return false;
        }

        if (parameterType.getKind().isPrimitive()) {
            return addRequestBodyStatements(body, parameterType, requestBody.getParameterName(),
                processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        } else {
            addRequestBodyWithNullCheck(body, parameterType, requestBody.getParameterName(),
                processingEnv.getElementUtils(), processingEnv.getTypeUtils());
            // serializationFormat could be set but not in scope to use for response body handling
            return false;
        }
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
        TypeElement stringElement = getTypeElement(elementUtils, "java.lang.String");
        if (stringElement == null) {
            return false;
        }

        return typeUtils.isSameType(parameterType, stringElement.asType());
    }

    private static TypeElement getTypeElement(Elements elementUtils, String name) {
        TypeElement element = elementUtils.getTypeElement(name);
        if (element == null) {
            return null;
        }
        return element;
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
        TypeElement byteBufferElement = getTypeElement(elementUtils, "java.nio.ByteBuffer");
        if (byteBufferElement == null) {
            return false;
        }
        return typeUtils.isSameType(parameterType, byteBufferElement.asType());
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
    private static void setEmptyBody(BlockStmt body) {
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

    private static void addRequestBodyWithNullCheck(BlockStmt body, TypeMirror parameterType, String parameterName,
        Elements elementUtils, Types typeUtils) {
        body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);

        BlockStmt ifBlock = new BlockStmt();
        IfStmt ifStatement = new IfStmt(StaticJavaParser.parseExpression(parameterName + " != null"), ifBlock, null);

        addRequestBodyStatements(ifBlock, parameterType, parameterName, elementUtils, typeUtils);
        body.addStatement(ifStatement);
    }

    private static boolean addRequestBodyStatements(BlockStmt body, TypeMirror parameterType, String parameterName,
        Elements elementUtils, Types typeUtils) {
        if (RequestBodyHandler.isBinaryDataType(parameterType, elementUtils, typeUtils)) {
            RequestBodyHandler.addBinaryDataRequestBody(body, parameterName);
        } else if (RequestBodyHandler.isByteArray(parameterType)) {
            RequestBodyHandler.addByteArrayRequestBody(body, parameterName);
        } else if (RequestBodyHandler.isStringType(parameterType, elementUtils, typeUtils)) {
            RequestBodyHandler.addStringRequestBody(body, parameterName);
        } else if (RequestBodyHandler.isByteBufferType(parameterType, elementUtils, typeUtils)) {
            RequestBodyHandler.addByteBufferRequestBody(body, parameterName);
        } else {
            RequestBodyHandler.handleRequestBodySerialization(body, parameterName);
            // return true as this is the only place where serialization statements are set
            return true;
        }
        return false;
    }

    private RequestBodyHandler() {
    }
}
