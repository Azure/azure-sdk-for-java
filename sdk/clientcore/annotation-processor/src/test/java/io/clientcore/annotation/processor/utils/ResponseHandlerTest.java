// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import io.clientcore.core.models.binarydata.BinaryData;
import com.github.javaparser.ast.stmt.BlockStmt;
import java.io.InputStream;
import java.util.List;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResponseHandlerTest {
    @Test
    public void eagerlyConsumedResponsesUseTryWithResources() {
        assertTrue(ResponseHandler.useTryWithResources(String.class));
        assertTrue(ResponseHandler.useTryWithResources(List.class));
        assertTrue(ResponseHandler.useTryWithResources(byte[].class));
        assertTrue(ResponseHandler.useTryWithResources(Void.class));
    }

    @Test
    public void streamingResponsesRemainOpen() {
        assertFalse(ResponseHandler.useTryWithResources(InputStream.class));
        assertFalse(ResponseHandler.useTryWithResources(BinaryData.class));
        assertFalse(ResponseHandler.useTryWithResources(void.class));
    }

    @Test
    public void decodesQuotedStandardBase64AndPreservesRawBytes() {
        BlockStmt body = new BlockStmt();

        ResponseHandler.handleByteArrayReturn(body, false, null);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("boolean quotedBase64"));
        assertTrue(generatedCode.contains("GeneratedCodeUtils.isJsonContentType"));
        assertTrue(generatedCode.contains("Base64.getDecoder().decode(Arrays.copyOfRange"));
        assertTrue(generatedCode.contains(": responseBytes"));
    }

    @Test
    public void textResponseUsesRawBodyString() {
        BlockStmt body = new BlockStmt();

        ResponseHandler.addSerializationFormatResponseBodyStatements(body, true);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("responseSerializationFormat == SerializationFormat.TEXT"));
        assertTrue(generatedCode.contains("responseBody.toString()"));
    }

    @Test
    public void textModelResponseUsesJsonDecoder() {
        BlockStmt body = new BlockStmt();

        ResponseHandler.addSerializationFormatResponseBodyStatements(body, false);

        assertTrue(body.toString().contains("responseSerializationFormat == SerializationFormat.TEXT"));
    }

    @Test
    public void generatesPrimitiveReturnHandling() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.setExpectedStatusCodes(new int[] { 200 });
        requestContext.setHttpMethod(HttpMethod.GET);
        BlockStmt body = new BlockStmt();

        ResponseHandler.generateResponseHandling(body, new MockTypeMirror(TypeKind.INT, "int"), requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("int deserializedResult"));
        assertTrue(generatedCode.contains("CoreUtils.createParameterizedType(int.class)"));
    }

    @Test
    public void unexpectedResponseHandlerOwnsResponseClosure() {
        HttpRequestContext requestContext = new HttpRequestContext();
        BlockStmt body = new BlockStmt();

        ResponseHandler.addExceptionHandling(body, requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("GeneratedCodeUtils.handleUnexpectedResponse"));
        assertFalse(generatedCode.contains("networkResponse.close()"));
    }

    @Test
    public void eagerResponseClosingScopeStartsAfterErrorHandling() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.setExpectedStatusCodes(new int[] { 200 });
        requestContext.setHttpMethod(HttpMethod.GET);
        BlockStmt body = new BlockStmt();

        ResponseHandler.generateResponseHandling(body,
            new io.clientcore.annotation.processor.mocks.MockDeclaredType(TypeKind.DECLARED, "java.lang.String"),
            requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.indexOf("GeneratedCodeUtils.handleUnexpectedResponse")
            < generatedCode.indexOf("networkResponseToClose = networkResponse"));
        assertTrue(generatedCode.contains("networkResponseToClose.getValue()"));
    }

    @Test
    public void eagerResponseWithoutBodyUseClosesInFinally() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.setExpectedStatusCodes(new int[] { 200 });
        requestContext.setHttpMethod(HttpMethod.GET);
        BlockStmt body = new BlockStmt();

        ResponseHandler.generateResponseHandling(body,
            new io.clientcore.annotation.processor.mocks.MockDeclaredType(TypeKind.DECLARED, "java.lang.Void"),
            requestContext);

        String generatedCode = body.toString();
        assertFalse(generatedCode.contains("networkResponseToClose = networkResponse"));
        assertTrue(generatedCode.contains("finally"));
        assertTrue(generatedCode.contains("networkResponse.close()"));
    }
}
