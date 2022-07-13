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
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.AddHeadersFromContextPolicy;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.models.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

public abstract class RestProxyTestBase<TOptions extends CorePerfStressOptions> extends PerfStressTest<TOptions> {
    private static final AtomicInteger ID_SOURCE = new AtomicInteger();

    // An integer is good enough for perf testing. Overwriting resources between runs is ok.
    protected final String id = Integer.toString(ID_SOURCE.incrementAndGet());
    protected final String endpoint;
    protected final MyRestProxyService service;
    protected final HttpPipeline httpPipeline;

    private final WireMockServer wireMockServer;

    public RestProxyTestBase(TOptions options) {
        this(options, null);
    }

    public RestProxyTestBase(TOptions options, Function<HttpRequest, HttpResponse> mockResponseSupplier) {
        super(options);
        if (options.getBackendType() == CorePerfStressOptions.BackendType.WIREMOCK) {
            wireMockServer = createWireMockServer(mockResponseSupplier);
            endpoint = wireMockServer.baseUrl();
        } else if (options.getBackendType() == CorePerfStressOptions.BackendType.BLOBS) {
            String containerSASUrl = Configuration.getGlobalConfiguration().get("AZURE_STORAGE_CONTAINER_SAS_URL");
            if (CoreUtils.isNullOrEmpty(containerSASUrl)) {
                throw new IllegalStateException("Environment variable AZURE_STORAGE_CONTAINER_SAS_URL must be set");
            }
            wireMockServer = null;
            endpoint= containerSASUrl;
        } else {
            wireMockServer = null;
            endpoint = "http://unused";
        }
        HttpClient httpClient = createHttpClient(options, mockResponseSupplier);
        httpPipeline = new HttpPipelineBuilder()
            .policies(createPipelinePolicies(options))
            .httpClient(httpClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, httpPipeline);
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return super.cleanupAsync()
            .then(Mono.fromRunnable(() -> {
                if (wireMockServer != null) {
                    wireMockServer.shutdown();
                }
            }));
    }

    private HttpPipelinePolicy[] createPipelinePolicies(TOptions options) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (options.getBackendType() == CorePerfStressOptions.BackendType.BLOBS) {
            policies.add(new AddHeadersPolicy(
                new HttpHeaders()
                    .add("x-ms-blob-type", "BlockBlob")
                    .add("x-ms-version", "2021-08-06")));
        }

        if (options.isIncludePipelinePolicies()) {
            policies.add(new AddDatePolicy());
            policies.add(new AddHeadersFromContextPolicy());
            policies.add(new CookiePolicy());
            policies.add(new UserAgentPolicy());
            policies.add(new RetryPolicy());
            policies.add(new RequestIdPolicy());
            policies.add(new HttpLoggingPolicy(new HttpLogOptions()));
        }

        return policies.toArray(new HttpPipelinePolicy[0]);
    }

    private HttpClient createHttpClient(TOptions options, Function<HttpRequest, HttpResponse> mockResponseSupplier) {
        if (options.getBackendType() == CorePerfStressOptions.BackendType.MOCK) {
            return new MockHttpClient(mockResponseSupplier);
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

    private static WireMockServer createWireMockServer(Function<HttpRequest, HttpResponse> mockResponseSupplier) {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options()
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        if (mockResponseSupplier == null) {
            server.stubFor(any(urlPathMatching("/(RawData|UserDatabase|BinaryData).*")));
        } else {
            HttpResponse response = mockResponseSupplier.apply(null);
            server.stubFor(
                any(urlPathMatching("/(RawData|UserDatabase|BinaryData).*"))
                    .willReturn(aResponse()
                        .withBody(response.getBodyAsByteArray().block())
                        .withStatus(response.getStatusCode())
                        .withHeader("Content-Type", response.getHeaderValue("Content-Type"))
                    ));
        }

        server.start();
        return server;
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

    public static Supplier<BinaryData> createBinaryDataSupplier(CorePerfStressOptions options) {
        long size = options.getSize();
        switch (options.getBinaryDataSource()) {
            case BYTES:
                byte[] bytes = new byte[(int) size];
                new Random().nextBytes(bytes);
                return  () -> BinaryData.fromBytes(bytes);
            case FILE:
                try {
                    Path tempFile = Files.createTempFile("binarydataforperftest", null);
                    tempFile.toFile().deleteOnExit();
                    String tempFilePath = tempFile.toString();
                    TestDataCreationHelper.writeToFile(tempFilePath, size, 8192);
                    return () -> BinaryData.fromFile(tempFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case FLUX:
                return () -> BinaryData.fromFlux(
                    TestDataCreationHelper.createRandomByteBufferFlux(size), size, false).block();
            case STREAM:
                RepeatingInputStream inputStream =
                    (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(size);
                inputStream.mark(Long.MAX_VALUE);
                return () -> {
                    inputStream.reset();
                    return BinaryData.fromStream(inputStream);
                };
            default:
                throw new IllegalArgumentException("Unknown binary data source " + options.getBinaryDataSource());
        }
    }
}
