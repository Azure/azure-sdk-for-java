// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.ThroughputRequestThrottler;
import com.azure.cosmos.implementation.throughputControl.controller.request.PkRangesThroughputRequestController;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.OPTIONAL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class PkRangesThroughputRequestControllerTests {
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private static String targetCollectionRid;
    private static double scheduledThroughput;
    private static PartitionKeyRange randomPkRange;
    private static RxPartitionKeyRangeCache pkRangeCache;
    private static List<PartitionKeyRange> pkRanges;

    private PkRangesThroughputRequestController requestController;

    @BeforeClass(groups = "unit")
    public static void beforeClass_PkRangesThroughputRequestControllerTests() throws URISyntaxException {
        targetCollectionRid = "FakeCollection==";
        scheduledThroughput = 2.0;
        randomPkRange = new PartitionKeyRange(UUID.randomUUID().toString(), "randomMin", "randomMax");

        PartitionKeyRange pkRange1 = new PartitionKeyRange(UUID.randomUUID().toString(), "AA", "BB");
        PartitionKeyRange pkRange2 = new PartitionKeyRange(UUID.randomUUID().toString(), "BB", "CC");
        pkRanges = new ArrayList<>();
        pkRanges.add(pkRange1);
        pkRanges.add(pkRange2);
        pkRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        Mockito.when(
            pkRangeCache.tryGetOverlappingRangesAsync(
                any(),
                eq(targetCollectionRid),
                eq(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES),
                eq(true),
                any())
        ).thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));
    }

    @BeforeMethod(groups = "unit")
    public void before_PkRangesThroughputRequestControllerTests() {
        requestController = new PkRangesThroughputRequestController(pkRangeCache, targetCollectionRid, scheduledThroughput);
    }

    @Test(groups = "unit")
    public void init() {
        // Test init can complete without error
        requestController.init().subscribe();
    }

    @Test(groups = "unit")
    public void canHandleRequest() {
        requestController.init().subscribe();

        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        DocumentServiceRequestContext requestContext = Mockito.mock(DocumentServiceRequestContext.class);
        requestMock.requestContext = requestContext;

        for (PartitionKeyRange pkRange : pkRanges) {
            requestContext.resolvedPartitionKeyRange = pkRange;
            assertThat(requestController.canHandleRequest(requestMock)).isTrue();
        }

        requestContext.resolvedPartitionKeyRange = randomPkRange;
        assertThat(requestController.canHandleRequest(requestMock)).isFalse();
    }

    @Test(groups = "unit")
    public void processRequest() {
        requestController.init().subscribe();

        ConcurrentHashMap<String, ThroughputRequestThrottler> requestThrottlerMap =
            ReflectionUtils.getRequestThrottler(requestController);

        PartitionKeyRange pkRange = pkRanges.get(0);
        ThroughputRequestThrottler writeLocationThrottlerSpy = Mockito.spy(requestThrottlerMap.get(pkRange.getId()));
        requestThrottlerMap.put(pkRange.getId(), writeLocationThrottlerSpy);

        RxDocumentServiceRequest request1Mock = this.createMockRequest(pkRange);

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request1Mono = request1MonoPublisher.mono();
        StoreResponse storeResponse1Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request1Mock, request1Mono))
            .then(() -> request1MonoPublisher.emit(storeResponse1Mock))
            .expectNext(storeResponse1Mock)
            .verifyComplete();
        Mockito.verify(writeLocationThrottlerSpy, Mockito.times(1)).processRequest(request1Mock, request1Mono);
    }

    @Test(groups = "unit")
    public void renewThroughputUsageCycle() {
        requestController.init().subscribe();

        ConcurrentHashMap<String, ThroughputRequestThrottler> requestThrottlerMap =
            ReflectionUtils.getRequestThrottler(requestController);

        List<ThroughputRequestThrottler> requestThrottlerSpies = new ArrayList<>();
        for (String pkRangeId : Collections.list(requestThrottlerMap.keys())) {
            ThroughputRequestThrottler requestThrottlerSpy = Mockito.spy(requestThrottlerMap.get(pkRangeId));
            requestThrottlerSpies.add(requestThrottlerSpy);
            requestThrottlerMap.put(pkRangeId, requestThrottlerSpy);
        }

        double newScheduledThroughput = 3.0;
        double throughputPerPkRange = newScheduledThroughput/pkRanges.size();
        requestController.renewThroughputUsageCycle(newScheduledThroughput);
        for (ThroughputRequestThrottler requestThrottlerSpy : requestThrottlerSpies) {
            Mockito.verify(requestThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(throughputPerPkRange);
        }
    }

    private RxDocumentServiceRequest createMockRequest(PartitionKeyRange resolvedPkRange) {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(OperationType.Read).when(requestMock).getOperationType();
        DocumentServiceRequestContext requestContextMock = Mockito.mock(DocumentServiceRequestContext.class);
        requestContextMock.resolvedPartitionKeyRange = resolvedPkRange;
        requestMock.requestContext = requestContextMock;

        return requestMock;
    }
}
