// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.changefeed.CancellationTokenSource;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.LeaseCheckpointer;
import com.azure.cosmos.implementation.changefeed.PartitionCheckpointer;
import com.azure.cosmos.implementation.changefeed.ProcessorSettings;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStartFromInternal;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedState;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedStateV1;
import com.azure.cosmos.implementation.changefeed.epkversion.ServiceItemLeaseV1;
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PartitionProcessorImplTests {

    @DataProvider(name = "changeFeed304ResponseContinuationArgProvider")
    public static Object[][] changeFeed304ResponseContinuationArgProvider() {
        return new Object[][]{
            // hasMoreResult, hasContinuationTokenChange, shouldDoCheckpoint
            { true, true, true },
            { true, false, true },
            { false, true, true },
            { false, false, false}
        };
    }

    @Test(groups = "unit", dataProvider = "changeFeed304ResponseContinuationArgProvider")
    public void shouldCheckpointFor304WhenContinuationTokenChanges(
        boolean hasMoreResult,
        boolean hasContinuationTokenChange,
        boolean shouldDoCheckpoint) {
        ChangeFeedObserver<JsonNode> observerMock = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);

        // Setup initial state with continuation token
        ChangeFeedStateV1 initialChangeFeedState = this.getChangeFeedStateWithContinuationTokens(1);

        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        ProcessorSettings processorSettings = new ProcessorSettings(initialChangeFeedState, containerMock);
        processorSettings.withMaxItemCount(10);

        // Setup lease and checkpointer mocks
        Lease leaseMock = Mockito.mock(ServiceItemLease.class);
        Mockito
            .when(leaseMock.getContinuationToken())
            .thenReturn(initialChangeFeedState.getContinuation().getCurrentContinuationToken().getToken());

        PartitionCheckpointer partitionCheckpointer = Mockito.mock(PartitionCheckpointerImpl.class);

        // Mock feed response with no changes
        FeedResponse<Object> emptyResponse = Mockito.mock(FeedResponse.class);
        String lastContinuationToken = initialChangeFeedState.toString();
        if (hasContinuationTokenChange) {
            ChangeFeedState newChangeFeedState = this.getChangeFeedStateWithContinuationTokens(1);
            lastContinuationToken = newChangeFeedState.toString();
        }
        Mockito.when(emptyResponse.getContinuationToken()).thenReturn(lastContinuationToken);
        Mockito.when(emptyResponse.getResults()).thenReturn(new ArrayList<>());
        ReflectionUtils.setNoChanges(emptyResponse, !hasMoreResult);

        Mockito
            .when(changeFeedContextClientMock.createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Flux.just(emptyResponse))
            .thenReturn(Flux.error(new RuntimeException("terminating test")));

        // Checkpointing mock setup
        final ChangeFeedState continuationState = ChangeFeedState.fromString(lastContinuationToken);
        Mockito.when(partitionCheckpointer.checkpointPartition(continuationState))
            .thenReturn(Mono.empty());

        // Create processor
        PartitionProcessorImpl partitionProcessor = new PartitionProcessorImpl(
            observerMock,
            changeFeedContextClientMock,
            processorSettings,
            partitionCheckpointer,
            leaseMock,
            null);

        StepVerifier
            .create(partitionProcessor.run(new CancellationTokenSource().getToken()))
            .verifyComplete();

        if (shouldDoCheckpoint) {
            // Verify checkpoint was called since continuation token changed
            Mockito
                .verify(partitionCheckpointer, Mockito.times(1))
                .checkpointPartition(Mockito.any());
        } else {
            // Verify checkpoint was not called since continuation token has not changed
            Mockito
                .verify(partitionCheckpointer, Mockito.times(0))
                .checkpointPartition(Mockito.any());
        }
    }

    @Test(groups = "unit")
    public void processedTimeSetAfterProcessing() {
        ChangeFeedObserver<JsonNode> observerMock = Mockito.mock(ChangeFeedObserver.class);
        Mockito.when(observerMock.processChanges(Mockito.any(), Mockito.anyList())).thenReturn(Mono.empty());

        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);

        ChangeFeedStateV1 startState = getChangeFeedStateWithContinuationTokens(1);
        ProcessorSettings settings = new ProcessorSettings(startState, containerMock);
        settings.withMaxItemCount(10);

        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Mockito.when(leaseMock.getContinuationToken()).thenReturn(startState.toString());

        PartitionCheckpointer checkpointerMock = Mockito.mock(PartitionCheckpointerImpl.class);

        // Create a feed response with one mocked result
        @SuppressWarnings("unchecked") FeedResponse<ChangeFeedProcessorItem> feedResponseMock = Mockito.mock(FeedResponse.class);
        List<ChangeFeedProcessorItem> results = new ArrayList<>();
        results.add(Mockito.mock(ChangeFeedProcessorItem.class));
        AtomicInteger counter = new AtomicInteger(0);
        Mockito.when(feedResponseMock.getResults()).thenAnswer(invocation -> {
            Thread.sleep(500);
            return counter.getAndIncrement() < 10 ? results : new ArrayList<>();
        });
        ChangeFeedState changeFeedState = this.getChangeFeedStateWithContinuationTokens(1);
        Mockito.when(feedResponseMock.getContinuationToken()).thenReturn(changeFeedState.toString());

        // The processor will continuously fetch, but we will cancel shortly after first batch
        Mockito.doReturn(Flux.just(feedResponseMock))
            .when(changeFeedContextClientMock)
            .createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(), Mockito.any());

        PartitionProcessorImpl processor = new PartitionProcessorImpl(
            observerMock,
            changeFeedContextClientMock,
            settings,
            checkpointerMock,
            leaseMock,
            null);
        Instant initialTime = processor.getLastProcessedTime();

        CancellationTokenSource cts = new CancellationTokenSource();
        Mono<Void> runMono = processor.run(cts.getToken());

        StepVerifier.create(runMono)
            .thenAwait(Duration.ofMillis(800))
            .then(cts::cancel)
            .verifyComplete();

        assertThat(processor.getLastProcessedTime()).isAfter(initialTime);
    }

    @Test
    public void partitionSplitHappenOnFirstRequest() {
        @SuppressWarnings("unchecked") ChangeFeedObserver<JsonNode> observerMock =
            (ChangeFeedObserver<JsonNode>) Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        Mockito
            .when(changeFeedContextClientMock.createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(),
                Mockito.any()))
            .thenReturn(Flux.error(new PartitionKeyRangeIsSplittingException()));

        ChangeFeedState changeFeedState = this.getChangeFeedStateWithContinuationTokens(2);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        ProcessorSettings processorSettings = new ProcessorSettings(changeFeedState, containerMock);
        processorSettings.withMaxItemCount(10);

        Lease leaseMock = Mockito.mock(ServiceItemLease.class);
        Mockito
            .when(leaseMock.getContinuationToken())
            .thenReturn(changeFeedState.getContinuation().getCurrentContinuationToken().getToken());

        LeaseCheckpointer leaseCheckpointerMock = Mockito.mock(LeaseCheckpointer.class);
        PartitionCheckpointer partitionCheckpointer = new PartitionCheckpointerImpl(leaseCheckpointerMock, leaseMock);

        PartitionProcessorImpl partitionProcessor = new PartitionProcessorImpl(
            observerMock,
            changeFeedContextClientMock,
            processorSettings,
            partitionCheckpointer,
            leaseMock,
            null
        );

        StepVerifier.create(partitionProcessor.run(new CancellationTokenSource().getToken()))
                    .verifyComplete();

        RuntimeException runtimeException = partitionProcessor.getResultException();
        assertThat(runtimeException).isNotNull();
        assertThat(runtimeException).isInstanceOf(FeedRangeGoneException.class);
        FeedRangeGoneException feedRangeGoneException = (FeedRangeGoneException) runtimeException;
        assertThat(feedRangeGoneException.getLastContinuation()).isEqualTo(leaseMock.getContinuationToken());
    }

    private ChangeFeedStateV1 getChangeFeedStateWithContinuationTokens(int tokenCount) {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        String continuationDummy = UUID.randomUUID().toString();

        StringBuilder continuationBuilder = new StringBuilder();
        continuationBuilder.append("{\"V\":1,")
            .append("\"Rid\":\"").append(containerRid).append("\",")
            .append("\"Continuation\":[");

        for (int i = 0; i < tokenCount; i++) {
            if (i > 0) {
                continuationBuilder.append(",");
            }
            char minRange = (char)('A' + (i * 2));
            char maxRange = (char)('B' + (i * 2));
            continuationBuilder.append("{\"token\":\"").append(continuationDummy)
                .append("\",\"range\":{\"min\":\"").append(minRange).append(minRange)
                .append("\",\"max\":\"").append(maxRange).append(maxRange).append("\"}}");
        }

        continuationBuilder.append("],")
            .append("\"PKRangeId\":\"").append(pkRangeId).append("\"}");

        String continuationJson = continuationBuilder.toString();

        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        return new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            startFromSettings,
            continuation);
    }
}
