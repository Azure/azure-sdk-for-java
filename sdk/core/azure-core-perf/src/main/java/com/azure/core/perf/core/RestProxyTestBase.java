// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.models.MockHttpResponse;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class RestProxyTestBase<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    public RestProxyTestBase(TOptions options) {
        super(options);
    }

    public HttpResponse createMockResponse(HttpRequest httpRequest, String contentType, byte[] bodyBytes) {
        HttpHeaders headers = new HttpHeaders().put("Content-Type", contentType);
        HttpResponse res = new MockHttpResponse(httpRequest, 200, headers, bodyBytes);
        return res;
    }

    public byte[] serializeData(Object object, ObjectMapper objectMapper) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            objectMapper.writeValue(outputStream, object);
            return outputStream.toByteArray();
        } finally {
            outputStream.close();
        }
    }
}
