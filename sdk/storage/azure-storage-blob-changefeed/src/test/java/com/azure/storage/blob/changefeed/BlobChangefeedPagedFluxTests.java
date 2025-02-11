// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.changefeed;

import com.azure.storage.blob.changefeed.implementation.models.BlobChangefeedEventWrapper;
import com.azure.storage.blob.changefeed.implementation.models.ChangefeedCursor;
import com.azure.storage.blob.changefeed.models.BlobChangefeedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BlobChangefeedPagedFluxTests {
    private ChangefeedFactory mockChangefeedFactory;
    private Changefeed mockChangefeed;

    private List<ChangefeedCursor> mockCursors;
    private List<BlobChangefeedEvent> mockEvents;
    private List<BlobChangefeedEventWrapper> mockEventWrappers;

    @BeforeEach
    public void setup() {
        setupEvents();

        mockChangefeedFactory = mock(ChangefeedFactory.class);
        mockChangefeed = mock(Changefeed.class);

        when(mockChangefeed.getEvents()).thenReturn(Flux.fromIterable(mockEventWrappers));
    }

    /* No user cursor. */
    @Test
    public void subscribeMin() {
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime))
            .expectNext(mockEvents.get(0))
            .expectNext(mockEvents.get(1))
            .expectNext(mockEvents.get(2))
            .expectNext(mockEvents.get(3))
            .expectNext(mockEvents.get(4))
            .expectNext(mockEvents.get(5))
            .expectNext(mockEvents.get(6))
            .expectNext(mockEvents.get(7))
            .expectNext(mockEvents.get(8))
            .expectNext(mockEvents.get(9))
            .verifyComplete();

        verify(mockChangefeedFactory).getChangefeed(startTime, endTime);
        verify(mockChangefeed).getEvents();
    }

    /* user cursor. */
    @Test
    public void subscribeCursor() {
        String cursor = "cursor";
        when(mockChangefeedFactory.getChangefeed(any(String.class))).thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, cursor))
            .expectNext(mockEvents.get(0))
            .expectNext(mockEvents.get(1))
            .expectNext(mockEvents.get(2))
            .expectNext(mockEvents.get(3))
            .expectNext(mockEvents.get(4))
            .expectNext(mockEvents.get(5))
            .expectNext(mockEvents.get(6))
            .expectNext(mockEvents.get(7))
            .expectNext(mockEvents.get(8))
            .expectNext(mockEvents.get(9))
            .verifyComplete();

        verify(mockChangefeedFactory).getChangefeed(cursor);
        verify(mockChangefeed).getEvents();
    }

    @Test
    public void byPageMin() {
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime).byPage())
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)))
            .verifyComplete();

        verify(mockChangefeedFactory).getChangefeed(startTime, endTime);
        verify(mockChangefeed).getEvents();
    }

    @Test
    public void byPageSize() {
        int size = 3;
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime).byPage(size))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(2).serialize(),
                Arrays.asList(0, 1, 2)))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(5).serialize(),
                Arrays.asList(3, 4, 5)))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(8).serialize(),
                Arrays.asList(6, 7, 8)))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(),
                Collections.singletonList(9)))
            .verifyComplete();

        verify(mockChangefeedFactory).getChangefeed(startTime, endTime);
        verify(mockChangefeed).getEvents();
    }

    @Test
    public void byPageSizeContinuationTokenOverload() {
        int size = 7;
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime).byPage(null, size))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(6).serialize(),
                Arrays.asList(0, 1, 2, 3, 4, 5, 6)))
            .assertNext(pagedResponse -> validatePagedResponse(pagedResponse, mockCursors.get(9).serialize(),
                Arrays.asList(7, 8, 9)))
            .verifyComplete();

        verify(mockChangefeedFactory).getChangefeed(startTime, endTime);
        verify(mockChangefeed).getEvents();
    }

    @Test
    public void byPageContinuationTokenError() {
        String randomCursor = "randomCursor";
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime);

        StepVerifier.create(pagedFlux.byPage(randomCursor)).verifyError(UnsupportedOperationException.class);
        StepVerifier.create(pagedFlux.byPage(randomCursor, 7)).verifyError(UnsupportedOperationException.class);
    }

    @Test
    public void byPageIA() {
        OffsetDateTime startTime = OffsetDateTime.MIN;
        OffsetDateTime endTime = OffsetDateTime.MAX;
        when(mockChangefeedFactory.getChangefeed(any(OffsetDateTime.class), any(OffsetDateTime.class)))
            .thenReturn(mockChangefeed);

        StepVerifier.create(new BlobChangefeedPagedFlux(mockChangefeedFactory, startTime, endTime).byPage(null, -35))
            .verifyError(IllegalArgumentException.class);
    }

    private void validatePagedResponse(BlobChangefeedPagedResponse pagedResponse, String expectedCursor,
        List<Integer> expectedEvents) {
        assertEquals(expectedCursor, pagedResponse.getContinuationToken());
        assertEquals(expectedEvents.size(), pagedResponse.getValue().size());

        for (int i : expectedEvents) {
            assertTrue(pagedResponse.getValue().contains(mockEvents.get(i)));
        }
    }

    private void setupEvents() {
        mockEvents = new ArrayList<>();
        mockCursors = new ArrayList<>();
        mockEventWrappers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            BlobChangefeedEvent event = MockedChangefeedResources.getMockBlobChangefeedEvent(i);
            mockEvents.add(event);
        }
        String urlHost = "testaccount.blob.core.windows.net";
        OffsetDateTime endTime = OffsetDateTime.of(2020, 10, 2, 20, 15, 0, 0, ZoneOffset.UTC);
        String segmentPath = "idx/segments/2020/08/02/2300/meta.json";
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/00/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/01/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/02/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/03/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/04/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/05/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/06/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/07/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/08/2020/08/02/2300/"));
        mockCursors.add(new ChangefeedCursor(urlHost, endTime).toSegmentCursor(segmentPath, null)
            .toShardCursor("log/09/2020/08/02/2300/"));

        for (int i = 0; i < 10; i++) {
            mockEventWrappers.add(new BlobChangefeedEventWrapper(mockEvents.get(i), mockCursors.get(i)));
        }
    }
}
