// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.models.MockHttpResponse;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class RestProxyTestBase<TOptions extends CorePerfStressOptions> extends PerfStressTest<TOptions> {

    protected final String endpoint;
    protected final MyRestProxyService service;

    public RestProxyTestBase(TOptions options, HttpClient mockHttpClient) {
        super(options);
        endpoint = Objects.requireNonNull(options.getEndpoint(), "endpoint must not be null");
        HttpClient httpClient = createHttpClient(options,
            Objects.requireNonNull(mockHttpClient, "mockHttpClient must not be null"));
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(createPipelinePolicies(options))
            .httpClient(httpClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, pipeline);
    }

    private HttpPipelinePolicy[] createPipelinePolicies(TOptions options) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (options.getBackendType() == CorePerfStressOptions.BackendType.BLOBS) {
            policies.add(new StorageHeadersInjectionPolicy());
        }
        return policies.toArray(new HttpPipelinePolicy[0]);
    }

    private HttpClient createHttpClient(TOptions options, HttpClient mockHttpClient) {
        if (options.getBackendType() == CorePerfStressOptions.BackendType.MOCK) {
            return mockHttpClient;
        } else {
            switch (options.getHttpClient()) {
                case NETTY:
                    return new NettyAsyncHttpClientBuilder().build();
                case OKHTTP:
                    return new OkHttpAsyncHttpClientBuilder().build();
                default:
                    throw new IllegalArgumentException("Unsupported http client " + options.getHttpClient());
            }
        }
    }

    public static HttpResponse createMockResponse(HttpRequest httpRequest, String contentType, byte[] bodyBytes) {
        HttpHeaders headers = new HttpHeaders().put("Content-Type", contentType);
        HttpResponse res = new MockHttpResponse(httpRequest, 200, headers, bodyBytes);
        return res;
    }

    public static byte[] serializeData(Object object, ObjectMapper objectMapper) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            objectMapper.writeValue(outputStream, object);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
