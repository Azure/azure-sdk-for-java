// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.http.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreResponseTest {
    @Test(groups = { "unit" })
    public void stringContent() {
        String content = "I am body";
        String jsonContent = "{\"id\":\"" + content + "\"}";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        StoreResponse sp = new StoreResponse(null, 200, headerMap, new ByteBufInputStream(buffer, true), buffer.readableBytes());

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBodyAsJson().get("id").asText()).isEqualTo(content);
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }

    @Test(groups = { "unit" })
    public void headerNamesAreCaseInsensitive() {
        String content = "I am body";
        String jsonContent = "{\"id\":\"" + content + "\"}";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");
        headerMap.put("KEY3", "value3");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        StoreResponse sp = new StoreResponse(null, 200, headerMap, new ByteBufInputStream(buffer, true), buffer.readableBytes());

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBodyAsJson().get("id").asText()).isEqualTo(content);
        assertThat(sp.getHeaderValue("keY1")).isEqualTo("value1");
        assertThat(sp.getHeaderValue("kEy2")).isEqualTo("value2");
        assertThat(sp.getHeaderValue("KEY3")).isEqualTo("value3");
    }

    @Test(groups = { "unit" })
    public void httpHeadersConstructorProducesSameResultAsMapConstructor() {
        String jsonContent = "{\"id\":\"test\"}";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("Content-Type", "application/json");
        headerMap.put("X-Custom-Header", "customValue");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("key1", "value1");
        httpHeaders.set("Content-Type", "application/json");
        httpHeaders.set("X-Custom-Header", "customValue");

        ByteBuf buffer1 = getUTF8BytesOrNull(jsonContent);
        StoreResponse fromMap = new StoreResponse(
            "endpoint1", 200, headerMap, new ByteBufInputStream(buffer1, true), buffer1.readableBytes());

        ByteBuf buffer2 = getUTF8BytesOrNull(jsonContent);
        StoreResponse fromHttpHeaders = new StoreResponse(
            "endpoint1", 200, httpHeaders, new ByteBufInputStream(buffer2, true), buffer2.readableBytes());

        assertThat(fromHttpHeaders.getStatus()).isEqualTo(fromMap.getStatus());
        assertThat(fromHttpHeaders.getEndpoint()).isEqualTo(fromMap.getEndpoint());

        // Verify all headers are accessible with case-insensitive lookup
        assertThat(fromHttpHeaders.getHeaderValue("key1")).isEqualTo("value1");
        assertThat(fromHttpHeaders.getHeaderValue("content-type")).isEqualTo("application/json");
        assertThat(fromHttpHeaders.getHeaderValue("x-custom-header")).isEqualTo("customValue");

        // HttpHeaders constructor stores lowercase names
        String[] headerNames = fromHttpHeaders.getResponseHeaderNames();
        for (String name : headerNames) {
            assertThat(name).isEqualTo(name.toLowerCase());
        }
    }

    @Test(groups = { "unit" })
    public void httpHeadersConstructorWithNullEndpoint() {
        String jsonContent = "{\"id\":\"test\"}";
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("key1", "value1");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        StoreResponse sp = new StoreResponse(
            null, 200, httpHeaders, new ByteBufInputStream(buffer, true), buffer.readableBytes());

        assertThat(sp.getEndpoint()).isEqualTo("");
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }

    @Test(groups = { "unit" })
    public void httpHeadersConstructorWithNoContent() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("key1", "value1");

        StoreResponse sp = new StoreResponse("endpoint", 204, httpHeaders, null, 0);

        assertThat(sp.getStatus()).isEqualTo(204);
        assertThat(sp.getResponseBodyAsJson()).isNull();
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }

    @Test(groups = { "unit" })
    public void httpHeadersConstructorDecodesOwnerFullName() {
        // OWNER_FULL_NAME value with URL-encoded segments (e.g. spaces encoded as %20)
        String encodedOwner = "dbs%2FmyDb%2Fcolls%2Fmy%20Collection";
        String expectedDecoded = "dbs/myDb/colls/my Collection";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpConstants.HttpHeaders.OWNER_FULL_NAME, encodedOwner);
        httpHeaders.set("X-Other", "plain");

        StoreResponse sp = new StoreResponse("endpoint", 200, httpHeaders, null, 0);

        // The encoded OWNER_FULL_NAME should be URL-decoded when accessed via getHeaderValue
        assertThat(sp.getHeaderValue(HttpConstants.HttpHeaders.OWNER_FULL_NAME)).isEqualTo(expectedDecoded);
        // Other headers are left as-is
        assertThat(sp.getHeaderValue("X-Other")).isEqualTo("plain");
    }
}
