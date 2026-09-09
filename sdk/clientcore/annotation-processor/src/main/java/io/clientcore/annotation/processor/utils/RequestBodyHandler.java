// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.implementation.utils.ImplUtils;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.CoreUtils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
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
     * @param requestContext The request body context containing parameter type and content type.
     * @param processingEnv The processing environment providing utility methods for operating on program elements and types.
     * @return true if a serialization format is set and used in the request body, false otherwise.
     */
    public static boolean configureRequestBody(BlockStmt body, HttpRequestContext requestContext,
        ProcessingEnvironment processingEnv) {
        if (!requestContext.getFormParameters().isEmpty()) {
            configureFormRequestBody(body, requestContext);
            return false;
        }

        HttpRequestContext.Body requestBody = requestContext.getBody();
        if (requestBody == null) {
            return false;
        }
        TypeMirror parameterType = requestBody.getParameterType();

        if (parameterType == null) {
            // set content-length = 0
            setEmptyBody(body);
            return false;
        }
        ContentTypeInfo contentType = resolveContentType(requestContext, requestBody);

        if (parameterType.getKind().isPrimitive()) {
            return addRequestBodyStatements(body, parameterType, requestBody, processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(), contentType);
        } else {
            addRequestBodyWithNullCheck(body, parameterType, requestBody, processingEnv.getElementUtils(),
                processingEnv.getTypeUtils(), contentType);
            // serializationFormat could be set but not in scope to use for response body handling
            return false;
        }
    }

    private static ContentTypeInfo resolveContentType(HttpRequestContext requestContext,
        HttpRequestContext.Body requestBody) {
        Optional<java.util.Map.Entry<String, List<String>>> contentTypeHeader = requestContext.getHeaders()
            .entrySet()
            .stream()
            .filter(header -> "Content-Type".equalsIgnoreCase(header.getKey()))
            .findFirst();
        if (contentTypeHeader.isPresent()) {
            for (String value : contentTypeHeader.get().getValue()) {
                Optional<HttpRequestContext.MethodParameter> parameter = requestContext.getParameters()
                    .stream()
                    .filter(methodParameter -> methodParameter.getName().equals(value))
                    .findFirst();
                if (parameter.isPresent()) {
                    return new ContentTypeInfo(parameter.get(), requestBody.getContentType());
                }
            }

            if (!contentTypeHeader.get().getValue().isEmpty()) {
                return new ContentTypeInfo(null, contentTypeHeader.get().getValue().get(0));
            }
        }

        return new ContentTypeInfo(null,
            requestBody.getContentType() == null ? ContentType.APPLICATION_JSON : requestBody.getContentType());
    }

    static void configureFormRequestBody(BlockStmt body, HttpRequestContext requestContext) {
        body.tryAddImportToParentCompilationUnit(ArrayList.class);
        body.tryAddImportToParentCompilationUnit(List.class);
        String formValuesName = getAvailableVariableName(requestContext, "formDataValues");
        body.addStatement("List<String> " + formValuesName + " = new ArrayList<>();");

        for (HttpRequestContext.FormParameter formParameter : requestContext.getFormParameters()) {
            String parameterName = formParameter.getParameterName();
            String formName = UriEscapers.FORM_ESCAPER.escape(formParameter.getName());
            if (isCollectionType(formParameter.getParameterType())) {
                String itemName = getAvailableVariableName(requestContext, parameterName + "Item");
                String valueExpression = createFormValueExpression(itemName, formParameter.shouldEncode());
                String statement = "if (" + parameterName + " != null) { for (Object " + itemName + " : "
                    + parameterName + ") { if (" + itemName + " != null) { " + formValuesName + ".add(\"" + formName
                    + "=\" + " + valueExpression + "); } } }";
                body.addStatement(StaticJavaParser.parseStatement(statement));
            } else {
                String condition
                    = formParameter.getParameterType().getKind().isPrimitive() ? "true" : parameterName + " != null";
                String valueExpression = createFormValueExpression(parameterName, formParameter.shouldEncode());
                String statement = "if (" + condition + ") { " + formValuesName + ".add(\"" + formName + "=\" + "
                    + valueExpression + "); }";
                body.addStatement(StaticJavaParser.parseStatement(statement));
            }
        }

        setContentTypeHeader(body, ContentType.APPLICATION_X_WWW_FORM_URLENCODED);
        body.addStatement(StaticJavaParser
            .parseStatement("httpRequest.setBody(BinaryData.fromString(String.join(\"&\", " + formValuesName + ")));"));
    }

    private static String createFormValueExpression(String valueName, boolean shouldEncode) {
        String stringValue = "String.valueOf(" + valueName + ")";
        return shouldEncode ? "UriEscapers.FORM_ESCAPER.escape(" + stringValue + ")" : stringValue;
    }

    private static boolean isCollectionType(TypeMirror type) {
        if (!(type instanceof DeclaredType)) {
            return false;
        }

        String typeName = ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
        return Collection.class.getCanonicalName().equals(typeName)
            || List.class.getCanonicalName().equals(typeName)
            || Set.class.getCanonicalName().equals(typeName)
            || Iterable.class.getCanonicalName().equals(typeName);
    }

    private static String getAvailableVariableName(HttpRequestContext requestContext, String preferredName) {
        Set<String> parameterNames = new HashSet<>();
        requestContext.getParameters().forEach(parameter -> parameterNames.add(parameter.getName()));
        String name = preferredName;
        int suffix = 2;
        while (parameterNames.contains(name)) {
            name = preferredName + suffix++;
        }
        return name;
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
        body.addStatement(StaticJavaParser.parseStatement("if (binaryData.getLength() != null "
            + "&& httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) { "
            + "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength())); "
            + "}"));
        body.addStatement(StaticJavaParser.parseStatement("httpRequest.setBody(binaryData);"));
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
        return elementUtils.getTypeElement(name);
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
        body.addStatement(StaticJavaParser.parseStatement(
            String.format("httpRequest.setBody(BinaryData.fromBytes(%s.byteBufferToArray(%s.duplicate())));",
                ImplUtils.class.getCanonicalName(), parameterName)));
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
            "SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());"));
        body.addStatement(StaticJavaParser.parseStatement(String.format(
            "if (xmlSerializer.supportsFormat(requestSerializationFormat)) {"
                + "httpRequest.setBody(BinaryData.fromObject(%s, xmlSerializer));" + "} else {"
                + "httpRequest.setBody(BinaryData.fromObject(%s, jsonSerializer));" + "}",
            parameterName, parameterName)));
    }

    private static void addRequestBodyWithNullCheck(BlockStmt body, TypeMirror parameterType,
        HttpRequestContext.Body requestBody, Elements elementUtils, Types typeUtils, ContentTypeInfo contentType) {
        body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);
        String parameterName = requestBody.getParameterName();

        BlockStmt ifBlock = new BlockStmt();
        IfStmt ifStatement = new IfStmt(StaticJavaParser.parseExpression(parameterName + " != null"), ifBlock, null);

        addRequestBodyStatements(ifBlock, parameterType, requestBody, elementUtils, typeUtils, contentType);
        body.addStatement(ifStatement);
    }

    private static boolean addRequestBodyStatements(BlockStmt body, TypeMirror parameterType,
        HttpRequestContext.Body requestBody, Elements elementUtils, Types typeUtils, ContentTypeInfo contentType) {
        String parameterName = requestBody.getParameterName();
        if (contentType.parameter != null) {
            String contentTypeParameterName = contentType.parameter.getName();
            String paramType = contentType.parameter.getShortTypeName();
            String valueExpression;
            if ("String".equals(paramType)) {
                valueExpression = contentTypeParameterName;
            } else {
                valueExpression = "String.valueOf(" + contentTypeParameterName + ")";
            }
            if (contentType.parameter.getTypeMirror().getKind().isPrimitive()) {
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(" + "HttpHeaderName.CONTENT_TYPE, " + valueExpression + ");"));
            } else {
                body.addStatement(StaticJavaParser.parseStatement("if (" + contentTypeParameterName + " != null) { "
                    + "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, " + valueExpression + "); }"));
            }
        } else {
            setContentTypeHeader(body, contentType.fallback);
        }

        if (isBinaryDataType(parameterType, elementUtils, typeUtils)) {
            addBinaryDataRequestBody(body, parameterName);
            return false;
        }

        if (isStringType(parameterType, elementUtils, typeUtils) || isByteArray(parameterType)) {
            addTextOrBinaryRequestBody(body, parameterType, parameterName, elementUtils, typeUtils);
            return false;
        }

        if (isByteBufferType(parameterType, elementUtils, typeUtils)) {
            addByteBufferRequestBody(body, parameterName);
            return false;
        }

        handleRequestBodySerialization(body, parameterName);
        return true;
    }

    private static void addTextOrBinaryRequestBody(BlockStmt body, TypeMirror parameterType, String parameterName,
        Elements elementUtils, Types typeUtils) {
        body.tryAddImportToParentCompilationUnit(SerializationFormat.class);
        BlockStmt jsonBody = new BlockStmt().addStatement(StaticJavaParser
            .parseStatement("httpRequest.setBody(BinaryData.fromObject(" + parameterName + ", jsonSerializer));"));
        BlockStmt rawBody = new BlockStmt();
        if (isByteArray(parameterType)) {
            addByteArrayRequestBody(rawBody, parameterName);
        } else if (isStringType(parameterType, elementUtils, typeUtils)) {
            addStringRequestBody(rawBody, parameterName);
        }
        body.addStatement(new IfStmt(
            StaticJavaParser.parseExpression(
                "io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())"),
            jsonBody, rawBody));
    }

    private static final class ContentTypeInfo {
        private final HttpRequestContext.MethodParameter parameter;
        private final String fallback;

        private ContentTypeInfo(HttpRequestContext.MethodParameter parameter, String fallback) {
            this.parameter = parameter;
            this.fallback = fallback == null ? ContentType.APPLICATION_JSON : fallback;
        }
    }

    private RequestBodyHandler() {
    }
}
