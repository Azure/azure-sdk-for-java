// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the request formation in codegen
 */
public class HttpRequestInitializerTest {

    @ParameterizedTest
    @CsvSource({
        "GET, \"/my/uri/path\", key1, value1, key2, value2",
        "POST, \"/my/uri/path2\", key3, value3, key4, value4" })
    public void testInitializeHttpRequestWithParameterizedQueryParams(String httpMethod, String host, String queryKey1,
        String queryValue1, String queryKey2, String queryValue2) {
        com.github.javaparser.ast.stmt.BlockStmt body = new com.github.javaparser.ast.stmt.BlockStmt(); // Directly using the BlockStmt class
        HttpRequestContext method = new HttpRequestContext(); // Create a new instance of HttpRequestContext
        JavaParserTemplateProcessor processor = new JavaParserTemplateProcessor();

        // Arrange: Set up method with query params and various HTTP methods
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(queryKey1, queryValue1);
        queryParams.put(queryKey2, queryValue2);

        method.setHost(host);
        method.setHttpMethod(HttpMethod.valueOf(httpMethod));
        method.addQueryParam(queryKey1, queryValue1);
        method.addQueryParam(queryKey2, queryValue2);

        // Act: Call the method
        processor.initializeHttpRequest(body, method);

        // Assert: Check if URI and query parameters are set properly
        String normalizedBody = body.toString().replaceAll("\\s+", " ").trim();
        String expectedHostStatement = "String host = " + host;
        assertTrue(normalizedBody.contains(expectedHostStatement));

        String expectedQueryStatement1 = "if (" + queryValue1 + " != null) { host = CoreUtils.appendQueryParam(host, \""
            + queryKey1 + "\", " + queryValue1 + "); }";
        String expectedQueryStatement2 = "if (" + queryValue2 + " != null) { host = CoreUtils.appendQueryParam(host, \""
            + queryKey2 + "\", " + queryValue2 + "); }";

        assertTrue(normalizedBody.contains(expectedQueryStatement1));
        assertTrue(normalizedBody.contains(expectedQueryStatement2));

        String expectedHttpRequestStatement
            = "HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod." + httpMethod + ").setUri(host);";
        assertTrue(normalizedBody.contains(expectedHttpRequestStatement));
    }
}
