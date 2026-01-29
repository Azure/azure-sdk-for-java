// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.testng.annotations.Test;

import java.util.HashMap;

import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreResponseTest {
    @Test(groups = { "unit" })
    public void stringContent() throws Exception {
        String content = "I am body";
        String jsonContent = "{\"id\":\"" + content + "\"}";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        StoreResponse sp = new StoreResponse(null, 200, headerMap, new ByteBufInputStream(buffer, true), buffer.readableBytes(), null);

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBodyAsJson().get("id").asText()).isEqualTo(content);
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }

    @Test(groups = { "unit" })
    public void headerNamesAreCaseInsensitive() throws Exception {
        String content = "I am body";
        String jsonContent = "{\"id\":\"" + content + "\"}";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");
        headerMap.put("KEY3", "value3");

        ByteBuf buffer = getUTF8BytesOrNull(jsonContent);
        StoreResponse sp = new StoreResponse(null, 200, headerMap, new ByteBufInputStream(buffer, true), buffer.readableBytes(), null);

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBodyAsJson().get("id").asText()).isEqualTo(content);
        assertThat(sp.getHeaderValue("keY1")).isEqualTo("value1");
        assertThat(sp.getHeaderValue("kEy2")).isEqualTo("value2");
        assertThat(sp.getHeaderValue("KEY3")).isEqualTo("value3");
    }
}
