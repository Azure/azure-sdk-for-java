// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.sdk.controller;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.caches.RxCollectionCache;
import com.azure.cosmos.implementation.caches.RxPartitionKeyRangeCache;
import com.azure.cosmos.implementation.throughputControl.sdk.config.LocalThroughputControlGroup;
import com.azure.cosmos.implementation.throughputControl.sdk.config.SDKThroughputControlGroupInternal;
import com.azure.cosmos.implementation.throughputControl.sdk.controller.container.SDKThroughputContainerController;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.PriorityLevel;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SDKThroughputContainerControllerTests {

    @Test(groups = "unit")
    public void throughputQueryMonoNotSubscribedWhenOnlyTargetThroughputConfigured() {
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        CosmosAsyncDatabase databaseMock = Mockito.mock(CosmosAsyncDatabase.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn("FakeCollection").when(containerMock).getId();
        Mockito.doReturn(databaseMock).when(containerMock).getDatabase();
        Mockito.doReturn("FakeDatabase").when(databaseMock).getId();

        // Mock container.read() for resolveContainerResourceId()
        CosmosContainerResponse containerResponseMock = Mockito.mock(CosmosContainerResponse.class);
        CosmosContainerProperties containerPropertiesMock = Mockito.mock(CosmosContainerProperties.class);
        Mockito.when(containerResponseMock.getProperties()).thenReturn(containerPropertiesMock);
        Mockito.when(containerPropertiesMock.getResourceId()).thenReturn("fakeContainerRid");
        Mockito.when(containerMock.read()).thenReturn(Mono.just(containerResponseMock));

        // Group with targetThroughput only (no threshold)
        LocalThroughputControlGroup group = new LocalThroughputControlGroup(
            "test-" + UUID.randomUUID(),
            containerMock,
            6,        // targetThroughput
            null,     // targetThroughputThreshold — NOT set
            PriorityLevel.HIGH,
            true,
            false);

        Map<String, SDKThroughputControlGroupInternal> groups = new HashMap<>();
        groups.put(group.getGroupName(), group);

        AtomicBoolean throughputQuerySubscribed = new AtomicBoolean(false);
        Mono<Integer> trackingThroughputQueryMono = Mono.<Integer>just(10000)
            .doOnSubscribe(s -> throughputQuerySubscribed.set(true));

        RxCollectionCache collectionCacheMock = Mockito.mock(RxCollectionCache.class);
        RxPartitionKeyRangeCache pkRangeCacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);

        SDKThroughputContainerController controller = new SDKThroughputContainerController(
            collectionCacheMock,
            ConnectionMode.DIRECT,
            groups,
            pkRangeCacheMock,
            null,
            trackingThroughputQueryMono);

        // Call init() which drives the full pipeline:
        //   resolveContainerResourceId -> resolveContainerMaxThroughput -> createAndInitializeGroupControllers
        // This exercises both getThroughputResolveLevel() (scope calculation) and
        // the resolveContainerMaxThroughput() guard. The last step may fail due to
        // insufficient mocking, but resolveContainerMaxThroughput has already completed by then.
        try {
            controller.<Object>init().block();
        } catch (Exception ignored) {
            // createAndInitializeGroupControllers may fail — acceptable for this test
        }

        assertThat(throughputQuerySubscribed.get())
            .as("throughputQueryMono should not be subscribed when only targetThroughput is configured")
            .isFalse();
    }

    @Test(groups = "unit")
    public void throughputQueryMonoSubscribedWhenThresholdConfigured() {
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        CosmosAsyncDatabase databaseMock = Mockito.mock(CosmosAsyncDatabase.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.doReturn("FakeCollection").when(containerMock).getId();
        Mockito.doReturn(databaseMock).when(containerMock).getDatabase();
        Mockito.doReturn("FakeDatabase").when(databaseMock).getId();

        // Mock container.read() for resolveContainerResourceId()
        CosmosContainerResponse containerResponseMock = Mockito.mock(CosmosContainerResponse.class);
        CosmosContainerProperties containerPropertiesMock = Mockito.mock(CosmosContainerProperties.class);
        Mockito.when(containerResponseMock.getProperties()).thenReturn(containerPropertiesMock);
        Mockito.when(containerPropertiesMock.getResourceId()).thenReturn("fakeContainerRid");
        Mockito.when(containerMock.read()).thenReturn(Mono.just(containerResponseMock));

        // Group with targetThroughputThreshold set
        LocalThroughputControlGroup group = new LocalThroughputControlGroup(
            "test-" + UUID.randomUUID(),
            containerMock,
            null,     // targetThroughput
            0.5,      // targetThroughputThreshold — IS set
            PriorityLevel.HIGH,
            true,
            false);

        Map<String, SDKThroughputControlGroupInternal> groups = new HashMap<>();
        groups.put(group.getGroupName(), group);

        AtomicBoolean throughputQuerySubscribed = new AtomicBoolean(false);
        Mono<Integer> trackingThroughputQueryMono = Mono.<Integer>just(10000)
            .doOnSubscribe(s -> throughputQuerySubscribed.set(true));

        RxCollectionCache collectionCacheMock = Mockito.mock(RxCollectionCache.class);
        RxPartitionKeyRangeCache pkRangeCacheMock = Mockito.mock(RxPartitionKeyRangeCache.class);

        SDKThroughputContainerController controller = new SDKThroughputContainerController(
            collectionCacheMock,
            ConnectionMode.DIRECT,
            groups,
            pkRangeCacheMock,
            null,
            trackingThroughputQueryMono);

        // Call init() — when targetThroughputThreshold is configured,
        // resolveContainerMaxThroughput should subscribe to the throughput query mono.
        try {
            controller.<Object>init().block();
        } catch (Exception ignored) {
            // createAndInitializeGroupControllers may fail — acceptable for this test
        }

        assertThat(throughputQuerySubscribed.get())
            .as("throughputQueryMono should be subscribed when targetThroughputThreshold is configured")
            .isTrue();
    }
}
