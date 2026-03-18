// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import org.testng.annotations.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpHeadersTests {

    @Test(groups = "unit")
    public void caseInsensitiveToMap() {
        String headerName = "Etag";
        String headerValue = "123";

        HttpHeaders headers = new HttpHeaders();
        headers.set(headerName, headerValue);

        Map<String, String> lowerCaseMap = headers.toLowerCaseMap();
        assertThat(lowerCaseMap.get(headerName.toLowerCase())).isEqualTo(headerValue);

        Map<String, String> caseSensitiveMap = headers.toMap();
        assertThat(caseSensitiveMap.get(headerName.toLowerCase())).isNull();
        assertThat(caseSensitiveMap.get(headerName)).isEqualTo(headerValue);
    }

    @Test(groups = "unit")
    public void keysAlreadyLowerCaseSkipsNormalization() {
        String headerName = "etag";
        String headerValue = "456";

        // Simulates HTTP/2 where header names are already lowercase
        HttpHeaders headers = new HttpHeaders(4, true);
        headers.set(headerName, headerValue);

        Map<String, String> map = headers.toMap();
        assertThat(map.get(headerName)).isEqualTo(headerValue);
    }

    @Test(groups = "unit")
    public void valueRetrievalIsCaseInsensitive() {
        String headerName = "Content-Type";
        String headerValue = "application/json";

        HttpHeaders headers = new HttpHeaders();
        headers.set(headerName, headerValue);

        // Lookup by any case should work since getHeader() lowercases the lookup key
        assertThat(headers.value("Content-Type")).isEqualTo(headerValue);
        assertThat(headers.value("content-type")).isEqualTo(headerValue);
        assertThat(headers.value("CONTENT-TYPE")).isEqualTo(headerValue);
    }
}
