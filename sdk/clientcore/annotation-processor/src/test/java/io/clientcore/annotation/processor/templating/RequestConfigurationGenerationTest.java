// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.ast.stmt.BlockStmt;
import io.clientcore.annotation.processor.mocks.MockDeclaredType;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestConfigurationGenerationTest {
    @Test
    public void usesRequestConfigurationParameterNames() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.addParameter(parameter("options", "io.clientcore.core.http.models.RequestContext"));
        requestContext.addParameter(parameter("listener", "io.clientcore.core.http.models.ServerSentEventListener"));
        BlockStmt body = new BlockStmt();

        new JavaParserTemplateProcessor().addRequestConfigurationToRequest(body, requestContext);

        String generatedCode = body.toString();
        assertTrue(generatedCode.contains("httpRequest.setContext(options)"));
        assertTrue(generatedCode.contains("httpRequest.setServerSentEventListener(listener)"));
    }

    @Test
    public void serializesNonExplodedCollectionQueryAsSingleValue() {
        HttpRequestContext requestContext = new HttpRequestContext();
        requestContext.setHost("\"https://example.test\"");
        requestContext.setTemplateHasHost(true);
        requestContext.addQueryParam("colors", "colors", false, true, false);
        requestContext.addParameter(parameter("colors", "java.util.List"));
        BlockStmt body = new BlockStmt();

        new JavaParserTemplateProcessor().setHttpRequestUri(body,
            com.github.javaparser.StaticJavaParser.parseExpression("new HttpRequest().setMethod(HttpMethod.GET)"),
            requestContext);

        assertTrue(body.toString().contains("BinaryData.fromObject(colors, jsonSerializer).toString()"));
    }

    private static HttpRequestContext.MethodParameter parameter(String name, String type) {
        return new HttpRequestContext.MethodParameter(new MockDeclaredType(TypeKind.DECLARED, type), type, name, null);
    }
}
