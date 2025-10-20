// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
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
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.directconnectivity.ReflectionUtils;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
        ChangeFeedObserver<ChangeFeedProcessorItem> observerMock = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);

        // Setup initial state with continuation token
        ChangeFeedStateV1 initialChangeFeedState = this.getChangeFeedStateWithContinuationTokens(1);

        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        ProcessorSettings processorSettings = new ProcessorSettings(initialChangeFeedState, containerMock);
        processorSettings.withMaxItemCount(10);

        // Setup lease and checkpointer mocks
        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Mockito.when(leaseMock.getContinuationToken()).thenReturn(initialChangeFeedState.toString());

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
        PartitionProcessorImpl<ChangeFeedProcessorItem> partitionProcessor = new PartitionProcessorImpl<>(
            observerMock,
            changeFeedContextClientMock,
            processorSettings,
            partitionCheckpointer,
            leaseMock,
            ChangeFeedProcessorItem.class,
            ChangeFeedMode.INCREMENTAL,
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
    public void partitionSplitHappenOnFirstRequest() {
        @SuppressWarnings("unchecked")  ChangeFeedObserver<ChangeFeedProcessorItem> observerMock =
            (ChangeFeedObserver<ChangeFeedProcessorItem>) Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        Mockito
            .when(changeFeedContextClientMock.createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Flux.error(new PartitionKeyRangeIsSplittingException()));

        ChangeFeedState changeFeedState = this.getChangeFeedStateWithContinuationTokens(2);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        ProcessorSettings processorSettings = new ProcessorSettings(changeFeedState, containerMock);
        processorSettings.withMaxItemCount(10);

        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Mockito
            .when(leaseMock.getContinuationToken())
            .thenReturn(changeFeedState.getContinuation().getCurrentContinuationToken().getToken());

        LeaseCheckpointer leaseCheckpointerMock = Mockito.mock(LeaseCheckpointer.class);
        PartitionCheckpointer partitionCheckpointer = new PartitionCheckpointerImpl(leaseCheckpointerMock, leaseMock);

        PartitionProcessorImpl<ChangeFeedProcessorItem> partitionProcessor = new PartitionProcessorImpl<>(
            observerMock,
            changeFeedContextClientMock,
            processorSettings,
            partitionCheckpointer,
            leaseMock,
            ChangeFeedProcessorItem.class,
            ChangeFeedMode.INCREMENTAL,
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

    @Test(groups = "unit")
    public void partitionProcessingErrorWhenInternalServerErrorIsHit() {

        CosmosException parsingException = BridgeInternal.createCosmosException(
            "A parsing error occurred.",
            new IOException("An error occurred."),
            new HashMap<>(),
            HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR,
            null);

        BridgeInternal.setSubStatusCode(parsingException, HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE);

        ChangeFeedObserver<ChangeFeedProcessorItem> observerMock = Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);

        // Setup initial state with continuation token
        ChangeFeedStateV1 initialChangeFeedState = this.getChangeFeedStateWithContinuationTokens(1);

        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        ProcessorSettings processorSettings = new ProcessorSettings(initialChangeFeedState, containerMock);
        processorSettings.withMaxItemCount(10);

        // Setup lease and checkpointer mocks
        Lease leaseMock = Mockito.mock(ServiceItemLeaseV1.class);
        Mockito.when(leaseMock.getContinuationToken()).thenReturn(initialChangeFeedState.toString());

        PartitionCheckpointer partitionCheckpointer = Mockito.mock(PartitionCheckpointerImpl.class);

        String lastContinuationToken = initialChangeFeedState.toString();

        Mockito
            .when(changeFeedContextClientMock.createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Flux.error(parsingException));

        // Checkpointing mock setup
        final ChangeFeedState continuationState = ChangeFeedState.fromString(lastContinuationToken);
        Mockito.when(partitionCheckpointer.checkpointPartition(continuationState))
            .thenReturn(Mono.empty());

        // Create processor
        PartitionProcessorImpl<ChangeFeedProcessorItem> partitionProcessor = new PartitionProcessorImpl<>(
            observerMock,
            changeFeedContextClientMock,
            processorSettings,
            partitionCheckpointer,
            leaseMock,
            ChangeFeedProcessorItem.class,
            ChangeFeedMode.INCREMENTAL,
            null);

        StepVerifier
            .create(partitionProcessor.run(new CancellationTokenSource().getToken()))
            .verifyComplete();

        RuntimeException runtimeException = partitionProcessor.getResultException();
        assertThat(runtimeException).isNotNull();
        assertThat(runtimeException.getCause()).isInstanceOf(CosmosException.class);
        CosmosException cosmosException = (CosmosException) runtimeException.getCause();
        assertThat(cosmosException.getStatusCode()).isEqualTo(HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR);
        assertThat(cosmosException.getSubStatusCode()).isEqualTo(HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE);
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
