// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

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
import com.azure.cosmos.implementation.changefeed.exceptions.FeedRangeGoneException;
import com.azure.cosmos.implementation.feedranges.FeedRangeContinuation;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PartitionProcessorImplTests {

    @Test
    public void partitionSplitHappenOnFirstRequest() {
        @SuppressWarnings("unchecked")  ChangeFeedObserver<ChangeFeedProcessorItem> observerMock =
            (ChangeFeedObserver<ChangeFeedProcessorItem>) Mockito.mock(ChangeFeedObserver.class);
        ChangeFeedContextClient changeFeedContextClientMock = Mockito.mock(ChangeFeedContextClient.class);
        Mockito
            .when(changeFeedContextClientMock.createDocumentChangeFeedQuery(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(Flux.error(new PartitionKeyRangeIsSplittingException()));

        ChangeFeedState changeFeedState = this.getChangeFeedStateWithContinuationToken();
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
            ChangeFeedMode.INCREMENTAL
        );

        StepVerifier.create(partitionProcessor.run(new CancellationTokenSource().getToken()))
            .verifyComplete();

        RuntimeException runtimeException = partitionProcessor.getResultException();
        assertThat(runtimeException).isNotNull();
        assertThat(runtimeException).isInstanceOf(FeedRangeGoneException.class);
        FeedRangeGoneException feedRangeGoneException = (FeedRangeGoneException) runtimeException;
        assertThat(feedRangeGoneException.getLastContinuation()).isEqualTo(leaseMock.getContinuationToken());
    }

    private ChangeFeedState getChangeFeedStateWithContinuationToken() {
        String containerRid = "/cols/" + UUID.randomUUID();
        String pkRangeId = UUID.randomUUID().toString();
        String continuationDummy = UUID.randomUUID().toString();
        String continuationJson = String.format(
            "{\"V\":1," +
                "\"Rid\":\"%s\"," +
                "\"Continuation\":[" +
                "{\"token\":\"%s\",\"range\":{\"min\":\"AA\",\"max\":\"BB\"}}," +
                "{\"token\":\"%s\",\"range\":{\"min\":\"CC\",\"max\":\"DD\"}}" +
                "]," +
                "\"PKRangeId\":\"%s\"}",
            containerRid,
            continuationDummy,
            continuationDummy,
            pkRangeId);

        FeedRangePartitionKeyRangeImpl feedRange = new FeedRangePartitionKeyRangeImpl(pkRangeId);
        ChangeFeedStartFromInternal startFromSettings = ChangeFeedStartFromInternal.createFromNow();
        FeedRangeContinuation continuation = FeedRangeContinuation.convert(continuationJson);

        ChangeFeedState state = new ChangeFeedStateV1(
            containerRid,
            feedRange,
            ChangeFeedMode.INCREMENTAL,
            startFromSettings,
            continuation);

        return state;
    }
}
