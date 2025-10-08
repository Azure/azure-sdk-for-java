// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk.controller;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.throughputControl.sdk.config.LocalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.group.local.LocalThroughputControlGroupController;
import com.azure.cosmos.models.PriorityLevel;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class LocalThroughputControllerTests {

    private static final Range<String> RANGE_INCLUDING_ALL_PARTITION_KEY_RANGES = new Range<String>(
        PartitionKeyInternalHelper.MinimumInclusiveEffectivePartitionKey,
        PartitionKeyInternalHelper.MaximumExclusiveEffectivePartitionKey, true, false);

    private static String targetCollectionRid;
    private static String databaseRid;
    private CosmosAsyncContainer container;
    private PartitionKeyRange pkRange1;
    private List<PartitionKeyRange> pkRanges;
    private RxPartitionKeyRangeCache pkRangeCache;
    private static LocalThroughputControlGroupController controller;

    @BeforeClass(groups = "unit")
    public void beforeClass_LocalThroughputRequestControllerTests() {
        targetCollectionRid = "FakeCollection==";
        databaseRid = "FakeDatabase==";
        container = createMockContainer();

        pkRange1 = new PartitionKeyRange(UUID.randomUUID().toString(), "AA", "BB");
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
                any(),
                any())
        ).thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRanges)));
        Mockito.when(
            pkRangeCache.tryGetRangeByPartitionKeyRangeId(
                any(),
                any(),
                any(),
                any(),
                any())
            ).thenReturn(Mono.just(Utils.ValueHolder.initialize(pkRange1)));
    }

    @DataProvider
    public static Object[][] throughputControlConfigProvider() {
        return new Object[][]{
            { 6, null, null, 6 },
            { 6, null, PriorityLevel.HIGH, 6 },
            { null, 0.5, null, 3 },
            { null, 0.5, PriorityLevel.HIGH, 3 },
            { Integer.MAX_VALUE, null, PriorityLevel.HIGH, Integer.MAX_VALUE },
            { Integer.MAX_VALUE, 0.5, PriorityLevel.HIGH, 3 }
        };
    }

    @Test(groups = "unit", dataProvider = "throughputControlConfigProvider")
    public void setPriorityLevel(Integer targetThroughput, Double targetThroughputThreshold, PriorityLevel priorityLevel, int expectedClientAllocatedThroughput) {
        LocalThroughputControlGroup throughputControlGroup = new LocalThroughputControlGroup(
            "test-" + UUID.randomUUID(),
            container,
            targetThroughput,
            targetThroughputThreshold,
            priorityLevel,
            true,
            false);

        controller = new LocalThroughputControlGroupController(ConnectionMode.DIRECT,
            throughputControlGroup,
            6,
            pkRangeCache,
            targetCollectionRid,
            null);
        controller.init().subscribe();

        RxDocumentServiceRequest requestMock = createMockRequest();

        TestPublisher<StoreResponse> request1MonoPublisher = TestPublisher.create();
        Mono<StoreResponse> requestMono = request1MonoPublisher.mono();
        StoreResponse storeResponseMock = Mockito.mock(StoreResponse.class);

        StepVerifier.create(controller.processRequest(requestMock, requestMono))
            .then(() -> request1MonoPublisher.emit(storeResponseMock))
            .expectNext(storeResponseMock)
            .verifyComplete();
        Mockito.verify(requestMock, Mockito.times(1)).setPriorityLevel(eq(priorityLevel));
        assertThat(controller.getClientAllocatedThroughput()).isEqualTo(expectedClientAllocatedThroughput);
    }

    private RxDocumentServiceRequest createMockRequest() {
        RxDocumentServiceRequest requestMock = Mockito.mock(RxDocumentServiceRequest.class);
        Mockito.doReturn(OperationType.Read).when(requestMock).getOperationType();
        DocumentServiceRequestContext requestContextMock = new DocumentServiceRequestContext();
        requestContextMock.resolvedPartitionKeyRange = pkRange1;
        requestMock.requestContext = requestContextMock;

        return requestMock;
    }

    private static CosmosAsyncContainer createMockContainer() {
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        CosmosAsyncDatabase databaseMock = Mockito.mock(CosmosAsyncDatabase.class);

        Mockito.doReturn(targetCollectionRid).when(containerMock).getId();
        Mockito.doReturn(databaseMock).when(containerMock).getDatabase();
        Mockito.doReturn(databaseRid).when(databaseMock).getId();
        return containerMock;
    }
}
