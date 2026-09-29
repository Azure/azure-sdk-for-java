// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeaderCollectionGenerationTest {
    @Test
    public void expandsMapIntoPrefixedHeaders() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.addHeader("x-ms-meta-", "metadata");
        requestContext.addParameter(new HttpRequestContext.MethodParameter(new MockDeclaredType(TypeKind.DECLARED,
            "java.util.Map", new MockDeclaredType(TypeKind.DECLARED, "java.lang.String"),
            new MockDeclaredType(TypeKind.DECLARED, "java.lang.String")), "Map<String, String>", "metadata", null));
        BlockStmt body = new BlockStmt();

        new JavaParserTemplateProcessor().addHeadersToRequest(body, requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("for (Map.Entry<?, ?> metadataHeaderEntry : metadata.entrySet())"));
        assertTrue(generatedCode.contains("HttpHeaderName.fromString(\"x-ms-meta-\" + metadataHeaderEntry.getKey())"));
        assertTrue(generatedCode.contains("String.valueOf(metadataHeaderEntry.getValue())"));
    }
}
