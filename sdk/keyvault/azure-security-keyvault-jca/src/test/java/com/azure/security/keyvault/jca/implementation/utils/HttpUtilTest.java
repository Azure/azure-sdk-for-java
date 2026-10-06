// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.*;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, HttpUtil.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + VERSION, HttpUtil.USER_AGENT_VALUE);
    }

    @Test
    @Disabled("Disable this because it will cause pipeline failure: https://dev.azure.com/azure-sdk/internal/_build/results?buildId=1196171&view=logs&j=4a83f3be-c53d-53dd-7954-86872056fb11&t=54174aae-5a55-579d-08e2-94fb446f7b77&l=29")
    public void testHttpUtilGet() {
        String url = "https://azure.com/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Disabled("This is only used to test in localhost manually")
    public void testHttpUtilGet1() {
        String url = "http://localhost:8000/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void binaryResponsePreservesBodyAndFreshnessHeaders() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
        byte[] body = new byte[] { 1, 2, 3 };
        response.setEntity(new ByteArrayEntity(body, ContentType.APPLICATION_OCTET_STREAM));
        response.addHeader("Cache-Control", "public, max-age=300");
        response.addHeader("Date", "Wed, 05 Aug 2026 10:00:00 GMT");
        response.addHeader("Age", "30");
        response.addHeader("Expires", "Wed, 05 Aug 2026 10:05:00 GMT");

        HttpUtil.BinaryHttpResponse result = HttpUtil.toBinaryResponse(response, "https://example.test/cert.crt");

        assertArrayEquals(body, result.getBody());
        assertEquals("public, max-age=300", result.getCacheControl());
        assertEquals("Wed, 05 Aug 2026 10:00:00 GMT", result.getDate());
        assertEquals("30", result.getAge());
        assertEquals("Wed, 05 Aug 2026 10:05:00 GMT", result.getExpires());
    }

    @Test
    void binaryResponseForFailureHasNoBodyOrFreshnessMetadata() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(503);
        response.addHeader("Cache-Control", "max-age=3600");

        HttpUtil.BinaryHttpResponse result = HttpUtil.toBinaryResponse(response, "https://example.test/cert.crt");

        assertNull(result.getBody());
        assertEquals("max-age=3600", result.getCacheControl());
        assertNull(result.getDate());
        assertNull(result.getAge());
        assertNull(result.getExpires());
    }

    @Test
    void binaryResponseCombinesMultipleCacheControlHeaders() throws Exception {
        BasicClassicHttpResponse response = new BasicClassicHttpResponse(200);
        response.setEntity(new ByteArrayEntity(new byte[] { 1 }, ContentType.APPLICATION_OCTET_STREAM));
        response.addHeader("Cache-Control", "public, max-age=300");
        response.addHeader("Cache-Control", "no-store");

        HttpUtil.BinaryHttpResponse result = HttpUtil.toBinaryResponse(response, "https://example.test/cert.crt");

        assertEquals("public, max-age=300, no-store", result.getCacheControl());
    }
}
