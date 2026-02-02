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

public class SasImplUtilsTests {

    private static Map<String, String> requestHeaders;

    @BeforeEach
    public void setup() {
        requestHeaders = new HashMap<>();
    }

    @Test
    public void formatRequestHeadersForSasSigningNullReturnsEmptyString() {
        assertEquals("", SasImplUtils.formatRequestHeadersForSasSigning(null));
    }

    @Test
    public void formatRequestHeadersForSasSigningEmptyReturnsEmptyString() {
        assertEquals("", SasImplUtils.formatRequestHeadersForSasSigning(requestHeaders));
    }

    @Test
    public void formatRequestHeadersForSasSigningPopulatedHeaders() {
        requestHeaders.put(Constants.HeaderConstants.ENCRYPTION_KEY, "encryptionKeyValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_ENCODING, "contentEncodingValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_TYPE, "contentTypeValue");
        requestHeaders.put(Constants.HeaderConstants.CLIENT_REQUEST_ID, "clientRequestId");

        String expected
            = String.join("\n", "x-ms-encryption-key:encryptionKeyValue", "Content-Encoding:contentEncodingValue",
                "Content-Type:contentTypeValue", "x-ms-client-request-id:clientRequestId");

        String headers = SasImplUtils.formatRequestHeadersForSasSigning(requestHeaders);
        Integer newLineCount
            = Arrays.stream(headers.split("")).filter(s -> s.equals("\n")).collect(Collectors.toList()).size();

        String sortedExpected = Arrays.stream(expected.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        String sortedHeaders = Arrays.stream(headers.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        assertEquals(4, newLineCount);
        assertEquals(sortedExpected, sortedHeaders);
    }
}
