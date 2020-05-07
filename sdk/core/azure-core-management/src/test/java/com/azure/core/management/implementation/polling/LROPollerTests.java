// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.Resource;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class LROPollerTests {
    private static final SerializerAdapter SERIALIZER = new AzureJacksonAdapter();

    private static final Duration POLLING_DURATION = Duration.ofMillis(100);

    @BeforeEach
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void afterTest() {
        Mockito.framework().clearInlineMocks();
    }

    @Host("http://localhost")
    @ServiceInterface(name = "ProvisioningStateLroService")
    interface ProvisioningStateLroServiceClient {
        @Put("/resource/1")
        Mono<Response<Flux<ByteBuffer>>> startLro();
    }

    @Test
    public void lroBasedOnProvisioningState() {
        final String resourceEndpoint = "/resource/1";
        ResponseTransformer provisioningStateLroService = new ResponseTransformer() {
            private int[] getCallCount = new int[1];

            @Override
            public com.github.tomakehurst.wiremock.http.Response transform(Request request,
                                                                           com.github.tomakehurst.wiremock.http.Response response,
                                                                           FileSource fileSource,
                                                                           Parameters parameters) {

                if (!request.getUrl().endsWith(resourceEndpoint)) {
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .status(500)
                        .body("Unsupported path:" + request.getUrl())
                        .build();
                }
                if (request.getMethod().isOneOf(RequestMethod.PUT)) {
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .body(toJson(new FooWithProvisioningState("IN_PROGRESS")))
                        .build();
                }
                if (request.getMethod().isOneOf(RequestMethod.GET)) {
                    getCallCount[0]++;
                    if (getCallCount[0] == 1) {
                        return new com.github.tomakehurst.wiremock.http.Response.Builder()
                            .body(toJson(new FooWithProvisioningState("IN_PROGRESS")))
                            .build();
                    } else if (getCallCount[0] == 2) {
                        return new com.github.tomakehurst.wiremock.http.Response.Builder()
                            .body(toJson(new FooWithProvisioningState("SUCCEEDED", UUID.randomUUID().toString())))
                            .build();
                    }
                }
                return response;
            }

            @Override
            public String getName() {
                return "LroService";
            }
        };

        WireMockServer lroServer = createServer(provisioningStateLroService, resourceEndpoint);
        lroServer.start();

        try {
            final ProvisioningStateLroServiceClient client = RestProxy.create(ProvisioningStateLroServiceClient.class,
                createHttpPipeline(lroServer.port()),
                SERIALIZER);

            Function<PollingContext<PollResult<FooWithProvisioningState>>, Mono<PollResult<FooWithProvisioningState>>>
                lroInitFunction = newLroInitFunction(client, FooWithProvisioningState.class);

            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = PollerFactory.create(SERIALIZER,
                new HttpPipelineBuilder().build(),
                FooWithProvisioningState.class,
                FooWithProvisioningState.class,
                POLLING_DURATION,
                lroInitFunction);

            int[] onNextCallCount = new int[1];
            lroFlux.doOnNext(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                Assertions.assertNotNull(pollResult);
                Assertions.assertNotNull(pollResult.value());
                onNextCallCount[0]++;
                if (onNextCallCount[0] == 1) {
                    Assertions.assertEquals(response.getStatus(),
                        LongRunningOperationStatus.IN_PROGRESS);
                    Assertions.assertNull(pollResult.value().getResourceId());
                } else if (onNextCallCount[0] == 2) {
                    Assertions.assertEquals(response.getStatus(),
                        LongRunningOperationStatus.IN_PROGRESS);
                    Assertions.assertNull(pollResult.value().getResourceId());
                } else if (onNextCallCount[0] == 3) {
                    Assertions.assertEquals(response.getStatus(),
                        LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
                    Assertions.assertNotNull(pollResult.value().getResourceId());
                } else {
                    throw new IllegalStateException("Poller emitted more than expected value.");
                }
            }).blockLast();
        } finally {
            if (lroServer.isRunning()) {
                lroServer.shutdown();
            }
        }
    }

    @Test
    public void lroSucceededNoPoll() {
        final String resourceEndpoint = "/resource/1";
        final String sampleVaultUpdateSucceededResponse = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu\",\"name\":\"v1weidxu\",\"type\":\"Microsoft.KeyVault/vaults\",\"location\":\"centralus\",\"tags\":{},\"properties\":{\"sku\":{\"family\":\"A\",\"name\":\"standard\"},\"tenantId\":\"###\",\"accessPolicies\":[],\"enabledForDeployment\":false,\"vaultUri\":\"https://v1weidxu.vault.azure.net/\",\"provisioningState\":\"Succeeded\"}}";
        ResponseTransformer provisioningStateLroService = new ResponseTransformer() {
            @Override
            public com.github.tomakehurst.wiremock.http.Response transform(Request request,
                                                                           com.github.tomakehurst.wiremock.http.Response response,
                                                                           FileSource fileSource,
                                                                           Parameters parameters) {

                if (!request.getUrl().endsWith(resourceEndpoint)) {
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .status(500)
                        .body("Unsupported path:" + request.getUrl())
                        .build();
                }
                if (request.getMethod().isOneOf(RequestMethod.PUT)) {
                    // 200 response with provisioningState=Succeeded.
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .status(200)
                        .body(sampleVaultUpdateSucceededResponse)
                        .build();
                }
                return response;
            }

            @Override
            public String getName() {
                return "LroService";
            }
        };

        WireMockServer lroServer = createServer(provisioningStateLroService, resourceEndpoint);
        lroServer.start();

        try {
            final ProvisioningStateLroServiceClient client = RestProxy.create(ProvisioningStateLroServiceClient.class,
                createHttpPipeline(lroServer.port()),
                SERIALIZER);

            Function<PollingContext<PollResult<Resource>>, Mono<PollResult<Resource>>>
                lroInitFunction = newLroInitFunction(client, Resource.class);

            PollerFlux<PollResult<Resource>, Resource> lroFlux
                = PollerFactory.create(SERIALIZER,
                new HttpPipelineBuilder().build(),
                Resource.class,
                Resource.class,
                POLLING_DURATION,
                lroInitFunction);

            AsyncPollResponse<PollResult<Resource>, Resource> asyncPollResponse = lroFlux.doOnNext(response -> {
                PollResult<Resource> pollResult = response.getValue();
                Assertions.assertNotNull(pollResult);
                Assertions.assertNotNull(pollResult.value());
                Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                Assertions.assertNotNull(pollResult.value().id());
            }).blockLast();
            Assertions.assertNotNull(asyncPollResponse);

            Resource result = asyncPollResponse.getFinalResult().block();
            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.id());
            Assertions.assertEquals("v1weidxu", result.name());
            Assertions.assertEquals("Microsoft.KeyVault/vaults", result.type());
        } finally {
            if (lroServer.isRunning()) {
                lroServer.shutdown();
            }
        }
    }

    @Test
    public void lroTimeout() {
        final Duration timeoutDuration = Duration.ofMillis(1000);   // use a large timeout for manual verification

        final String resourceEndpoint = "/resource/1";
        final AtomicInteger getCallCount = new AtomicInteger(0);
        ResponseTransformer provisioningStateLroService = new ResponseTransformer() {
            @Override
            public com.github.tomakehurst.wiremock.http.Response transform(Request request,
                                                                           com.github.tomakehurst.wiremock.http.Response response,
                                                                           FileSource fileSource,
                                                                           Parameters parameters) {

                if (!request.getUrl().endsWith(resourceEndpoint)) {
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .status(500)
                        .body("Unsupported path:" + request.getUrl())
                        .build();
                }
                if (request.getMethod().isOneOf(RequestMethod.PUT, RequestMethod.GET)) {
                    if (request.getMethod().isOneOf(RequestMethod.GET)) {
                        getCallCount.getAndIncrement();
                    }
                    return new com.github.tomakehurst.wiremock.http.Response.Builder()
                        .body(toJson(new FooWithProvisioningState("IN_PROGRESS")))
                        .build();
                }
                return response;
            }

            @Override
            public String getName() {
                return "LroService";
            }
        };

        WireMockServer lroServer = createServer(provisioningStateLroService, resourceEndpoint);
        lroServer.start();

        try {
            final ProvisioningStateLroServiceClient client = RestProxy.create(ProvisioningStateLroServiceClient.class,
                createHttpPipeline(lroServer.port()),
                SERIALIZER);

            Function<PollingContext<PollResult<FooWithProvisioningState>>, Mono<PollResult<FooWithProvisioningState>>>
                lroInitFunction = newLroInitFunction(client, FooWithProvisioningState.class);

            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = PollerFactory.create(SERIALIZER,
                new HttpPipelineBuilder().build(),
                FooWithProvisioningState.class,
                FooWithProvisioningState.class,
                POLLING_DURATION,
                lroInitFunction);

            Mono<FooWithProvisioningState> resultMonoWithTimeout = lroFlux.last()
                .flatMap(AsyncPollResponse::getFinalResult)
                .timeout(timeoutDuration);

            // VirtualTimeScheduler seems not working correctly in StepVerifier. Could be a problem.
            // verify timeout.
            StepVerifier.create(resultMonoWithTimeout)
                .thenAwait()
                .verifyError(TimeoutException.class);

            // verify no more polling after timeout.
            int count = getCallCount.get();
            try {
                Thread.sleep(timeoutDuration.toMillis());
            } catch (InterruptedException e) {
                //
            }
            Assertions.assertEquals(count, getCallCount.get());
        } finally {
            if (lroServer.isRunning()) {
                lroServer.shutdown();
            }
        }
    }

    private static WireMockServer createServer(ResponseTransformer transformer,
                                               String... endpoints) {
        WireMockServer server = new WireMockServer(WireMockConfiguration
            .options()
            .dynamicPort()
            .extensions(transformer)
            .disableRequestJournal());
        for (String endpoint : endpoints) {
            server.stubFor(WireMock.any(WireMock.urlEqualTo(endpoint))
                .willReturn(WireMock.aResponse()));
        }
        return server;
    }

    private static HttpPipeline createHttpPipeline(int port) {
        return new HttpPipelineBuilder()
            .policies(new HttpPipelinePolicy() {
                @Override
                public Mono<HttpResponse> process(HttpPipelineCallContext context,
                                                  HttpPipelineNextPolicy next) {
                    HttpRequest request = context.getHttpRequest();
                    request.setUrl(updatePort(request.getUrl(), port));
                    context.setHttpRequest(request);
                    return next.process();
                }

                private URL updatePort(URL url, int port) {
                    try {
                        return new URL(url.getProtocol(), url.getHost(), port, url.getFile());
                    } catch (MalformedURLException mue) {
                        throw new RuntimeException(mue);
                    }
                }
            })
            .build();
    }

    private static <T> Function<PollingContext<PollResult<T>>, Mono<PollResult<T>>> newLroInitFunction(ProvisioningStateLroServiceClient client, Type type) {
        return context -> client.startLro()
            .flatMap(response -> FluxUtil.collectBytesInByteBufferStream(response.getValue())
                .map(bytes -> {
                    String content = new String(bytes, StandardCharsets.UTF_8);
                    //
                    PollingState state = PollingState.create(SERIALIZER,
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        content);
                    state.store(context);
                    //
                    T entity
                        = fromJson(content, type);
                    return new PollResult<>(entity);
                }));
    }


    private static String toJson(Object object) {
        try {
            return SERIALIZER.serialize(object, SerializerEncoding.JSON);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static <T> T fromJson(String json, Type type) {
        try {
            return SERIALIZER.deserialize(json, type, SerializerEncoding.JSON);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
