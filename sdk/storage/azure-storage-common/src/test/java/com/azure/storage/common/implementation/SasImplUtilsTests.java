// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SasImplUtilsTests {

    private Map<String, String> requestHeaders;
    private Map<String, String> requestQueryParams;

    @BeforeEach
    public void setup() {
        requestHeaders = new HashMap<>();
        requestQueryParams = new HashMap<>();
    }

    @Test
    public void formatRequestHeadersNullReturnsNull() {
        assertNull(SasImplUtils.formatRequestHeaders(null, false));
    }

    @Test
    public void formatRequestHeadersEmptyReturnsNull() {
        assertNull(SasImplUtils.formatRequestHeaders(requestHeaders, false));
    }

    @Test
    public void formatRequestHeadersReturnsWithLastCharAsNewline() {
        requestHeaders.put("Some-Header", "someValue");
        String headerString = SasImplUtils.formatRequestHeaders(requestHeaders, true);

        assertNotEquals("", headerString);
        assertEquals("\n", headerString.substring(headerString.length() - 1));
    }

    @Test
    public void formatRequestHeadersForStringToSign() {
        requestHeaders.put(Constants.HeaderConstants.ENCRYPTION_KEY, "encryptionKeyValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_ENCODING, "contentEncodingValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_TYPE, "contentTypeValue");
        requestHeaders.put(Constants.HeaderConstants.CLIENT_REQUEST_ID, "clientRequestId");

        String expected
            = String.join("\n", "x-ms-encryption-key:encryptionKeyValue", "Content-Encoding:contentEncodingValue",
                "Content-Type:contentTypeValue", "x-ms-client-request-id:clientRequestId");

        String headers = SasImplUtils.formatRequestHeaders(requestHeaders, true);
        Integer newLineCount
            = Arrays.stream(headers.split("")).filter(s -> s.equals("\n")).collect(Collectors.toList()).size();

        String sortedExpected = Arrays.stream(expected.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        String sortedHeaders = Arrays.stream(headers.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        assertEquals(4, newLineCount);
        assertEquals(sortedExpected, sortedHeaders);
    }

    @Test
    public void formatRequestHeaders() {
        requestHeaders.put(Constants.HeaderConstants.ENCRYPTION_KEY, "encryptionKeyValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_ENCODING, "contentEncodingValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_TYPE, "contentTypeValue");
        requestHeaders.put(Constants.HeaderConstants.CLIENT_REQUEST_ID, "clientRequestId");

        String expected
            = String.join(",", "x-ms-encryption-key", "Content-Encoding", "Content-Type", "x-ms-client-request-id");

        String headers = SasImplUtils.formatRequestHeaders(requestHeaders, false);
        Integer expectedCount
            = Arrays.stream(headers.split("")).filter(s -> s.equals(",")).collect(Collectors.toList()).size() + 1;

        String sortedExpected = Arrays.stream(expected.split(",")).sorted().collect(Collectors.joining(","));
        String sortedHeaders = Arrays.stream(headers.split(",")).sorted().collect(Collectors.joining(","));

        assertEquals(4, expectedCount);
        assertEquals(sortedExpected, sortedHeaders);
    }

    @Test
    public void formatRequestQueryParamsNullReturnsNull() {
        assertNull(SasImplUtils.formatRequestQueryParameters(null, false));
    }

    @Test
    public void formatRequestQueryParamsEmptyReturnsNull() {
        assertNull(SasImplUtils.formatRequestQueryParameters(requestQueryParams, false));
    }

    @Test
    public void formatRequestQueryParamsReturnsWithFirstCharAsNewline() {
        requestQueryParams.put("someParam", "someValue");

        String queryParamString = SasImplUtils.formatRequestQueryParameters(requestQueryParams, true);

        assertNotEquals("", queryParamString);
        assertEquals("\n", queryParamString.substring(0, 1));
    }

    @Test
    public void formatRequestQueryParamsForStringToSign() {
        requestQueryParams.put("paramA", "valueA");
        requestQueryParams.put("paramB", "valueB");
        requestQueryParams.put("paramC", "valueC");
        String expected = "\nparamA:valueA\nparamB:valueB\nparamC:valueC";

        String queryParams = SasImplUtils.formatRequestQueryParameters(requestQueryParams, true);
        Integer newLineCount
            = Arrays.stream(queryParams.split("")).filter(s -> s.equals("\n")).collect(Collectors.toList()).size();
        String sortedExpected
            = "\n" + Arrays.stream(expected.substring(1).split("\n")).sorted().collect(Collectors.joining("\n"));
        String sortedQueryParams
            = "\n" + Arrays.stream(queryParams.substring(1).split("\n")).sorted().collect(Collectors.joining("\n"));

        assertEquals(3, newLineCount);
        assertEquals(sortedExpected, sortedQueryParams);
    }

    @Test
    public void formatRequestQueryParams() {
        requestQueryParams.put("paramA", "valueA");
        requestQueryParams.put("paramB", "valueB");
        requestQueryParams.put("paramC", "valueC");
        String expected = "paramA,paramB,paramC";

        String queryParams = SasImplUtils.formatRequestQueryParameters(requestQueryParams, false);
        Integer expectedCount
            = Arrays.stream(queryParams.split("")).filter(s -> s.equals(",")).collect(Collectors.toList()).size() + 1;
        String sortedExpected = Arrays.stream(expected.split(",")).sorted().collect(Collectors.joining(","));
        String sortedHeaders = Arrays.stream(queryParams.split(",")).sorted().collect(Collectors.joining(","));

        assertEquals(3, expectedCount);
        assertEquals(sortedExpected, sortedHeaders);
    }
}
