// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class StoreResponseTest {
    @Test(groups = { "unit" })
    public void stringContent() {
        String content = "I am body";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");

        StoreResponse sp = new StoreResponse(200, new ArrayList<>(headerMap.entrySet()), content);

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseStream()).isNull();
        assertThat(sp.getResponseBody()).isEqualTo(content);
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }

    @Test(groups = { "unit" })
    public void streamContent() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[] { 3, 0, 1, 9, -1, 125 });
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");

        StoreResponse sp = new StoreResponse(200, new ArrayList<>(headerMap.entrySet()), new ByteArrayInputStream(baos.toByteArray()));

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBody()).isNull();
        assertThat(sp.getResponseStream()).isNotNull();
        assertThat(IOUtils.contentEquals(new ByteArrayInputStream(baos.toByteArray()), sp.getResponseStream()));
    }
}
