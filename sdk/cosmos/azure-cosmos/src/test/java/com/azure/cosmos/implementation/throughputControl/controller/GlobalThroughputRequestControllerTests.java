// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import com.azure.cosmos.implementation.throughputControl.controller.request.GlobalThroughputRequestController;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalThroughputRequestControllerTests {
    private final double DEFAULT_SCHEDULED_THROUGHPUT = 2.0;

    private GlobalEndpointManager globalEndpointManager;
    private GlobalThroughputRequestController requestController;
    private URI readLocation;
    private URI writeLocation;

    @BeforeMethod
    public void before_GlobalThroughputRequestControllerTest() throws URISyntaxException {
        writeLocation = new URI("https://write-localtion1.documents.azure.com");
        readLocation = new URI("https://read-localtion1.documents.azure.com");
        globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new UnmodifiableList<>(Collections.singletonList(writeLocation))).when(globalEndpointManager).getWriteEndpoints();
        Mockito.doReturn(new UnmodifiableList<>(Collections.singletonList(readLocation))).when(globalEndpointManager).getReadEndpoints();

        requestController = new GlobalThroughputRequestController(globalEndpointManager, DEFAULT_SCHEDULED_THROUGHPUT);
    }


    @Test
    public void init() {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerByRegion(requestController);
        assertThat(requestThrottlerMapByRegion).size().isEqualTo(2);
        assertThat(Collections.list(requestThrottlerMapByRegion.keys())).contains(writeLocation, readLocation);
    }

    @Test
    public void canHandleRequest() {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        assertThat(requestController.canHandleRequest(requestMock)).isTrue();
    }

    @Test
    public void processRequest() throws URISyntaxException {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerByRegion(requestController);
        ThroughputRequestThrottler writeLocationThrottlerSpy = Mockito.spy(requestThrottlerMapByRegion.get(writeLocation));
        requestThrottlerMapByRegion.put(writeLocation, writeLocationThrottlerSpy);
        ReflectionUtils.setRequestThrottlerByRegion(requestController, requestThrottlerMapByRegion);

        // First request: Can find the matching request throttler in request controller
        RxDocumentServiceRequest request1Mock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(writeLocation).when(globalEndpointManager).resolveServiceEndpoint(request1Mock);

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request1Mono = request1MonoPublisher.mono();
        StoreResponse storeResponse1Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request1Mock, request1Mono))
            .then(() -> request1MonoPublisher.emit(storeResponse1Mock))
            .expectNext(storeResponse1Mock)
            .verifyComplete();
        Mockito.verify(writeLocationThrottlerSpy, Mockito.times(1)).processRequest(request1Mock, request1Mono);

        // Second request: Cannot find the matching request throttler in request controller, will create a new one
        RxDocumentServiceRequest request2Mock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(new URI("https://write-localtion2.documents.azure.com")).when(globalEndpointManager).resolveServiceEndpoint(request2Mock);

        TestPublisher<StoreResponse> request2MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request2Mono = request2MonoPublisher.mono();
        StoreResponse storeResponse2Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request2Mock, request2Mono))
            .then(() -> request2MonoPublisher.emit(storeResponse2Mock))
            .expectNext(storeResponse2Mock)
            .verifyComplete();

        assertThat(requestThrottlerMapByRegion).size().isEqualTo(3);
    }

    @Test
    public void renewThroughputUsageCycle() {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerByRegion(requestController);
        ThroughputRequestThrottler writeLocationThrottlerSpy = Mockito.spy(requestThrottlerMapByRegion.get(writeLocation));
        ThroughputRequestThrottler readLocationThrottlerSpy = Mockito.spy(requestThrottlerMapByRegion.get(readLocation));

        requestThrottlerMapByRegion.put(writeLocation, writeLocationThrottlerSpy);
        requestThrottlerMapByRegion.put(readLocation, readLocationThrottlerSpy);

        double newScheduledThroughput = 3.0;
        requestController.renewThroughputUsageCycle(newScheduledThroughput);
        Mockito.verify(writeLocationThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(newScheduledThroughput);
        Mockito.verify(readLocationThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(newScheduledThroughput);
    }
}
