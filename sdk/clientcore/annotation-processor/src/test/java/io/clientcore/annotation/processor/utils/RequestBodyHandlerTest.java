// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockElements;
import io.clientcore.annotation.processor.mocks.MockFiler;
import io.clientcore.annotation.processor.mocks.MockJavaFileObject;
import io.clientcore.annotation.processor.mocks.MockProcessingEnvironment;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import io.clientcore.annotation.processor.mocks.MockTypes;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.implementation.http.ContentType;
import java.util.stream.Stream;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RequestBodyHandler}
 */
public class RequestBodyHandlerTest {

    private BlockStmt body;
    private ProcessingEnvironment processingEnv;
    private Elements elementUtils;
    private Types typeUtils;

    @BeforeEach
    void setUp() {
        body = new BlockStmt();
        JavaFileObject filerSourceFile = new MockJavaFileObject();
        Filer filer = new MockFiler(filerSourceFile);
        elementUtils = new MockElements();
        typeUtils = new MockTypes();
        processingEnv = new MockProcessingEnvironment(filer, elementUtils, typeUtils);
    }

    static Stream<Arguments> provideTestData() {
        return Stream.of(
            // Test case 1: Null parameter type, should set an empty body
            Arguments.of(null, "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");", false),
            // Test case 2:  Add JSON request body for String type
            Arguments.of(new MockTypeMirror(TypeKind.DECLARED, "java.lang.String"), "BinaryData.fromString", false),
            // Test case 3: Add BinaryData request body for BinaryData type
            Arguments.of(new MockTypeMirror(TypeKind.DECLARED, "io.clientcore.core.models.binarydata.BinaryData"),
                "binaryData);", false),
            // Test case 4: Add ByteArray request body for ByteBuffer type
            Arguments.of(new MockTypeMirror(TypeKind.DECLARED, "java.nio.ByteBuffer"), "BinaryData.fromBytes", false),
            // Test case 5: Add ByteArray request body for byte[] type
            Arguments.of(new MockTypeMirror(TypeKind.ARRAY, "byte"), "BinaryData.fromBytes", false),
            // Test case 6: Add serialization request body for a non-specific type
            Arguments.of(new MockTypeMirror(TypeKind.DECLARED, "CustomType"),
                "SerializationFormat "
                    + "serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());",
                false),
            Arguments.of(new MockTypeMirror(TypeKind.INT, "CustomType"),
                "SerializationFormat "
                    + "serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());",
                true));
    }

    @ParameterizedTest
    @MethodSource("provideTestData")
    void configureBodyWithContentTypeTest(TypeMirror parameterType, String expectedStatement,
        boolean expectedSerializationFormatSet) {
        HttpRequestContext.Body requestBody
            = new HttpRequestContext.Body(ContentType.APPLICATION_OCTET_STREAM, parameterType, "requestBody");

        // Create BlockStmt body and processingEnv as needed
        boolean isSerializationFormatSet = RequestBodyHandler.configureRequestBody(body, requestBody, processingEnv);

        // Validate the result
        assertEquals(expectedSerializationFormatSet, isSerializationFormatSet);
        assertTrue(body.getStatements()
            .stream()
            .anyMatch(stmt -> containsStringNormalized(stmt.toString(), expectedStatement)));
    }

    // Helper method to match strings
    private boolean containsStringNormalized(String actual, String expected) {
        if (actual == null || expected == null) {
            return false;
        }
        // Normalize both strings by removing extra spaces and compare
        String normalizedTarget = actual.replaceAll("\\s+", " ").trim();
        String normalizedSearchString = expected.replaceAll("\\s+", " ").trim();
        return normalizedTarget.contains(normalizedSearchString);
    }
}
