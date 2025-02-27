// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BodyContentTypeProcessorTest {
    private JavaParserTemplateProcessor processor;

    @BeforeEach
    public void setup() {
        processor = new JavaParserTemplateProcessor();
    }

    /**
     * Test for the method configureRequestWithBodyAndContentType
     */
    @Test
    public void bodyParamAnnotationPriorityOverContentTypeHeaderTest() {
        // Create a new HttpRequestContext
        HttpRequestContext context = new HttpRequestContext();
        byte[] bytes = "hello".getBytes();

        // Set the body
        // BodyParam annotation is set to "application/octet-stream"
        context.setBody(new HttpRequestContext.Body("application/octet-stream", "ByteBuffer", "request"));

        // Add headers
        // Content-Type header is set to "application/json"
        context.addHeader("Content-Type", "application/json");
        context.addHeader("Content-Length", String.valueOf((long) bytes.length));
        HttpRequestContext.Body requestBody = context.getBody();

        BlockStmt body = new BlockStmt();
        processor.configureRequestWithBodyAndContentType(body, requestBody.getParameterType(),
            requestBody.getContentType(), requestBody.getParameterName(), false);

        // Expected output
        String expectedOutput = "httpRequest.setBody(BinaryData.fromBytes(((ByteBuffer) request).array()));";

        // Actual output
        String actualOutput = body.toString();

        assertTrue(actualOutput.contains(expectedOutput));
        // Verify headers in a separate test request content type header is set to application/octet-stream

    }

    @ParameterizedTest
    @MethodSource("knownParameterTypesProvider")
    public void testConfigureRequestWithBodyAndParameterType(HttpRequestContext.Body requestBody,
        String expectedOutput) {
        BlockStmt body = new BlockStmt();
        processor.configureRequestWithBodyAndContentType(body, requestBody.getParameterType(),
            requestBody.getContentType(), requestBody.getParameterName(), false);

        // Actual output
        String actualOutput = body.toString();
        assertTrue(actualOutput.contains(expectedOutput),
            "Expected output to contain: " + expectedOutput + " Actual output: " + actualOutput);
    }

    @ParameterizedTest
    @MethodSource("knownContentTypesProvider")
    public void testConfigureRequestWithBodyAndContentType(String parameterType, String expectedContentType) {
        // Create a new HttpRequestContext
        HttpRequestContext context = new HttpRequestContext();

        // Set the body without specifying ContentType
        context.setBody(new HttpRequestContext.Body(null, parameterType, "request"));

        BlockStmt body = new BlockStmt();
        processor.configureRequestWithBodyAndContentType(body, context.getBody().getParameterType(),
            context.getBody().getContentType(), context.getBody().getParameterName(), false);

        // Expected output
        String expectedOutput = "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, " + expectedContentType;

        // Actual output
        String actualOutput = body.toString();

        assertTrue(actualOutput.contains(expectedOutput),
            "Expected output to contain: " + expectedOutput + " Actual output: " + actualOutput);
    }

    @Test
    public void contentTypeHeaderPriorityOverBodyParamAnnotationTest() {
        // Create a new HttpRequestContext
        HttpRequestContext context = new HttpRequestContext();
        byte[] bytes = "hello".getBytes();

        // Set the body
        // BodyParam annotation is set to "application/octet-stream"
        context.setBody(new HttpRequestContext.Body("application/octet-stream", "ByteBuffer", "request"));

        // Add headers
        // Content-Type header is set to "application/json"
        context.addHeader("Content-Type", "application/json");
        context.addHeader("Content-Length", String.valueOf((long) bytes.length));
        HttpRequestContext.Body requestBody = context.getBody();

        BlockStmt body = new BlockStmt();
        processor.configureRequestWithBodyAndContentType(body, requestBody.getParameterType(),
            requestBody.getContentType(), requestBody.getParameterName(), true);

        // Expected output
        String expectedOutput = "httpRequest.setBody(BinaryData.fromBytes(((ByteBuffer) request).array()));";

        // Actual output
        String actualOutput = body.toString();

        assertTrue(actualOutput.contains(expectedOutput));
        // Verify headers in a separate test request content type header is set to application/octet-stream
    }

    private static Stream<Arguments> knownContentTypesProvider() {
        return Stream.of(Arguments.of("byte[]", "\"application/octet-stream\""),
            Arguments.of("String", "\"application/octet-stream\""), Arguments.of("BinaryData", "\"application/json\""),
            Arguments.of("Object", "\"application/json\""), Arguments.of("ByteBuffer", "\"application/json\""));
    }

    private static Stream<Arguments> knownParameterTypesProvider() {
        return Stream.of(
            // scenario for isJson = true and parameterType == "ByteBuffer"
            Arguments.of(new HttpRequestContext.Body(null, "ByteBuffer", "request"),
                "httpRequest.setBody(BinaryData.fromObject(request, serializer));"),
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "BinaryData", "request"),
                "httpRequest.setBody(BinaryData.fromObject(request, serializer));"),
            Arguments.of(new HttpRequestContext.Body("application/json", "BinaryData", "request"),
                "httpRequest.setBody(BinaryData.fromObject(request, serializer));"),
            Arguments.of(new HttpRequestContext.Body("application/json", "serializable", "request"),
                "httpRequest.setBody(BinaryData.fromObject(request, serializer))"),
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "byte[]", "request"),
                "httpRequest.setBody(BinaryData.fromBytes((byte[]) request))"),
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "String", "request"),
                "httpRequest.setBody(BinaryData.fromString((String) request))"),
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "ByteBuffer", "request"),
                "httpRequest.setBody(BinaryData.fromBytes(((ByteBuffer) request).array()))"),
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "Object", "request"),
                "httpRequest.setBody(BinaryData.fromObject(request, serializer))"),
            // scenario for isJson = false and parameterType == "String"
            Arguments.of(new HttpRequestContext.Body("text/html", "String", "request"),
                "httpRequest.setBody(BinaryData.fromString((String) request));"),
            // scenario for isJson = false and parameterType == "ByteBuffer"
            Arguments.of(new HttpRequestContext.Body("text/html", "ByteBuffer", "request"),
                "httpRequest.setBody(BinaryData.fromBytes(((ByteBuffer) request).array()));"),
            // scenario for parameterType = null
            Arguments.of(new HttpRequestContext.Body("application/json", null, "request"),
                "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"),
            // scenario for parameterType == "byte[]"
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "byte[]", "request"),
                "httpRequest.setBody(BinaryData.fromBytes((byte[]) request));"),
            // Add scenario for parameterType == "String"
            Arguments.of(new HttpRequestContext.Body("application/octet-stream", "String", "request"),
                "httpRequest.setBody(BinaryData.fromString((String) request));"));
    }
}
