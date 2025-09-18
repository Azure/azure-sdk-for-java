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
}
