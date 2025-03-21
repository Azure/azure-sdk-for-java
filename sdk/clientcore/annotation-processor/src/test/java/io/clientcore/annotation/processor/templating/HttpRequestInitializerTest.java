// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.core.http.models.HttpMethod;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        method.addQueryParam(queryKey1, queryValue1, false);
        method.addQueryParam(queryKey2, queryValue2, true);
        method.addHeader("Content-Type", "application/json");
        method.addHeader("Content-Length", String.valueOf(0));

        // Act: Call the method
        processor.initializeHttpRequest(body, method);

        // Assert: Check if the generated code matches expectations
        String normalizedBody = body.toString().replaceAll("\\s+", " ").trim();

        // Ensure URL initialization is present
        String expectedUrlStatement = "String url = " + url + ";";
        assertTrue(normalizedBody.contains(expectedUrlStatement));

        // Ensure newUrl is declared only once
        assertTrue(normalizedBody.contains("String newUrl;"));

        // Ensure each query parameter is appended correctly
        String expectedQueryStatement = "HashMap<String, Object> queryParamMap = new HashMap<>(); "
            + "queryParamMap.put(\"" + queryKey1 + "\", " + queryValue1 + "); "
            + "queryParamMap.put(\"" + queryKey2 + "\", " + queryValue2 + "); "
            + "newUrl = CoreUtils.appendQueryParam(url, queryParamMap, ',');";
        assertTrue(normalizedBody.contains(expectedQueryStatement));

        // Ensure the final HttpRequest construction is correct
        String expectedHttpRequestStatement
            = "HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod." + httpMethod + ").setUri(url);";
        assertTrue(normalizedBody.contains(expectedHttpRequestStatement));

        String expectedHttpRequestHeaderStatement
            = "httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(0)).add(HttpHeaderName.CONTENT_TYPE, String.valueOf(application / json));";
        assertTrue(normalizedBody.contains(expectedHttpRequestHeaderStatement));
    }

}
