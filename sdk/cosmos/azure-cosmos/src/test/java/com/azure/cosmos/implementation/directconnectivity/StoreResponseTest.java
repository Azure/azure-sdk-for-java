// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static com.azure.cosmos.implementation.Utils.getUTF8BytesOrNull;
import static org.assertj.core.api.Assertions.assertThat;

public class StoreResponseTest {
    @Test(groups = { "unit" })
    public void stringContent() {
        String content = "I am body";
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("key1", "value1");
        headerMap.put("key2", "value2");

        StoreResponse sp = new StoreResponse(200, new ArrayList<>(headerMap.entrySet()), getUTF8BytesOrNull(content));

        assertThat(sp.getStatus()).isEqualTo(200);
        assertThat(sp.getResponseBody()).isEqualTo(getUTF8BytesOrNull(content));
        assertThat(sp.getHeaderValue("key1")).isEqualTo("value1");
    }
}
