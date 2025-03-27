// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.clientcore.annotation.processor.mocks.MockTypeMirror;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import javax.lang.model.type.TypeKind;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verify the formation of the HTTP request URL in the code generation.
 */
public class HttpRequestInitializerTest {

    @ParameterizedTest
    @CsvSource({
        "GET, \"/my/uri/path\", key1, value1, key2, value2",
        "POST, \"/my/uri/path2\", key3, value3, key4, value4" })
    public void testInitializeHttpRequestWithParameterizedQueryParams(String httpMethod, String url, String queryKey1,
        String queryValue1, String queryKey2, String queryValue2) {

        com.github.javaparser.ast.stmt.BlockStmt body = new com.github.javaparser.ast.stmt.BlockStmt();
        HttpRequestContext method = new HttpRequestContext();
        JavaParserTemplateProcessor processor = new JavaParserTemplateProcessor();

        // Arrange: Set up method with query params
        method.setHost(url);
        method.setHttpMethod(HttpMethod.valueOf(httpMethod));
        method.addQueryParam(queryKey1, queryValue1, false, true);
        method.addParameter(new HttpRequestContext.MethodParameter(new MockTypeMirror(TypeKind.DECLARED, "String"),
            "String", "value1"));
        method.addQueryParam(queryKey2, queryValue2, true, false);
        method.addHeader("Content-Type", "application/json");
        method.addHeader("Content-Length", String.valueOf(0));

        // Act: Call the method
        processor.initializeHttpRequest(body, method);

        // Parse the generated code
        CompilationUnit generatedCode
            = StaticJavaParser.parse("public class TestClass { public void testMethod() " + body + " }");
        String expectedQuery1Statement;
        if ("POST".equals(httpMethod)) {
            expectedQuery1Statement = "queryParamMap.put(\"key3\", value3);\n";
        } else {
            expectedQuery1Statement
                = "queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape(\"key1\"), UriEscapers.QUERY_ESCAPER.escape"
                    + "(value1));\r\n";
        }
        String expectedCode = "public class TestClass { public void testMethod() {" + "String url = " + url + ";\n"
            + "// Append non-null query parameters\n" + "String newUrl;\n"
            + "LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();\n" + expectedQuery1Statement
            + "queryParamMap.put(\"" + queryKey2 + "\", " + queryValue2 + ");\n"
            + "newUrl = CoreUtils.appendQueryParams(url, queryParamMap);\n" + "if (newUrl != null) {\n"
            + "url = newUrl;\n" + "}\n" + "// Create the HTTP request\n"
            + "HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod." + httpMethod + ").setUri(url);\n"
            + "httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(0)).add(HttpHeaderName"
            + ".CONTENT_TYPE, String.valueOf(application/ json));" + "} }";
        CompilationUnit expectedCompilationUnit = StaticJavaParser.parse(expectedCode);

        // Assert: Compare the generated code with the expected code
        assertEquals(expectedCompilationUnit, generatedCode);
    }
}
