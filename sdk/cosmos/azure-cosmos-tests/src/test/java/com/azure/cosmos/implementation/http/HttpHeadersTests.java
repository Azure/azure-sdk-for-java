// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import org.testng.annotations.Test;

import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    public void populateLowerCaseHeadersProducesLowercaseNames() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Ms-Request-Id", "abc-123");
        headers.set("ETag", "\"v1\"");

        String[] names = new String[headers.size()];
        String[] values = new String[headers.size()];
        headers.populateLowerCaseHeaders(names, values);

        // All names should be lowercase
        for (String name : names) {
            assertThat(name).isEqualTo(name.toLowerCase(Locale.ROOT));
        }

        // Verify values are present (order depends on HashMap iteration, so use containment)
        Map<String, String> resultMap = new java.util.HashMap<>();
        for (int i = 0; i < names.length; i++) {
            resultMap.put(names[i], values[i]);
        }

        assertThat(resultMap).containsEntry("content-type", "application/json");
        assertThat(resultMap).containsEntry("x-ms-request-id", "abc-123");
        assertThat(resultMap).containsEntry("etag", "\"v1\"");
    }

    @Test(groups = "unit")
    public void populateLowerCaseHeadersWithEmptyHeaders() {
        HttpHeaders headers = new HttpHeaders();

        String[] names = new String[0];
        String[] values = new String[0];
        headers.populateLowerCaseHeaders(names, values);

        assertThat(names).isEmpty();
        assertThat(values).isEmpty();
    }

    @Test(groups = "unit")
    public void populateLowerCaseHeadersRejectsNullNames() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Key", "value");

        assertThatThrownBy(() -> headers.populateLowerCaseHeaders(null, new String[1]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("names");
    }

    @Test(groups = "unit")
    public void populateLowerCaseHeadersRejectsNullValues() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Key", "value");

        assertThatThrownBy(() -> headers.populateLowerCaseHeaders(new String[1], null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("values");
    }

    @Test(groups = "unit")
    public void populateLowerCaseHeadersRejectsTooSmallArrays() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("A", "1");
        headers.set("B", "2");

        assertThatThrownBy(() -> headers.populateLowerCaseHeaders(new String[1], new String[2]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("names");

        assertThatThrownBy(() -> headers.populateLowerCaseHeaders(new String[2], new String[1]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("values");
    }
}
