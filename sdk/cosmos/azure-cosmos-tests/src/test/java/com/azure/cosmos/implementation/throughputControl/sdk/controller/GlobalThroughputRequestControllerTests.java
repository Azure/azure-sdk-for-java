// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk.controller;

import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.throughputControl.ThroughputControlRequestContext;
import com.azure.cosmos.implementation.throughputControl.sdk.ThroughputRequestThrottler;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.request.GlobalThroughputRequestController;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.assertj.core.api.Assertions.assertThat;

public class GlobalThroughputRequestControllerTests {
    private static double scheduledThroughput = 2.0;
    private GlobalThroughputRequestController requestController;

    @BeforeMethod(groups = "unit")
    public void before_GlobalThroughputRequestControllerTest() {
        requestController = new GlobalThroughputRequestController(scheduledThroughput);
    }


    @Test(groups = "unit")
    public void init() {
        // Test init can complete without error
        requestController.init().subscribe();
    }

    @Test(groups = "unit")
    public void canHandleRequest() {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        assertThat(requestController.canHandleRequest(requestMock)).isTrue();
    }

    @Test(groups = "unit")
    public void processRequest() {
        requestController.init().subscribe();

        ThroughputRequestThrottler requestThrottler = ReflectionUtils.getRequestThrottler(requestController);
        ThroughputRequestThrottler requestThrottlerSpy = Mockito.spy(requestThrottler);
        ReflectionUtils.setRequestThrottler(requestController, requestThrottlerSpy);

        // First request: Can find the matching region request throttler in request controller
        RxDocumentServiceRequest request =
            RxDocumentServiceRequest.create(
                mockDiagnosticsClientContext(),
                OperationType.Read,
                ResourceType.Document);
        request.requestContext.setThroughputControlRequestContext(new ThroughputControlRequestContext("test"));

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request1Mono = request1MonoPublisher.mono();
        StoreResponse storeResponse1Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request, request1Mono))
            .then(() -> request1MonoPublisher.emit(storeResponse1Mock))
            .expectNext(storeResponse1Mock)
            .verifyComplete();
        Mockito.verify(requestThrottlerSpy, Mockito.times(1)).processRequest(request, request1Mono);
    }

    @Test(groups = "unit")
    public void renewThroughputUsageCycle() {
        requestController.init().subscribe();

        ThroughputRequestThrottler requestThrottler = ReflectionUtils.getRequestThrottler(requestController);
        ThroughputRequestThrottler requestThrottlerSpy = Mockito.spy(requestThrottler);
        ReflectionUtils.setRequestThrottler(requestController, requestThrottlerSpy);

        double newScheduledThroughput = 3.0;
        requestController.renewThroughputUsageCycle(newScheduledThroughput);
        Mockito.verify(requestThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(newScheduledThroughput);
    }
}
