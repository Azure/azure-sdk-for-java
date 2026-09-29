// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestBodyHandlerTest {
    @Test
    public void binaryDataBodyIsSetOutsideLengthCheck() {
        BlockStmt body = new BlockStmt();

        RequestBodyHandler.addBinaryDataRequestBody(body, "content");

        String generatedCode = body.toString();
        assertEquals(3, body.getStatements().size());
        assertEquals("httpRequest.setBody(binaryData);", body.getStatement(2).toString());
        assertTrue(generatedCode.contains("get(HttpHeaderName.CONTENT_LENGTH) == null"));
    }

    @Test
    public void byteBufferConversionUsesRemainingViewWithoutMutatingInput() {
        BlockStmt body = new BlockStmt();

        RequestBodyHandler.addByteBufferRequestBody(body, "content");

        assertTrue(body.toString().contains("ImplUtils.byteBufferToArray(content.duplicate())"));
    }

    @Test
    public void configuresFormUrlEncodedBody() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.addFormParameter(new HttpRequestContext.FormParameter("display name",
            new MockTypeMirror(TypeKind.DECLARED, "java.lang.String"), "displayName", true));
        BlockStmt body = new BlockStmt();

        RequestBodyHandler.configureFormRequestBody(body, requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("display+name="));
        assertTrue(generatedCode.contains("UriEscapers.FORM_ESCAPER.escape(String.valueOf(displayName))"));
        assertTrue(generatedCode.contains("application/x-www-form-urlencoded"));
        assertTrue(generatedCode.contains("String.join(\"&\", formDataValues)"));
    }
}
