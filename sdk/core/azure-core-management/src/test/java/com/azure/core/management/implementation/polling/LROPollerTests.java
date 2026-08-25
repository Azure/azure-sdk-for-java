// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.azure.core.annotation.Host;
import com.azure.core.annotation.Put;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.management.Resource;
import com.azure.core.management.polling.PollResult;
import com.azure.core.management.polling.PollerFactory;
import com.azure.core.management.polling.SyncPollerFactory;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LROPollerTests {
    private static final ClientLogger LOGGER = new ClientLogger(LROPollerTests.class);

    private static final SerializerAdapter SERIALIZER = SerializerFactory.createDefaultManagementSerializerAdapter();

    private static final Duration POLLING_DURATION = Duration.ofMillis(100);

    @Host("http://localhost")
    @ServiceInterface(name = "ProvisioningStateLroService")
    interface ProvisioningStateLroServiceClient {
        @Put("/resource/1")
        Mono<Response<Flux<ByteBuffer>>> startLro(Context context);

        @Put("/resource/1")
        Response<BinaryData> startLroSync(Context context);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroBasedOnProvisioningState(boolean syncStack) {
        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> syncPoller = createSyncPoller(
                createMockHttpClient(new ServerConfigure()), FooWithProvisioningState.class, Context.NONE);

            for (int i = 0; i < 2; i++) {
                PollResponse<PollResult<FooWithProvisioningState>> pollResponse = syncPoller.poll();
                PollResult<FooWithProvisioningState> pollResult = pollResponse.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                if (i == 0) {
                    assertEquals(LongRunningOperationStatus.IN_PROGRESS, pollResponse.getStatus());
                    assertNull(pollResult.getValue().getResourceId());
                } else {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
                    assertNotNull(pollResult.getValue().getResourceId());
                }
            }
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(createMockHttpClient(new ServerConfigure()), FooWithProvisioningState.class);

            StepVerifier.create(lroFlux).assertNext(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
                assertNull(pollResult.getValue().getResourceId());
            }).assertNext(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                assertNotNull(pollResult.getValue().getResourceId());
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroBasedOnAsyncOperation(boolean syncStack) {
        ServerConfigure serverConfigure = new ServerConfigure();

        final String resourceEndpoint = "/resource/1";
        final String operationEndpoint = "/operations/1";
        HttpClient httpClient = new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);
                String url = request.getUrl().toString();
                if (!url.endsWith(resourceEndpoint) && !url.endsWith(operationEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    // accept response
                    response = new MockHttpResponse(request, 201,
                        new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION,
                            url.replace(resourceEndpoint, operationEndpoint)),
                        toJson(new FooWithProvisioningState("Creating")));
                } else if (request.getHttpMethod() == HttpMethod.GET) {
                    if (url.endsWith(operationEndpoint)) {
                        getCallCount[0]++;
                        if (getCallCount[0] < serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"InProgress\"}");
                        } else if (getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"Succeeded\"}");
                        }
                    } else if (url.endsWith(resourceEndpoint)
                        && getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                        // final resource
                        response = new MockHttpResponse(request, 200,
                            toJson(new FooWithProvisioningState("Succeeded", UUID.randomUUID().toString())));
                    } else {
                        response = mockResponse(request, 400, "Invalid state:" + url);
                    }
                }
                return Mono.just(response);
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroPoller
                = createSyncPoller(httpClient, FooWithProvisioningState.class, Context.NONE);

            for (int i = 0; i < 2; i++) {
                PollResponse<PollResult<FooWithProvisioningState>> pollResponse = lroPoller.poll();
                PollResult<FooWithProvisioningState> pollResult = pollResponse.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                if (i == 0) {
                    assertEquals(LongRunningOperationStatus.IN_PROGRESS, pollResponse.getStatus());
                } else {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());
                }
            }

            FooWithProvisioningState foo = lroPoller.getFinalResult();
            assertNotNull(foo.getResourceId());
            assertEquals("Succeeded", foo.getProvisioningState());
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(httpClient, FooWithProvisioningState.class);

            int[] onNextCallCount = new int[1];
            AsyncPollResponse<PollResult<FooWithProvisioningState>, FooWithProvisioningState> pollResponse
                = lroFlux.doOnNext(response -> {
                    PollResult<FooWithProvisioningState> pollResult = response.getValue();
                    assertNotNull(pollResult);
                    assertNotNull(pollResult.getValue());
                    onNextCallCount[0]++;
                    if (onNextCallCount[0] == 1) {
                        assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
                    } else if (onNextCallCount[0] == 2) {
                        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                    } else {
                        throw new IllegalStateException("Poller emitted more than expected value.");
                    }
                }).blockLast();

            FooWithProvisioningState foo = pollResponse.getFinalResult().block();
            assertNotNull(foo.getResourceId());
            assertEquals("Succeeded", foo.getProvisioningState());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroBasedOnAsyncOperationFailed(boolean syncStack) {
        ServerConfigure serverConfigure = new ServerConfigure();

        final String resourceEndpoint = "/resource/1";
        final String operationEndpoint = "/operations/1";
        HttpClient httpClient = new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);
                String url = request.getUrl().toString();
                if (!url.endsWith(resourceEndpoint) && !url.endsWith(operationEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    // accept response
                    response = new MockHttpResponse(request, 201,
                        new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION,
                            url.replace(resourceEndpoint, operationEndpoint)),
                        toJson(new FooWithProvisioningState("Creating")));
                } else if (request.getHttpMethod() == HttpMethod.GET) {
                    if (url.endsWith(operationEndpoint)) {
                        getCallCount[0]++;
                        if (getCallCount[0] < serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"InProgress\"}");
                        } else if (getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                            response = new MockHttpResponse(request, 200,
                                new HttpHeaders().add(HttpHeaderName.X_MS_REQUEST_ID, UUID.randomUUID().toString()),
                                stringBytes("{\"status\": \"Failed\"}"));
                        }
                    } else {
                        response = mockResponse(request, 400, "Invalid state:" + request.getUrl());
                    }
                }
                return Mono.just(response);
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroPoller
                = createSyncPoller(httpClient, FooWithProvisioningState.class, Context.NONE);

            for (int i = 0; i < 2; i++) {
                PollResponse<PollResult<FooWithProvisioningState>> pollResponse = lroPoller.poll();
                PollResult<FooWithProvisioningState> pollResult = pollResponse.getValue();
                assertNotNull(pollResult);
                if (i == 0) {
                    assertEquals(LongRunningOperationStatus.IN_PROGRESS, pollResponse.getStatus());
                    assertNotNull(pollResult.getValue());
                } else {
                    assertEquals(LongRunningOperationStatus.FAILED, pollResponse.getStatus());
                    assertNotNull(pollResult.getError());
                }
            }
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(httpClient, FooWithProvisioningState.class);

            StepVerifier.create(lroFlux).assertNext(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
            }).assertNext(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertEquals(LongRunningOperationStatus.FAILED, response.getStatus());
                assertEquals(200, response.getValue().getError().getResponseStatusCode());
                assertNotNull(response.getValue().getError());
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lro200SucceededNoPoll(boolean syncStack) {
        final String resourceEndpoint = "/resource/1";
        final String sampleVaultUpdateSucceededResponse
            = "{\"id\":\"/subscriptions/000/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu\","
                + "\"name\":\"v1weidxu\",\"type\":\"Microsoft.KeyVault/vaults\",\"location\":\"centralus\",\"tags\":{},"
                + "\"properties\":{\"sku\":{\"family\":\"A\",\"name\":\"standard\"},\"tenantId\":\"000\","
                + "\"accessPolicies\":[],\"enabledForDeployment\":false,\"vaultUri\":\"https://v1weidxu.vault.azure.net/\","
                + "\"provisioningState\":\"Succeeded\"}}";
        Duration pollInterval = Duration.ofSeconds(30);
        HttpClient httpClient = new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);

                if (!request.getUrl().toString().endsWith(resourceEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    getCallCount[0]++;
                    if (getCallCount[0] > 1) {
                        response = mockResponse(request, 500, "Unexpected additional polling detected.");
                    } else {
                        // 200 response with provisioningState=Succeeded.
                        response = new MockHttpResponse(request, 200,
                            new HttpHeaders().add(HttpHeaderName.RETRY_AFTER,
                                String.valueOf(pollInterval.getSeconds())),
                            stringBytes(sampleVaultUpdateSucceededResponse));
                    }
                }
                return Mono.just(response);
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<Resource>, Resource> lroPoller
                = createSyncPoller(httpClient, Resource.class, Context.NONE);

            lroPoller.setPollInterval(pollInterval);
            long timeBeforePoll = System.currentTimeMillis();

            Resource result = lroPoller.getFinalResult();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("v1weidxu", result.name());
            assertEquals("Microsoft.KeyVault/vaults", result.type());

            assertTrue(System.currentTimeMillis() - timeBeforePoll < pollInterval.toMillis());
        } else {
            PollerFlux<PollResult<Resource>, Resource> lroFlux = createAsyncPoller(httpClient, Resource.class);

            StepVerifier.create(lroFlux).expectSubscription().expectNextMatches(response -> {
                PollResult<Resource> pollResult = response.getValue();
                return response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    && pollResult != null
                    && pollResult.getValue() != null
                    && pollResult.getValue().id() != null;
            }).verifyComplete();

            AsyncPollResponse<PollResult<Resource>, Resource> asyncPollResponse = lroFlux.blockLast();
            assertNotNull(asyncPollResponse);

            Resource result = asyncPollResponse.getFinalResult().block();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("v1weidxu", result.name());
            assertEquals("Microsoft.KeyVault/vaults", result.type());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lro201AsyncOperationSucceededNoPoll(boolean syncStack) {
        final String resourceEndpoint = "/resource/1";
        final String sampleNicCreateSucceededResponse
            = "{\"name\":\"nic4159682782\",\"id\":\"/subscriptions/000/resourceGroups/javanwmrg59122/providers/"
                + "Microsoft.Network/networkInterfaces/nic4159682782\","
                + "\"etag\":\"W/\\\"92581fdf-b55d-4ca1-a1fa-9de0cf117b4f\\\"\",\"location\":\"eastus\",\"tags\":{},"
                + "\"properties\":{\"provisioningState\":\"Succeeded\","
                + "\"resourceGuid\":\"e0a8ecd1-faa0-468c-8e30-411ca27417a1\",\"ipConfigurations\":[{\"name\":\"primary\","
                + "\"id\":\"/subscriptions/ec0aa5f7-9e78-40c9-85cd-535c6305b380/resourceGroups/javanwmrg59122/providers"
                + "/Microsoft.Network/networkInterfaces/nic4159682782/ipConfigurations/primary\","
                + "\"etag\":\"W/\\\"92581fdf-b55d-4ca1-a1fa-9de0cf117b4f\\\"\","
                + "\"type\":\"Microsoft.Network/networkInterfaces/ipConfigurations\",\"properties\":"
                + "{\"provisioningState\":\"Succeeded\",\"privateIPAddress\":\"10.0.0.6\","
                + "\"privateIPAllocationMethod\":\"Dynamic\",\"subnet\":{\"id\":\"/subscriptions/"
                + "ec0aa5f7-9e78-40c9-85cd-535c6305b380/resourceGroups/javanwmrg59122/providers/Microsoft.Network/"
                + "virtualNetworks/neta3e8953331/subnets/subnet1\"},\"primary\":true,"
                + "\"privateIPAddressVersion\":\"IPv4\"}}],\"dnsSettings\":{\"dnsServers\":[],\"appliedDnsServers\":[],"
                + "\"internalDomainNameSuffix\":\"a4vv4vgg2cluvfhfgw43jtn2aa.bx.internal.cloudapp.net\"},"
                + "\"enableAcceleratedNetworking\":false,\"enableIPForwarding\":false,\"hostedWorkloads\":[],"
                + "\"tapConfigurations\":[]},\"type\":\"Microsoft.Network/networkInterfaces\"}";
        HttpClient httpClient = request -> {
            HttpResponse response = unmockedPath(request);
            if (!request.getUrl().toString().endsWith(resourceEndpoint)) {
                response = unsupportedPath(request);
            } else if (request.getHttpMethod() == HttpMethod.PUT) {
                // 201 response with provisioningState=Succeeded.
                response = new MockHttpResponse(request, 201, new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION,
                    "https://management.azure.com/subscriptions/000/providers/Microsoft.Network/locations/eastus/operations/123"),
                    stringBytes(sampleNicCreateSucceededResponse));
            }
            return Mono.just(response);
        };

        if (syncStack) {
            SyncPoller<PollResult<Resource>, Resource> lroPoller
                = createSyncPoller(httpClient, Resource.class, Context.NONE);

            PollResponse<PollResult<Resource>> response = lroPoller.poll();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
            PollResult<Resource> pollResult = response.getValue();
            assertNotNull(pollResult);
            assertNotNull(pollResult.getValue().id());

            Resource result = lroPoller.getFinalResult();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("nic4159682782", result.name());
            assertEquals("Microsoft.Network/networkInterfaces", result.type());
        } else {
            PollerFlux<PollResult<Resource>, Resource> lroFlux = createAsyncPoller(httpClient, Resource.class);

            StepVerifier.create(lroFlux).expectSubscription().expectNextMatches(response -> {
                PollResult<Resource> pollResult = response.getValue();
                return response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    && pollResult != null
                    && pollResult.getValue() != null
                    && pollResult.getValue().id() != null;
            }).verifyComplete();

            AsyncPollResponse<PollResult<Resource>, Resource> asyncPollResponse = lroFlux.blockLast();
            assertNotNull(asyncPollResponse);

            Resource result = asyncPollResponse.getFinalResult().block();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("nic4159682782", result.name());
            assertEquals("Microsoft.Network/networkInterfaces", result.type());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lro201SucceededNoPoll(boolean syncStack) {
        final String resourceEndpoint = "/resource/1";
        final String sampleSearchServiceCreateSucceededResponse
            = "{\"id\":\"/subscriptions/000/resourceGroups/rg86829b7a87d74/providers/Microsoft.Search/searchServices/"
                + "ss3edfb54d\",\"name\":\"ss3edfb54d\",\"type\":\"Microsoft.Search/searchServices\","
                + "\"location\":\"West US\",\"properties\":{\"replicaCount\":1,\"partitionCount\":1,\"status\":\"running\","
                + "\"statusDetails\":\"\",\"provisioningState\":\"succeeded\",\"hostingMode\":\"Default\","
                + "\"publicNetworkAccess\":\"Enabled\",\"networkRuleSet\":{\"ipRules\":[],\"bypass\":\"None\"},"
                + "\"privateEndpointConnections\":[],\"sharedPrivateLinkResources\":[]},\"sku\":{\"name\":\"free\"}}";
        HttpClient httpClient = request -> {
            HttpResponse response = unmockedPath(request);
            if (!request.getUrl().toString().endsWith(resourceEndpoint)) {
                response = unsupportedPath(request);
            } else if (request.getHttpMethod() == HttpMethod.PUT) {
                // 201 response with provisioningState=Succeeded.
                response = new MockHttpResponse(request, 201, new HttpHeaders().add(HttpHeaderName.LOCATION,
                    "https://management.azure.com/subscriptions/000/resourceGroups/rg86829b7a87d74/providers/Microsoft.Search/searchServices/ss3edfb54d"),
                    stringBytes(sampleSearchServiceCreateSucceededResponse));
            }
            return Mono.just(response);
        };

        if (syncStack) {
            SyncPoller<PollResult<Resource>, Resource> lroPoller
                = createSyncPoller(httpClient, Resource.class, Context.NONE);

            PollResponse<PollResult<Resource>> pollResponse = lroPoller.poll();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());

            Resource result = lroPoller.getFinalResult();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("ss3edfb54d", result.name());
            assertEquals("Microsoft.Search/searchServices", result.type());
        } else {
            PollerFlux<PollResult<Resource>, Resource> lroFlux = createAsyncPoller(httpClient, Resource.class);

            StepVerifier.create(lroFlux).expectSubscription().expectNextMatches(response -> {
                PollResult<Resource> pollResult = response.getValue();
                return response.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED
                    && pollResult != null
                    && pollResult.getValue() != null
                    && pollResult.getValue().id() != null;
            }).verifyComplete();

            AsyncPollResponse<PollResult<Resource>, Resource> asyncPollResponse = lroFlux.blockLast();
            assertNotNull(asyncPollResponse);

            Resource result = asyncPollResponse.getFinalResult().block();
            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("ss3edfb54d", result.name());
            assertEquals("Microsoft.Search/searchServices", result.type());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroMalformedAaoUrl(boolean syncStack) {
        ServerConfigure serverConfigure = new ServerConfigure();
        serverConfigure.pollingCountTillSuccess = 3;

        final String resourceEndpoint = "/resource/1";
        final String operationEndpoint = "/operations/1";
        HttpClient httpClient = new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);
                String url = request.getUrl().toString();
                if (!url.endsWith(resourceEndpoint) && !url.endsWith(operationEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    // accept response
                    response = new MockHttpResponse(request, 201,
                        new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION,
                            url.replace(resourceEndpoint, operationEndpoint)),
                        toJson(new FooWithProvisioningState("Creating")));
                } else if (request.getHttpMethod() == HttpMethod.GET) {
                    if (url.endsWith(operationEndpoint)) {
                        getCallCount[0]++;
                        if (getCallCount[0] < serverConfigure.pollingCountTillSuccess) {
                            if (getCallCount[0] == 1) {
                                response = mockResponse(request, 200, "{\"status\": \"InProgress\"}");
                            } else {
                                response = new MockHttpResponse(request, 200,
                                    new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION, "/invalid_url"),
                                    stringBytes("{\"status\": \"InProgress\"}"));
                            }
                        } else if (getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"Succeeded\"}");
                        }
                    } else if (url.endsWith(resourceEndpoint)
                        && getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                        // final resource
                        response = new MockHttpResponse(request, 200,
                            toJson(new FooWithProvisioningState("Succeeded", UUID.randomUUID().toString())));
                    } else {
                        response = mockResponse(request, 400, "Invalid state:" + request.getUrl());
                    }
                }
                return Mono.just(response);
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroPoller
                = createSyncPoller(httpClient, FooWithProvisioningState.class, Context.NONE);

            PollResponse<PollResult<FooWithProvisioningState>> pollResult = lroPoller.poll();
            assertEquals(LongRunningOperationStatus.IN_PROGRESS, pollResult.getStatus());

            pollResult = lroPoller.poll();
            assertEquals(LongRunningOperationStatus.FAILED, pollResult.getStatus());
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(httpClient, FooWithProvisioningState.class);

            StepVerifier.create(lroFlux).expectSubscription().expectNextMatches(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                return response.getStatus() == LongRunningOperationStatus.IN_PROGRESS
                    && pollResult != null
                    && pollResult.getValue() != null;
            }).expectNextMatches(response -> {
                PollResult<FooWithProvisioningState> pollResult = response.getValue();
                return response.getStatus() == LongRunningOperationStatus.FAILED
                    && pollResult != null
                    && pollResult.getError() != null;
            }).verifyComplete();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroTimeout(boolean syncStack) {
        final Duration timeoutDuration = Duration.ofMillis(1000);   // use a large timeout for manual verification

        final String resourceEndpoint = "/resource/1";
        final AtomicInteger getCallCount = new AtomicInteger(0);
        HttpClient httpClient = request -> {
            HttpResponse response = unmockedPath(request);
            if (!request.getUrl().toString().endsWith(resourceEndpoint)) {
                response = unsupportedPath(request);
            } else if (request.getHttpMethod() == HttpMethod.PUT || request.getHttpMethod() == HttpMethod.GET) {
                if (request.getHttpMethod() == HttpMethod.GET) {
                    getCallCount.getAndIncrement();
                }
                response = new MockHttpResponse(request, 200, toJson(new FooWithProvisioningState("IN_PROGRESS")));
            }
            return Mono.just(response);
        };

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroPoller
                = createSyncPoller(httpClient, FooWithProvisioningState.class, Context.NONE);

            boolean timedOut = false;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<?> future = executor.submit((Runnable) lroPoller::getFinalResult);
                future.get(timeoutDuration.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                timedOut = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertTrue(timedOut);
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(httpClient, FooWithProvisioningState.class);

            Mono<FooWithProvisioningState> resultMonoWithTimeout
                = lroFlux.last().flatMap(AsyncPollResponse::getFinalResult).timeout(timeoutDuration);

            // VirtualTimeScheduler seems not working correctly in StepVerifier. Could be a problem.
            // verify timeout.
            StepVerifier.create(resultMonoWithTimeout).thenAwait().verifyError(TimeoutException.class);

            // verify no more polling after timeout.
            int count = getCallCount.get();
            try {
                Thread.sleep(timeoutDuration.toMillis());
            } catch (InterruptedException e) {
                //
            }
            assertEquals(count, getCallCount.get());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroRetryAfter(boolean syncStack) {
        ServerConfigure configure = new ServerConfigure();
        Duration expectedPollingDuration = Duration.ofSeconds(1);
        configure.pollingCountTillSuccess = 3;
        configure.additionalHeaders = new HttpHeaders().add(HttpHeaderName.RETRY_AFTER, "1");  // 1 second

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> syncPoller
                = createSyncPoller(createMockHttpClient(configure), FooWithProvisioningState.class, Context.NONE);

            long nanoTime = System.nanoTime();
            FooWithProvisioningState result = syncPoller.getFinalResult();
            assertNotNull(result);

            Duration pollingDuration = Duration.ofNanos(System.nanoTime() - nanoTime);
            assertTrue(pollingDuration.compareTo(expectedPollingDuration) > 0);
        } else {
            PollerFlux<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroFlux
                = createAsyncPoller(createMockHttpClient(configure), FooWithProvisioningState.class);

            long nanoTime = System.nanoTime();

            FooWithProvisioningState result = lroFlux
                .doOnNext(response -> LOGGER.log(LogLevel.VERBOSE,
                    () -> String.format("[%s] status %s%n", OffsetDateTime.now(), response.getStatus())))
                .blockLast()
                .getFinalResult()
                .block();
            assertNotNull(result);

            Duration pollingDuration = Duration.ofNanos(System.nanoTime() - nanoTime);
            assertTrue(pollingDuration.compareTo(expectedPollingDuration) > 0);
        }
    }

    @Test
    public void lroContextSubscriberContext() {
        HttpPipelinePolicy contextVerifyPolicy = (context, next) -> {
            Optional<Object> valueOpt = context.getData("key1");
            if (valueOpt.isPresent() && "value1".equals(valueOpt.get())) {
                return next.process();
            } else {
                return Mono.error(new AssertionError());
            }
        };

        Flux<AsyncPollResponse<PollResult<FooWithProvisioningState>, FooWithProvisioningState>> lroFlux
            = createAsyncPoller(createMockHttpClient(new ServerConfigure()), FooWithProvisioningState.class,
                contextVerifyPolicy);
        lroFlux = lroFlux.contextWrite(context -> context.put("key1", "value1"));

        StepVerifier.create(lroFlux.last())
            .assertNext(response -> assertNotNull(response.getFinalResult()))
            .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroContext(boolean syncStack) {
        HttpPipelinePolicy contextVerifyPolicy = (context, next) -> {
            Optional<Object> valueOpt = context.getData("key1");
            if (valueOpt.isPresent() && "value1".equals(valueOpt.get())) {
                return next.process();
            } else {
                return Mono.error(new AssertionError());
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> lroPoller
                = createSyncPoller(createMockHttpClient(new ServerConfigure()), FooWithProvisioningState.class,
                    new Context("key1", "value1"), contextVerifyPolicy);

            FooWithProvisioningState result = lroPoller.getFinalResult();
            assertNotNull(result);
        } else {
            final HttpPipeline httpPipeline
                = createHttpPipeline(createMockHttpClient(new ServerConfigure()), contextVerifyPolicy);
            Flux<AsyncPollResponse<PollResult<FooWithProvisioningState>, FooWithProvisioningState>> lroFlux
                = PollerFactory.create(SERIALIZER, httpPipeline, FooWithProvisioningState.class,
                    FooWithProvisioningState.class, POLLING_DURATION, newLroInitFunction(createClient(httpPipeline))
                        .contextWrite(context -> context.put("key1", "value1")),
                    new Context("key1", "value1"));

            FooWithProvisioningState result = lroFlux.blockLast().getFinalResult().block();
            assertNotNull(result);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void lroBasedOnAsyncOperationInBinaryData(boolean syncStack) {
        ServerConfigure serverConfigure = new ServerConfigure();

        final String resourceEndpoint = "/resource/1";
        final String operationEndpoint = "/operations/1";
        HttpClient httpClient = new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);
                String url = request.getUrl().toString();
                if (!url.endsWith(resourceEndpoint) && !url.endsWith(operationEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    // accept response
                    response = new MockHttpResponse(request, 201,
                        new HttpHeaders().add(HttpHeaderName.AZURE_ASYNCOPERATION,
                            url.replace(resourceEndpoint, operationEndpoint)),
                        toJson(new FooWithProvisioningState("Creating")));
                } else if (request.getHttpMethod() == HttpMethod.GET) {
                    if (url.endsWith(operationEndpoint)) {
                        getCallCount[0]++;
                        if (getCallCount[0] < serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"InProgress\"}");
                        } else if (getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                            response = mockResponse(request, 200, "{\"status\": \"Succeeded\"}");
                        }
                    } else if (url.endsWith(resourceEndpoint)
                        && getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                        // final resource
                        response = new MockHttpResponse(request, 200,
                            toJson(new FooWithProvisioningState("Succeeded", UUID.randomUUID().toString())));
                    } else {
                        response = mockResponse(request, 400, "Invalid state:" + request.getUrl());
                    }
                }
                return Mono.just(response);
            }
        };

        if (syncStack) {
            SyncPoller<PollResult<BinaryData>, BinaryData> lroPoller
                = createSyncPoller(httpClient, BinaryData.class, Context.NONE);

            for (int i = 0; i < 2; i++) {
                PollResponse<PollResult<BinaryData>> response = lroPoller.poll();
                PollResult<BinaryData> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                if (i == 0) {
                    assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
                } else {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                }
            }

            BinaryData foo = lroPoller.getFinalResult();
            FooWithProvisioningState fooAsObject = foo.toObject(FooWithProvisioningState.class);
            assertNotNull(fooAsObject.getResourceId());
            assertEquals("Succeeded", fooAsObject.getProvisioningState());
        } else {
            PollerFlux<PollResult<BinaryData>, BinaryData> lroFlux = createAsyncPoller(httpClient, BinaryData.class);

            int[] onNextCallCount = new int[1];
            AsyncPollResponse<PollResult<BinaryData>, BinaryData> pollResponse = lroFlux.doOnNext(response -> {
                PollResult<BinaryData> pollResult = response.getValue();
                assertNotNull(pollResult);
                assertNotNull(pollResult.getValue());
                onNextCallCount[0]++;
                if (onNextCallCount[0] == 1) {
                    assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
                } else if (onNextCallCount[0] == 2) {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                } else {
                    throw new IllegalStateException("Poller emitted more than expected value.");
                }
            }).blockLast();

            BinaryData foo = pollResponse.getFinalResult().block();
            FooWithProvisioningState fooAsObject = foo.toObject(FooWithProvisioningState.class);
            assertNotNull(fooAsObject.getResourceId());
            assertEquals("Succeeded", fooAsObject.getProvisioningState());
        }
    }

    private static class ServerConfigure {
        private int pollingCountTillSuccess = 2;

        private HttpHeaders additionalHeaders = new HttpHeaders();
    }

    private static HttpClient createMockHttpClient(ServerConfigure serverConfigure) {
        final String resourceEndpoint = "/resource/1";

        return new HttpClient() {
            private final int[] getCallCount = new int[1];

            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                HttpResponse response = unmockedPath(request);
                if (!request.getUrl().toString().endsWith(resourceEndpoint)) {
                    response = unsupportedPath(request);
                } else if (request.getHttpMethod() == HttpMethod.PUT) {
                    LOGGER.log(LogLevel.VERBOSE,
                        () -> String.format("[%s] PUT status %s%n", OffsetDateTime.now(), "IN_PROGRESS"));
                    response = new MockHttpResponse(request, 200, serverConfigure.additionalHeaders,
                        toJson(new FooWithProvisioningState("IN_PROGRESS")));
                } else if (request.getHttpMethod() == HttpMethod.GET) {
                    getCallCount[0]++;
                    if (getCallCount[0] < serverConfigure.pollingCountTillSuccess) {
                        LOGGER.log(LogLevel.VERBOSE,
                            () -> String.format("[%s] GET status %s%n", OffsetDateTime.now(), "IN_PROGRESS"));
                        response = new MockHttpResponse(request, 200, serverConfigure.additionalHeaders,
                            toJson(new FooWithProvisioningState("IN_PROGRESS")));
                    } else if (getCallCount[0] == serverConfigure.pollingCountTillSuccess) {
                        LOGGER.log(LogLevel.VERBOSE,
                            () -> String.format("[%s] GET status %s%n", OffsetDateTime.now(), "SUCCEEDED"));
                        response = new MockHttpResponse(request, 200,
                            toJson(new FooWithProvisioningState("SUCCEEDED", UUID.randomUUID().toString())));
                    } else {
                        response = mockResponse(request, 500, "GET-based polling exceeded expected poll count.");
                    }
                }
                return Mono.just(response);
            }
        };
    }

    private static ProvisioningStateLroServiceClient createClient(HttpPipeline httpPipeline) {
        return RestProxy.create(ProvisioningStateLroServiceClient.class, httpPipeline, SERIALIZER);
    }

    private static HttpPipeline createHttpPipeline(HttpClient httpClient, HttpPipelinePolicy... additionalPolicies) {
        return new HttpPipelineBuilder().policies(additionalPolicies).httpClient(httpClient).build();
    }

    private static <T> SyncPoller<PollResult<T>, T> createSyncPoller(HttpClient httpClient, Class<T> type,
        Context context, HttpPipelinePolicy... policies) {
        HttpPipeline httpPipeline = createHttpPipeline(httpClient, policies);
        ProvisioningStateLroServiceClient client
            = RestProxy.create(ProvisioningStateLroServiceClient.class, httpPipeline, SERIALIZER);
        return SyncPollerFactory.create(SERIALIZER, httpPipeline, type, type, POLLING_DURATION,
            () -> client.startLroSync(context), context);
    }

    private static <T> PollerFlux<PollResult<T>, T> createAsyncPoller(HttpClient httpClient, Class<T> type,
        HttpPipelinePolicy... policies) {
        HttpPipeline httpPipeline = createHttpPipeline(httpClient, policies);
        ProvisioningStateLroServiceClient client
            = RestProxy.create(ProvisioningStateLroServiceClient.class, httpPipeline, SERIALIZER);
        return PollerFactory.create(SERIALIZER, httpPipeline, type, type, POLLING_DURATION,
            FluxUtil.fluxContext(context -> client.startLro(context).flux()).next());
    }

    private Mono<Response<Flux<ByteBuffer>>> newLroInitFunction(ProvisioningStateLroServiceClient client) {
        return FluxUtil.fluxContext(context -> client.startLro(context).flux()).next();
    }

    private static byte[] toJson(Object object) {
        try {
            return SERIALIZER.serializeToBytes(object, SerializerEncoding.JSON);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static byte[] stringBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static HttpResponse mockResponse(HttpRequest request, int status, String body) {
        return new MockHttpResponse(request, status, stringBytes(body));
    }

    private static HttpResponse unmockedPath(HttpRequest request) {
        return new MockHttpResponse(request, 500, stringBytes("Unmocked request path: " + request.getUrl()));
    }

    private static HttpResponse unsupportedPath(HttpRequest request) {
        return new MockHttpResponse(request, 500, stringBytes("Unsupported path: " + request.getUrl()));
    }
}
