package com.azure.storage.common.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;;

public class SasImplUtilsTests {

    private Map<String, String> requestHeaders;

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
    public void formatRequestHeadersForSasSigningReturnsWithLastCharAsNewline() {
        requestHeaders.put("Some-Header", "someValue");
        String headerString = SasImplUtils.formatRequestHeadersForSasSigning(requestHeaders);

        assertNotEquals("", headerString);
        assertEquals("\n", headerString.substring(headerString.length() - 1));
    }

    @Test
    public void formatRequestHeadersForSasSigningPopulatedHeaders() {
        requestHeaders.put(Constants.HeaderConstants.ENCRYPTION_KEY, "encryptionKeyValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_ENCODING, "contentEncodingValue");
        requestHeaders.put(Constants.HeaderConstants.CONTENT_TYPE, "contentTypeValue");
        requestHeaders.put(Constants.HeaderConstants.CLIENT_REQUEST_ID, "clientRequestId");

        String expected = "x-ms-encryption-key:encryptionKeyValue\n" + "Content-Encoding:contentEncodingValue\n"
            + "Content-Type:contentTypeValue\n" + "x-ms-client-request-id:clientRequestId\n";

        String headers = SasImplUtils.formatRequestHeadersForSasSigning(requestHeaders);
        Integer newLineCount
            = Arrays.stream(headers.split("")).filter(s -> s.equals("\n")).collect(Collectors.toList()).size();

        String sortedExpected = Arrays.stream(expected.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        String sortedHeaders = Arrays.stream(headers.split("\n")).sorted().collect(Collectors.joining("\n")) + "\n";

        assertEquals(4, newLineCount);
        assertEquals(sortedExpected, sortedHeaders);
    }
}
