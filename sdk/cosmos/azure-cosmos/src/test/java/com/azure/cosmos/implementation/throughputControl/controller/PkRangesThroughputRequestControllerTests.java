// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller;

import com.azure.cosmos.implementation.DocumentServiceRequestContext;
import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.MetadataDiagnosticsContext;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.collections.list.UnmodifiableList;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;

public class PkRangesThroughputRequestControllerTests {
    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private static String targetCollectionRid;
    private static double scheduledThroughput;
    private static PartitionKeyRange randomPkRange;
    private static GlobalEndpointManager globalEndpointManager;
    private static RxPartitionKeyRangeCache pkRangeCache;
    private static List<PartitionKeyRange> pkRanges;
    private static URI readLocation;

    private PkRangesThroughputRequestController requestController;

    @BeforeClass(groups = "unit")
    public static void beforeClass_PkRangesThroughputRequestControllerTests() throws URISyntaxException {
        targetCollectionRid = "FakeCollection==";
        scheduledThroughput = 2.0;
        randomPkRange = new PartitionKeyRange(UUID.randomUUID().toString(), "randomMin", "randomMax");

        readLocation = new URI("https://read-localtion1.documents.azure.com");
        globalEndpointManager = Mockito.mock(GlobalEndpointManager.class);
        Mockito.doReturn(new UnmodifiableList<>(Collections.singletonList(readLocation))).when(globalEndpointManager).getReadEndpoints();

        PartitionKeyRange pkRange1 = new PartitionKeyRange(UUID.randomUUID().toString(), "AA", "BB");
        PartitionKeyRange pkRange2 = new PartitionKeyRange(UUID.randomUUID().toString(), "BB", "CC");
        pkRanges = new ArrayList<>();
        pkRanges.add(pkRange1);
        pkRanges.add(pkRange2);
        pkRangeCache = Mockito.mock(RxPartitionKeyRangeCache.class);
        Mockito.when(
            pkRangeCache.tryGetOverlappingRangesAsync(
                any(MetadataDiagnosticsContext.class),
                eq(targetCollectionRid),
                eq(RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES),
                eq(true),
                anyMapOf(String.class, Object.class))
        ).thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));
    }

    @BeforeMethod(groups = "unit")
    public void before_PkRangesThroughputRequestControllerTests() {
        requestController = new PkRangesThroughputRequestController(globalEndpointManager, pkRangeCache, targetCollectionRid, scheduledThroughput);
    }

    @Test(groups = "unit")
    public void init() {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ConcurrentHashMap<String, ThroughputRequestThrottler>> requestThrottlerMap =
            ReflectionUtils.getRequestThrottlerMap(requestController);

        Set<URI> locations = new HashSet<>();
        locations.add(readLocation);

        assertThat(requestThrottlerMap).size().isEqualTo(locations.size());
        assertThat(Collections.list(requestThrottlerMap.keys())).containsAll(locations);

        for (URI location : locations) {
            assertThat(Collections.list(requestThrottlerMap.get(location).keys()))
                .containsAll(pkRanges.stream().map(PartitionKeyRange::getId).collect(Collectors.toList()));
        }
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
    public void processRequest() throws URISyntaxException {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ConcurrentHashMap<String, ThroughputRequestThrottler>> requestThrottlerByRegion =
            ReflectionUtils.getRequestThrottlerMap(requestController);

        PartitionKeyRange pkRange = pkRanges.get(0);
        ConcurrentHashMap<String,ThroughputRequestThrottler> requestThrottlerByPkRangeId = requestThrottlerByRegion.get(readLocation);
        ThroughputRequestThrottler writeLocationThrottlerSpy = Mockito.spy(requestThrottlerByPkRangeId.get(pkRange.getId()));
        requestThrottlerByPkRangeId.put(pkRange.getId(), writeLocationThrottlerSpy);

        // First request: Can find the matching region request throttler in request controller
        RxDocumentServiceRequest request1Mock = this.createMockRequest(pkRange, readLocation);

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request1Mono = request1MonoPublisher.mono();
        StoreResponse storeResponse1Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request1Mock, request1Mono))
            .then(() -> request1MonoPublisher.emit(storeResponse1Mock))
            .expectNext(storeResponse1Mock)
            .verifyComplete();
        Mockito.verify(writeLocationThrottlerSpy, Mockito.times(1)).processRequest(request1Mock, request1Mono);

        // Second request: Cannot find the matching region request throttler in request controller, will create a new one
        RxDocumentServiceRequest request2Mock = this.createMockRequest(pkRange, new URI("https://write-localtion2.documents.azure.com"));

        TestPublisher<StoreResponse> request2MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> request2Mono = request2MonoPublisher.mono();
        StoreResponse storeResponse2Mock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(requestController.processRequest(request2Mock, request2Mono))
            .then(() -> request2MonoPublisher.emit(storeResponse2Mock))
            .expectNext(storeResponse2Mock)
            .verifyComplete();

        assertThat(requestThrottlerByRegion).size().isEqualTo(2);
    }

    @Test(groups = "unit")
    public void renewThroughputUsageCycle() {
        requestController.init().subscribe();

        ConcurrentHashMap<URI, ConcurrentHashMap<String, ThroughputRequestThrottler>> requestThrottlerByRegion =
            ReflectionUtils.getRequestThrottlerMap(requestController);

        List<ThroughputRequestThrottler> requestThrottlerSpies = new ArrayList<>();
        for (ConcurrentHashMap<String, ThroughputRequestThrottler> requestThrottlerByPkRangeId : requestThrottlerByRegion.values()) {
            for (String pkRangeId: Collections.list(requestThrottlerByPkRangeId.keys())) {
                ThroughputRequestThrottler requestThrottlerSpy = Mockito.spy(requestThrottlerByPkRangeId.get(pkRangeId));
                requestThrottlerSpies.add(requestThrottlerSpy);
                requestThrottlerByPkRangeId.put(pkRangeId, requestThrottlerSpy);
            }
        }

        double newScheduledThroughput = 3.0;
        double throughputPerPkRange = newScheduledThroughput/pkRanges.size();
        requestController.renewThroughputUsageCycle(newScheduledThroughput);
        for (ThroughputRequestThrottler requestThrottlerSpy : requestThrottlerSpies) {
            Mockito.verify(requestThrottlerSpy, Mockito.times(1)).renewThroughputUsageCycle(throughputPerPkRange);
        }
    }

    private RxDocumentServiceRequest createMockRequest(PartitionKeyRange resolvedPkRange, URI serviceEndpoint) {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        DocumentServiceRequestContext requestContextMock = Mockito.mock(DocumentServiceRequestContext.class);
        requestContextMock.resolvedPartitionKeyRange = resolvedPkRange;
        requestMock.requestContext = requestContextMock;
        Mockito.doReturn(serviceEndpoint).when(globalEndpointManager).resolveServiceEndpoint(requestMock);

        return requestMock;
    }
}
