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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalThroughputRequestControllerTests {
    private static double scheduledThroughput;
    private static GlobalEndpointManager globalEndpointManager;
    private static URI readLocation;

    private GlobalThroughputRequestController requestController;

    @BeforeClass(groups = "unit")
    public static void beforeClass_GlobalThroughputRequestControllerTests() throws URISyntaxException {
        scheduledThroughput = 2.0;

        readLocation = new URI("https://read-localtion1.documents.azure.com");

        globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new UnmodifiableList<>(Collections.singletonList(readLocation))).when(globalEndpointManager).getReadEndpoints();
    }

    @BeforeMethod(groups = "unit")
    public void before_GlobalThroughputRequestControllerTest() {
        requestController = new GlobalThroughputRequestController(globalEndpointManager, scheduledThroughput);
    }


    @Test(groups = "unit")
    public void init() {
        requestController.init().subscribe();

        Set<URI> locations = new HashSet<>();
        locations.add(readLocation);

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerMap(requestController);
        assertThat(requestThrottlerMapByRegion).size().isEqualTo(locations.size());
        assertThat(Collections.list(requestThrottlerMapByRegion.keys())).containsAll(locations);
    }

    @Test(groups = "unit")
    public void canHandleRequest() {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        assertThat(requestController.canHandleRequest(requestMock)).isTrue();
    }

    @Test(groups = "unit")
    public void processRequest() throws URISyntaxException {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerMap(requestController);
        ThroughputRequestThrottler writeLocationThrottlerSpy = Mockito.spy(requestThrottlerMapByRegion.get(readLocation));
        requestThrottlerMapByRegion.put(readLocation, writeLocationThrottlerSpy);

        // First request: Can find the matching region request throttler in request controller
        RxDocumentServiceRequest request1Mock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(readLocation).when(globalEndpointManager).resolveServiceEndpoint(request1Mock);

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request1Mono = request1MonoPublisher.mono();
        StoreResponse storeResponse1Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request1Mock, request1Mono))
            .then(() -> request1MonoPublisher.emit(storeResponse1Mock))
            .expectNext(storeResponse1Mock)
            .verifyComplete();
        Mockito.verify(writeLocationThrottlerSpy, Mockito.times(1)).processRequest(request1Mock, request1Mono);

        // Second request: Cannot find the matching region request throttler in request controller, will create a new one
        RxDocumentServiceRequest request2Mock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(new URI("https://write-localtion2.documents.azure.com")).when(globalEndpointManager).resolveServiceEndpoint(request2Mock);

        TestPublisher<StoreResponse> request2MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request2Mono = request2MonoPublisher.mono();
        StoreResponse storeResponse2Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request2Mock, request2Mono))
            .then(() -> request2MonoPublisher.emit(storeResponse2Mock))
            .expectNext(storeResponse2Mock)
            .verifyComplete();

        assertThat(requestThrottlerMapByRegion).size().isEqualTo(2);
    }

    @Test(groups = "unit")
    public void renewThroughputUsageCycle() {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ThroughputRequestThrottler> requestThrottlerMapByRegion = ReflectionUtils.getRequestThrottlerMap(requestController);
        List<ThroughputRequestThrottler> requestThrottlerSpies = new ArrayList<>();
        for (URI location : Collections.list(requestThrottlerMapByRegion.keys())) {
            ThroughputRequestThrottler requestThrottlerSpy = Mockito.spy(requestThrottlerMapByRegion.get(location));
            requestThrottlerSpies.add(requestThrottlerSpy);
            requestThrottlerMapByRegion.put(location, requestThrottlerSpy);
        }

        double newScheduledThroughput = 3.0;
        requestController.renewThroughputUsageCycle(newScheduledThroughput);
        for (ThroughputRequestThrottler requestThrottlerSpy : requestThrottlerSpies) {
            Mockito.verify(requestThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(newScheduledThroughput);
        }
    }
}
