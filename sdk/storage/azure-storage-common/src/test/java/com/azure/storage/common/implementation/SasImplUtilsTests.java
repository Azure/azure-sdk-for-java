package com.azure.storage.common.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;;

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

        String expected = "x-ms-encryption-key:encryptionKeyValue\n"
            + "Content-Encoding:contentEncodingValue\n"
            + "Content-Type:contentTypeValue\n"
            + "x-ms-client-request-id:clientRequestId\n";

        assertEquals(expected, SasImplUtils.formatRequestHeadersForSasSigning(requestHeaders));
    }

}
